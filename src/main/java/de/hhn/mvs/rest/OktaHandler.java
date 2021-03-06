package de.hhn.mvs.rest;

import de.hhn.mvs.config.OktaConfig;
import de.hhn.mvs.model.OktaUser;
import de.hhn.mvs.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static javax.ws.rs.core.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class OktaHandler {

    private OktaConfig config;

    @Autowired
    public OktaHandler(OktaConfig config) {
        this.config = config;
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        Mono<OktaUser> oktaUserMono = request.bodyToMono(User.class).map(user -> OktaUser.create(user, config.getUserGroupIds()));
        Mono<ClientResponse> clientResponseMono = getAPIClient().post().uri("/users").body(oktaUserMono, OktaUser.class).exchange();

        return ServerResponseMapper.fromClientResponse(clientResponseMono, String.class);
    }

    private WebClient getAPIClient(){
        return WebClient
                .builder()
                .baseUrl(config.getApiUrl())
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .defaultHeader(AUTHORIZATION, "SSWS " + config.getApiKey())
                .build();
    }
}
