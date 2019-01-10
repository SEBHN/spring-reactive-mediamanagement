package de.hhn.mvs.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Component
public class FolderRouter {
    @Bean
    public RouterFunction foldersRoute(FolderHandler folderHandler) {
        return RouterFunctions
                .route(PUT("/users/folders/{oldPath}").and(accept(TEXT_PLAIN)), folderHandler::updateFolder);
    }
}
