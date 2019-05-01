package org.isegodin.spring.rest.socket.template.system.security;

import org.isegodin.spring.rest.socket.template.system.security.data.JsonAuthentication;
import org.isegodin.spring.rest.socket.template.system.security.data.JsonAuthenticationDetails;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;
import java.util.UUID;

/**
 * @author isegodin
 */

public class JsonAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;

    public JsonAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JsonAuthentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        UserDetails loadedUser = userDetailsService.loadUserByUsername(String.valueOf(authentication.getPrincipal()));

        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, loadedUser.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        String tokenId = new String(Base64.getEncoder().encode(
                (UUID.randomUUID().toString() + System.currentTimeMillis()).getBytes()
        ));

        return JsonAuthentication.builder()
                .tokenId(tokenId)
                .authenticated(true)
                .details(
                        JsonAuthenticationDetails.builder()
                                .login(String.valueOf(authentication.getPrincipal()))
                                .build()
                )
                .build();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
