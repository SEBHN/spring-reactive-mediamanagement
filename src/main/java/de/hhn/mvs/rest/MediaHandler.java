package de.hhn.mvs.rest;

import de.hhn.mvs.MediaCreator;
import de.hhn.mvs.model.Media;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MediaHandler {

    public Mono<ServerResponse> get(ServerRequest request) {
        int id = Integer.parseInt(request.pathVariable("id"));
        Mono<Media> media = Mono.just(MediaCreator.getInstance().getDummyMedia().get(id));
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.APPLICATION_JSON).body(media, Media.class);
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        Flux<Media> medias = Flux.fromIterable(MediaCreator.getInstance().getDummyMedia());
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.APPLICATION_JSON).body(medias, Media.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Media> media = request.bodyToMono(Media.class);
        // TODO: returned saved person of repository with build instead of body // ServerResponse.ok().build(repository.save(media));
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).body(media, Media.class);
    }
}
