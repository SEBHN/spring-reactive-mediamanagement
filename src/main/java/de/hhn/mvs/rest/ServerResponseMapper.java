package de.hhn.mvs.rest;

import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Im responsible for transforming an {@link ClientResponse} to an {@link ServerResponse}
 */
final class ServerResponseMapper {

    static <T> Mono<ServerResponse> fromClientResponse(Mono<ClientResponse> clientResponseMono, Class<T> clazz){
        return clientResponseMono.flatMap(clientResponse -> fromClientResponse(clientResponse, clazz));
    }

    static <T> Mono<ServerResponse> fromClientResponse(ClientResponse clientResponse, Class<T> clazz){
        return ServerResponse.status(clientResponse.statusCode())
                             .headers(headerConsumer -> clientResponse.headers().asHttpHeaders().forEach(headerConsumer::addAll))
                             .body(clientResponse.bodyToMono(clazz), clazz);
    }
}
