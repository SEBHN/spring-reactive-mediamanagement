package de.hhn.mvs.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Component
public class MediaRouter {

    @Bean
    public RouterFunction route(MediaHandler mediaHandler) {
        return RouterFunctions
                .route(GET("/users/{userId}/media/{id}").and(accept(APPLICATION_JSON)), mediaHandler::get)
                .andRoute(GET("/users/{userId}/media").and(accept(APPLICATION_JSON)), mediaHandler::list)
                .andRoute(POST("/users/{userId}/media"), mediaHandler::create);
    }
}
