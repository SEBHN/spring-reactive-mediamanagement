package de.hhn.mvs;


import de.hhn.mvs.database.UserCrudRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.http.HttpMethod.POST;

@Primary
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private static final String FRONTEND_ANGULAR_LOCALHOST = "http://localhost:4200";
    private static final String FRONTEND_VUE_LOCALHOST = "http://localhost:8081";
    private static final String FRONTEND_VUE_LOCALHOST_STAGED = "http://localhost:8080";
    private static final String FRONTEND_STAGING = "https://sebhn.github.io";

    @Value("${own.security}")
    private boolean useOwnSecurity;

    private UserCrudRepo users;

    @Autowired
    public SecurityConfiguration(UserCrudRepo users) {
        this.users = users;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf()
                .disable()
                .authorizeExchange()
                .pathMatchers(POST, "/users/register").permitAll()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();

        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfiguration() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.addAllowedMethod(HttpMethod.PUT);
        corsConfig.addAllowedMethod(HttpMethod.DELETE);
        corsConfig.setAllowedOrigins(Arrays.asList(FRONTEND_ANGULAR_LOCALHOST, FRONTEND_VUE_LOCALHOST, FRONTEND_VUE_LOCALHOST_STAGED, FRONTEND_STAGING));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

}

