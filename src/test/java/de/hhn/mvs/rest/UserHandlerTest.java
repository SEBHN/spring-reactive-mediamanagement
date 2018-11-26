package de.hhn.mvs.rest;

import de.hhn.mvs.database.UserCrudRepo;
import de.hhn.mvs.model.User;
import de.hhn.mvs.model.UserImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserHandlerTest {

    @Autowired
    private UserCrudRepo userRepo;
    @Autowired
    private WebTestClient webClient;

    private User testUser;
    private Mono<User> userSave;

    @Before
    public void setUp() {
        testUser = new UserImpl(UUID.randomUUID().toString(), false, "example@domain.tld", "testPassword123", "Token123");

        userSave = userRepo.save(testUser);
    }

    @After
    public void cleanUp() {
        userRepo.deleteAll().block();
    }


    @Test
    public void createUser(){

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

    }

    @Test
    public void deleteUser(){

    }

    @Test
    public void deleteNonExistingUser(){

    }

    @Test
    public void updateNonExistingUser(){

    }

    @Test
    public void getNonExitingUser(){

    }
}
