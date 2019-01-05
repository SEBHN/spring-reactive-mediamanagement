package de.hhn.mvs.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hhn.mvs.config.OktaConfig;
import de.hhn.mvs.model.User;
import de.hhn.mvs.model.UserImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "junit@hs-heilbronn.de", password = "testingRocks911!")
public class OktaHandlerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private OktaConfig config;

    private User user;
    private String userId;

    @Before
    public void setUp() {
        String randomEmail = "oktaHandler" + new Random().nextInt() + "test@junit.org";
        user = new UserImpl("anId", false, randomEmail, "superSecret123!", "", "OktaHandlerTest");
    }

    @After
    public void tearDown() {
        deleteUser();
    }

    @Test
    public void registerUser() {
        webClient.post()
                .uri("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(user))
                .exchange().expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(jsonResponseResult -> {
                    String jsonResponse = jsonResponseResult.getResponseBody();
                    assertNotEquals(null, jsonResponse);
                    userId = getId(jsonResponse);
                    assertNotEquals("", userId);
                });
    }

    private String getId(String jsonResponse) {
        try {
            Map<String, String> jsonToMap = new ObjectMapper().readValue(jsonResponse, new TypeReference<HashMap<String, Object>>() {
            });
            return jsonToMap.get("id");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return "";
    }

    private void deleteUser() {
        deactivateUser();
        Mono<ClientResponse> clientResponseMono = getAPIClient().delete().uri("/users/" + userId).exchange();
        HttpStatus httpStatus = clientResponseMono.map(ClientResponse::statusCode).block();
        assertEquals(HttpStatus.NO_CONTENT, httpStatus);
    }

    private void deactivateUser() {
        Mono<ClientResponse> clientResponseMono = getAPIClient().post().uri("/users/" + userId + "/lifecycle/deactivate").exchange();
        HttpStatus httpStatus = clientResponseMono.map(ClientResponse::statusCode).block();
        assertEquals(HttpStatus.OK, httpStatus);
    }

    private WebClient getAPIClient() {
        return WebClient
                .builder()
                .baseUrl(config.getApiUrl())
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "SSWS " + config.getApiKey())
                .build();
    }
}
