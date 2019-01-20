package de.hhn.mvs.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Component
public class OktaUserRouter {
    @Bean
    public RouterFunction<ServerResponse> userRoute(OktaHandler oktaHandler) {
        return RouterFunctions
                .route(POST("/users/register").and(accept(APPLICATION_JSON)), oktaHandler::register);
    }
}
