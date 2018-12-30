package de.hhn.mvs.rest;

import de.hhn.mvs.model.OktaUser;
import de.hhn.mvs.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class OktaHandler {

    @Value("${okta.api_key}")
    private String oktaApiKey;



    public Mono<ServerResponse> register(ServerRequest request) {
        Mono<OktaUser> oktaUserMono = request.bodyToMono(User.class).map(user -> OktaUser.create(user));
        Mono<ClientResponse> clientResponseMono = getAPIClient().post().uri("/users?activate=false").body(oktaUserMono, OktaUser.class).exchange();

        return ServerResponseMapper.fromClientResponse(clientResponseMono, String.class);
    }

    private WebClient getAPIClient(){
        return WebClient
                .builder()
                .baseUrl("https://dev-332680.oktapreview.com/api/v1")
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "SSWS " + oktaApiKey)
                .build();
    }
}
