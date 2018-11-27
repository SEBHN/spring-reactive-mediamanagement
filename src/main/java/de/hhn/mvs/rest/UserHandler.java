package de.hhn.mvs.rest;

import de.hhn.mvs.database.UserCrudRepo;
import de.hhn.mvs.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class UserHandler {

    @Autowired
    private UserCrudRepo userRepo;

    Mono<ServerResponse> get(ServerRequest request) {
        String userId = request.pathVariable("userId");
        return userRepo.findById(userId)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).body(fromObject(user)))
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> create(ServerRequest request) {
        Mono<User> user = request.bodyToMono(User.class);
        UUID id = UUID.randomUUID();
        return ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        fromPublisher(
                                user.map(p ->
                                {
                                    User createdUser = p.copy();
                                    return createdUser;
                                }).onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                                        .onErrorMap(DecodingException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                                        .flatMap(userRepo::save), User.class)

                )
                .onErrorMap(RuntimeException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    Mono<ServerResponse> update(ServerRequest request) {
        String userId = request.pathVariable("userId");
        if (userId == null || userId.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("userId must not be empty"));
        }

        Mono<User> user = request.bodyToMono(User.class);

        return userRepo
                .findById(userId)
                .flatMap(existingMedia -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                fromPublisher(
                                        user.map(p ->
                                                p.copy())
                                                .flatMap(userRepo::save), User.class)))
                .switchIfEmpty(notFound().build());
    }


    Mono<ServerResponse> delete(ServerRequest request) {
        String userId = request.pathVariable("userId");
        if (userId == null || userId.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("userId must not be empty"));
        }

        return userRepo
                .findById(userId)
                .flatMap(existingUser -> noContent().build(userRepo.delete(existingUser)))
                .switchIfEmpty(notFound().build());
    }

    //Mono<ServerResponse> auth(ServerRequest request){
    //    String userId = request.pathVariable("userId");
    //}

    HandlerFilterFunction<ServerResponse, ServerResponse> illegalStateToBadRequest() {
        return (request, next) -> next.handle(request)
                .onErrorMap(IllegalStateException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

}
