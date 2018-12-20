package de.hhn.mvs.rest;

import de.hhn.mvs.database.UserCrudRepo;
import de.hhn.mvs.model.User;
import de.hhn.mvs.model.UserImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "example@domain.tld", password = "testPassword123", roles = "USER")
public class UserHandlerTest {

    @Autowired
    ApplicationContext context;
    @Autowired
    private UserCrudRepo userRepo;
    @Autowired
    private WebTestClient webClient;


    private Base64.Encoder encoder = Base64.getEncoder();

    private User testUser;
    private User testUser2;
    private Mono<User> userSave;

    @Before
    public void setUp() {
        userRepo.deleteAll().block();

        PasswordEncoder enc = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        webClient = WebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                //.defaultHeaders( h-> h.add("Basic", "ZXhhbXBsZUBkb21haW4udGxkOnRlc3RQYXNzd29yZDEyMw=="))
                //.filter(basicAuthentication())
                .build();
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        testUser = new UserImpl(UUID.randomUUID().toString(),  "example@domain.tld", enc.encode("testPassword123"),  roles);
        testUser2 = new UserImpl(UUID.randomUUID().toString(),  "example2@domain.tld", enc.encode("testPassword987"),  roles);

        userSave = userRepo.save(testUser);
    }



    @Test
    public void connectivity(){
        userSave.block();
        User tmp = userRepo.findByEmail("example@domain.tld").block();
        System.out.println(tmp.getEmail());
        webClient.get().uri("/").exchange().expectStatus().isOk();
    }

    @Test
    public void createUser(){

        webClient.post().uri("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(testUser2))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(User.class)
            .consumeWith(returnedUserResult -> {
                User returnedUser = returnedUserResult.getResponseBody();
                assertNotEquals(null, returnedUser);
                assertEquals(testUser2.getEmail(), returnedUser.getEmail());

                //testUser2.hashPassword();
                //assertEquals(testUser2.getPassword(), returnedUser.getPassword());

                // TODO: check passwords against each other

            });
    }

    @Test
    public void getUser(){
        userSave.block();

        User checkUser = userRepo.findById(testUser.getId()).block();

        webClient.get().uri("/users/{userId}",testUser.getId()).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(User.class)
                .consumeWith(returnedUserResult -> {
                    User returnedUser = returnedUserResult.getResponseBody();
                    assertNotEquals(null, returnedUser);
                    assertEquals(checkUser.getId(), returnedUser.getId());
                    assertEquals(checkUser.getEmail(), returnedUser.getEmail());
                });
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

                .consumeWith(returnedUserResult -> {
                    User returnedUser = returnedUserResult.getResponseBody();
                    assertNotEquals(null, returnedUser);
                    assertEquals(testUser2.getId(), returnedUser.getId());
                    assertEquals(testUser2.getEmail(), returnedUser.getEmail());


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
