package de.hhn.mvs.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Component
public class UserRouter {
    @Bean
    public RouterFunction<ServerResponse> userRoute(UserHandler userHandler, OktaHandler oktaHandler) {
        return RouterFunctions
                .route(GET("/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::get)
                .andRoute(POST("/users/register").and(accept(APPLICATION_JSON)), oktaHandler::register)
                .andRoute(POST("/users").and(accept(APPLICATION_JSON)), userHandler::create)
                .filter(userHandler.illegalStateToBadRequest())
                .andRoute(PUT("/users/{userId}").and(accept(APPLICATION_JSON)), userHandler::update)
                .andRoute(DELETE("/users/{userId}").and(accept(ALL)), userHandler::delete);
    }
}