package org.isegodin.spring.rest.socket.template.system.security;

import lombok.extern.slf4j.Slf4j;
import org.isegodin.spring.rest.socket.template.system.security.data.JsonAuthentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author isegodin
 */
@Slf4j
public class CachedSecurityContextRepository implements SecurityContextRepository {

    private final String tokenIdHeader;

    private final Cache<String, JsonAuthentication> authCache;

    public CachedSecurityContextRepository(String tokenIdHeader, Cache<String, JsonAuthentication> cache) {
        this.tokenIdHeader = tokenIdHeader;
        this.authCache = cache;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(
                getTokenIdHeader(requestResponseHolder.getRequest())
                        .map(authCache::get)
                        .orElse(null)
        );

        return context;
    }

    private Optional<String> getTokenIdHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(tokenIdHeader));
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof JsonAuthentication) {
            JsonAuthentication jsonAuthentication = (JsonAuthentication) authentication;

            authCache.put(jsonAuthentication.getTokenId(), jsonAuthentication);
        } else if (
                authentication != null &&
                !(authentication instanceof AnonymousAuthenticationToken)
        ) {
            log.warn("Unknown authentication type {}", authentication);
        }
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return getTokenIdHeader(request).map(authCache::get).isPresent();
    }
}
