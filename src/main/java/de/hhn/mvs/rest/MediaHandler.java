package de.hhn.mvs.rest;

import de.hhn.mvs.MediaCreator;
import de.hhn.mvs.model.Media;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class MediaHandler {

    public Mono<ServerResponse> get(ServerRequest request) {
        int id = Integer.parseInt(request.pathVariable("id"));

        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.APPLICATION_JSON).body(Mono.just(MediaCreator.getInstance().getDummyMedia().get(id)), Media.class);
    }
}
