package org.isegodin.spring.rest.socket.template.system.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.isegodin.spring.rest.socket.template.system.security.CachedSecurityContextRepository;
import org.isegodin.spring.rest.socket.template.system.security.JsonAuthenticationProvider;
import org.isegodin.spring.rest.socket.template.system.security.data.JsonAuthentication;
import org.isegodin.spring.rest.socket.template.system.security.filter.JsonAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author isegodin
 */
@Configuration
@EnableAutoConfiguration(
        exclude = {UserDetailsServiceAutoConfiguration.class}
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/logout").authenticated();

        http.csrf().disable();

        http.exceptionHandling()
//                .accessDeniedHandler()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        http.securityContext()
                .securityContextRepository(new CachedSecurityContextRepository("token-id", createAuthCache()));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        http.addFilterBefore(
                new JsonAuthenticationFilter(
                        "/login",
                        new ProviderManager(Arrays.asList(
                                new JsonAuthenticationProvider(
                                        userDetailsService(passwordEncoder::encode),
                                        passwordEncoder
                                )
                        )),
                        objectMapper
                ),
                UsernamePasswordAuthenticationFilter.class
        );

        http.logout();
//                .addLogoutHandler()
//                .permitAll();
    }

    private UserDetailsService userDetailsService(Function<String, String> stringStringFunction) {
        UserDetails user = User.builder()
                .username("admin")
                .password("admin")
                .roles("USER")
                .passwordEncoder(stringStringFunction)
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    private Cache<String, JsonAuthentication> createAuthCache() {
        CachingProvider provider = new EhcacheCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        CacheConfiguration<String, JsonAuthentication> nativeEhCacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class,
                JsonAuthentication.class,
                ResourcePoolsBuilder.heap(5_000)
        ).withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofMinutes(30)))
                .build();

        return cacheManager.createCache("auth-token-cache", Eh107Configuration.fromEhcacheCacheConfiguration(nativeEhCacheConfig));
    }

}
