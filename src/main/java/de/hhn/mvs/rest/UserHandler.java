package de.hhn.mvs.rest;

import de.hhn.mvs.database.UserCrudRepo;
import de.hhn.mvs.model.User;
import de.hhn.mvs.model.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class UserHandler {

    @Autowired
    private UserCrudRepo userRepo;

    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    Mono<ServerResponse> get(ServerRequest request) {
        String userId = request.pathVariable("userId");
        return userRepo.findById(userId)
                .flatMap(user -> {
                    user.setPassword(null);
                    return ok().contentType(APPLICATION_JSON).body(fromObject(user));
                })
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> create(ServerRequest request) {
        Mono<User> userMono = request.bodyToMono(User.class);
        UUID id = UUID.randomUUID();
        return ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(userMono.map(user ->
                                {
                                    UserImpl createdUser = new UserImpl(id.toString(), user.isAdmin(), user.getEmail(),
                                        user.getPassword(), user.getToken(), user.getName(), new ArrayList<>(Arrays.asList("ROLE_USER")));
                                    createdUser.encodePassword();
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

        Mono<User> userMono = request.bodyToMono(User.class);

        return userRepo
                .findById(userId)
                .flatMap(existingMedia -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromPublisher(userMono.map(
                            user -> new UserImpl(userId, user.isAdmin(), user.getEmail(), user.getPassword(),
                                user.getToken(), user.getName(), user.getRoles()))
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

    HandlerFilterFunction<ServerResponse, ServerResponse> illegalStateToBadRequest() {
        return (request, next) -> next.handle(request)
                .onErrorMap(IllegalStateException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

}
