package de.hhn.mvs.rest;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class MediaHandlerTest {


    @Autowired
    private MediaCrudRepo mediaRepo;

    @Autowired
    private WebTestClient webClient;
    private Media catMedia;
    private Mono<Media> catMediaSave;
    private Media dogMedia;
    private Mono<Media> dogMediaSave;

    @Before
    public void setUp() {
        Tag cats = new Tag("1", "cats");
        Tag doge = new Tag("2", "doge");
        Tag cute = new Tag("3", "cute");
        Tag meme = new Tag("4", "meme");

        catMedia = new MediaImpl(UUID.randomUUID().toString(), "My fabulous cat", "cat.jpg", ".jpg", "", cats, cute);
        dogMedia = new MediaImpl(UUID.randomUUID().toString(), "Such Wow", "doge.jpg", ".jpg", "", doge, meme);
        catMediaSave = mediaRepo.save(catMedia);
        dogMediaSave = mediaRepo.save(dogMedia);
    }

    @After
    public void cleanUp() {
        mediaRepo.deleteAll().block(); // ensure repo is cleaned up
    }


    @Test
    public void getExisting() {
        catMediaSave.block(); // ensure is saved to db
        webClient.get().uri("/users/{userId}/media/{id}", 1, catMedia.getId()).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Media.class)
                .isEqualTo(catMedia);
    }
}
