package org.isegodin.spring.rest.socket.template.system.security.data;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author isegodin
 */
@Data
@Builder
public class JsonAuthentication implements Authentication {

    private final String tokenId;
    private boolean authenticated;
    private final JsonAuthenticationDetails details;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new LinkedList<>();
    }

    @Override
    public String getCredentials() {
        return null;
    }

    @Override
    public String getPrincipal() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
