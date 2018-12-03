package de.hhn.mvs.rest;

import de.hhn.mvs.database.UserCrudRepo;
import de.hhn.mvs.model.*;
import de.hhn.mvs.model.UserImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class UserHandlerTest {

    @Autowired
    private UserCrudRepo userRepo;
    @Autowired
    private WebTestClient webClient;

    private User testUser;
    private User testUser2;
    private Mono<User> userSave;

    @Before
    public void setUp() {
        testUser = new UserImpl(UUID.randomUUID().toString(), false, "example@domain.tld", "testPassword123", "Token123");
        testUser2 = new UserImpl(UUID.randomUUID().toString(), true, "example2@domain.tld", "testPassword987", "Token987");

        userSave = userRepo.save(testUser);
    }

    @After
    public void cleanUp() {
        userRepo.deleteAll().block();
    }


    @Test
    public void createUser(){
        webClient.post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(testUser))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class)
                .consumeWith(returnedUserResult -> {
                    User returnedUser = returnedUserResult.getResponseBody();
                    assertNotEquals(null, returnedUser);
                    //assertEquals(testUser.getId(), returnedUser.getId());// Vergleich nicht möglich da ID generiert wird
                    assertEquals(testUser.getEmail(), returnedUser.getEmail());
                    assertEquals(testUser.isAdmin(), returnedUser.isAdmin());
                    assertEquals(testUser.getPassword(), returnedUser.getPassword());
                    assertEquals(testUser.getToken(), returnedUser.getToken());
                });
    }

    @Test
    public void getUser(){
        userSave.block();
        webClient.get().uri("/users/{userId}",testUser.getId()).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .isEqualTo(testUser);
    }

    @Test
    public void updateUser(){
        String userId = createUser(testUser2).getResponseBody().getId();
        testUser2 = getUser(userId).getResponseBody();
        assertEquals(userId, testUser2.getId());

        testUser2.setEmail("newExample@domain.tld");
        testUser2.setPassword("Iamasecurepassword");

        webClient.put().uri("/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(testUser2))
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .consumeWith(returnedMediaResult -> {
                    User returnedMedia = returnedMediaResult.getResponseBody();
                    assertNotEquals(null, returnedMedia);
                    assertEquals(testUser2.getId(), returnedMedia.getId());
                    assertEquals(testUser2.isAdmin(), returnedMedia.isAdmin());
                    assertEquals(testUser2.getEmail(), returnedMedia.getEmail());
                    assertEquals(testUser2.getPassword(), returnedMedia.getPassword());
                    assertEquals(testUser2.getToken(), returnedMedia.getToken());
                });
    }

    @Test
    public void deleteUser(){
        String userId = createUser(testUser).getResponseBody().getId();
        webClient.delete().uri("/users/{userId}", userId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void deleteNonExistingUser(){
        webClient.delete().uri("/users/{userId}", 99999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void updateNonExistingUser(){
        webClient.put().uri("/users/{userId}", 99999)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(testUser2))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void getNonExitingUser(){
        webClient.get().uri("/users/{userId}",123456789).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    private EntityExchangeResult<User> createUser(User user) {
        return webClient.post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(user))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class).returnResult();
    }

    private EntityExchangeResult<User> getUser(String userId) {
        return webClient.get().uri("/users/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class).returnResult();
    }
}
