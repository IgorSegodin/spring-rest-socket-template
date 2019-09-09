package org.isegodin.spring.security.template.system.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.isegodin.spring.security.template.system.security.data.AuthRequestDto;
import org.isegodin.spring.security.template.system.security.data.AuthResponseDto;
import org.isegodin.spring.security.template.system.security.data.JsonAuthentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author isegodin
 */
public class JsonAuthenticationFilter extends OncePerRequestFilter {

    private final RequestMatcher requestMatcher;

    private final AuthenticationManager authenticationManager;

    private final ObjectMapper objectMapper;

    public JsonAuthenticationFilter(String processLoginUrl, AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        this.requestMatcher = new AntPathRequestMatcher(processLoginUrl, "POST");
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);

            return;
        }

        try {
            JsonAuthentication authResult = attemptAuthentication(request, response);
            if (authResult == null) {
                // return immediately as subclass has indicated that it hasn't completed
                // authentication
                return;
            }

            successfulAuthentication(request, response, filterChain, authResult);
        } catch (InternalAuthenticationServiceException failed) {
            logger.error("An internal error occurred while trying to authenticate the user.", failed);
            unsuccessfulAuthentication(request, response, failed);
        } catch (AuthenticationException failed) {
            unsuccessfulAuthentication(request, response, failed);
        }
    }

    @SneakyThrows
    private JsonAuthentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        AuthRequestDto requestDto;
        try (InputStream is = new BufferedInputStream(request.getInputStream())) {
            requestDto = objectMapper.readValue(is, AuthRequestDto.class);
        }

        Authentication result = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));

        if (result instanceof JsonAuthentication) {
            return (JsonAuthentication) result;
        } else {
            logger.error("Unsupported Authentication type: " + result);
            return null;
        }
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) {
        SecurityContextHolder.clearContext();

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication request failed: " + failed.toString(), failed);
            logger.debug("Updated SecurityContextHolder to contain null Authentication");
        }
//        TODO remember-me
//        rememberMeServices.loginFail(request, response);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @SneakyThrows
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            JsonAuthentication authResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

        objectMapper.writeValue(
                response.getOutputStream(),
                AuthResponseDto.builder()
                        .tokenId(authResult.getTokenId())
                        .build()
        );

//        TODO remember-me
//        rememberMeServices.loginSuccess(request, response, authResult);
    }
}
