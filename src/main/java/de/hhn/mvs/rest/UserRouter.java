package de.hhn.mvs.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Component
public class UserRouter {
    @Bean
    public RouterFunction<ServerResponse> UserRoute(UserHandler userHandler) {
        return RouterFunctions
                .route(GET("/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::get)
                .andRoute(POST("/users").and(accept(APPLICATION_JSON)), userHandler::create)
                .filter(userHandler.illegalStateToBadRequest())
                .andRoute(PUT("/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::update)
                .andRoute(DELETE("/users/{userId}").and(accept(ALL)), userHandler::delete);
    }
}