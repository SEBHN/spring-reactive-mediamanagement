package de.hhn.mvs.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Component
public class MediaRouter {

    @Bean
    public RouterFunction<ServerResponse> mediaRoute(MediaHandler mediaHandler) {
        return RouterFunctions
                .route(GET("/users/{userId}/media/{id}").and(accept(APPLICATION_JSON)), mediaHandler::get)
                .andRoute(GET("/users/{userId}/media/{id}/download").and(accept(APPLICATION_OCTET_STREAM)), mediaHandler::download)
                .andRoute(GET("/users/{userId}/media").and(accept(APPLICATION_JSON)), mediaHandler::list)
                .andRoute(POST("/users/{userId}/media").and(accept(APPLICATION_JSON)), mediaHandler::create)
                .andRoute(POST("/users/{userId}/media/{id}/upload").and(accept(MULTIPART_FORM_DATA)), mediaHandler::upload)
                .filter(mediaHandler.illegalStateToBadRequest())
                .andRoute(PUT("/users/{userId}/media/{id}").and(accept(APPLICATION_JSON)), mediaHandler::update)
                .andRoute(DELETE("/users/{userId}/media/{id}").and(accept(ALL)), mediaHandler::delete)
                .andRoute(GET("/users/{userId}/folders/{folderPath}/media").and(accept(APPLICATION_JSON)), mediaHandler::listFolderContent);
    }
}
