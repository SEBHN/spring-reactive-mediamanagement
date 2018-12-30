package de.hhn.mvs;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private static final String FRONTEND_LOCALHOST = "http://localhost:4200";
    private static final String FRONTEND_STAGING = "https://sebhn.github.io";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf()
                .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .authorizeExchange()
                .pathMatchers(POST, "/users/register").permitAll()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfiguration() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.addAllowedMethod(PUT);
        corsConfig.addAllowedMethod(DELETE);
        corsConfig.setAllowedOrigins(Arrays.asList(FRONTEND_LOCALHOST, FRONTEND_STAGING));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}