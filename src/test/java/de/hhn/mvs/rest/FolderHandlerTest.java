package de.hhn.mvs.rest;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "junit@hs-heilbronn.de", password = "testingRocks911!", roles = "USER")
public class FolderHandlerTest {

    private static final String ANY_USER_ID = "junit@hs-heilbronn.de";
    private static final String ANY_OTHER_USER_ID = "anotherjunit@hs-heilbronn.de";

    @Autowired
    private MediaCrudRepo mediaRepo;
    @Autowired
    private WebTestClient webClient;

    private Media catMedia;
    private Media cat2MediaInFolder;
    private Media cat3MediaInFolder;
    private Media kittenMediaInFolder;
    private Media dogMedia;
    private Media anotherDog;
    private Mono<Media> catMediaSave;
    private Mono<Media> cat2MediaInFolderMediaSave;
    private Mono<Media> cat3MediaInFolderMediaSave;
    private Mono<Media> kittenMediaInFolderMediaSave;
    private Mono<Media> dogMediaSave;
    private Mono<Media> anotherDogMediaSave;


    @Before
    public void setUp() {
        webClient = webClient.mutateWith(SecurityMockServerConfigurers.csrf());
        catMedia = new MediaImpl(UUID.randomUUID().toString(), "My fabulous cat", "123462345", ".jpg", "/", ANY_USER_ID);
        cat2MediaInFolder = new MediaImpl(UUID.randomUUID().toString(), "Cute cate", "123", ".png", "/catPictures/", ANY_USER_ID);
        cat3MediaInFolder = new MediaImpl(UUID.randomUUID().toString(), "Cute cat", "987", ".png", "/catPictures/", ANY_USER_ID);
        kittenMediaInFolder = new MediaImpl(UUID.randomUUID().toString(), "Cute kitten :)", "852", ".gif", "/kitten/", ANY_USER_ID);
        dogMedia = new MediaImpl(UUID.randomUUID().toString(), "Such Wow", "1337", ".jpg", "/", ANY_USER_ID);
        anotherDog = new MediaImpl(UUID.randomUUID().toString(), "Such fabulous", "1338", ".png", "/", ANY_OTHER_USER_ID);

        catMediaSave = mediaRepo.save(catMedia);
        cat2MediaInFolderMediaSave = mediaRepo.save(cat2MediaInFolder);
        cat3MediaInFolderMediaSave = mediaRepo.save(cat3MediaInFolder);
        kittenMediaInFolderMediaSave = mediaRepo.save(kittenMediaInFolder);
        dogMediaSave = mediaRepo.save(dogMedia);
        anotherDogMediaSave = mediaRepo.save(anotherDog);
    }

    @After
    public void cleanUp() {
        mediaRepo.deleteAll().block(); // ensure repo is cleaned up
    }


    @Test
    public void moveValidFolderPath() {
        //move folder Path /kitten/ into /catPictures/
        Media kitten = kittenMediaInFolderMediaSave.block();
        String oldPath = "/kitten/";//.replace("/", "%2F");
        webClient.put()
                 .uri("/users/folders/{oldPath}", oldPath)
                 .body(BodyInserters.fromObject("/catPictures/kitten/"))
                 .accept(MediaType.TEXT_PLAIN)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBodyList(Media.class)
                 .hasSize(1)
                 .consumeWith(mediaList -> {
                     List<Media> updated = mediaList.getResponseBody();
                     for (Media media : updated) {
                         assertEquals("/catPictures/kitten/", media.getFilePath());
                     }

                     kitten.setFilePath("/catPictures/kitten/");
                     assertEquals(true, updated.contains(kitten));
                 });
    }

    @Test
    public void renameValidFolderPath() {
        //change folder name /catPictures/ to /catPics/
        Media catMedia2 = cat2MediaInFolderMediaSave.block();
        Media catMedia3 = cat3MediaInFolderMediaSave.block();
        String oldPath = "/catPictures/";
        webClient.put()
                 .uri("/users/folders/{oldPath}", oldPath)
                 .body(BodyInserters.fromObject("/catPics/"))
                 .accept(MediaType.TEXT_PLAIN)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBodyList(Media.class)
                 .hasSize(2)
                 .consumeWith(mediaList -> {
                     List<Media> updated = mediaList.getResponseBody();
                     for (Media media : updated) {
                         assertEquals("/catPics/", media.getFilePath());
                     }

                     catMedia2.setFilePath("/catPics/");
                     assertEquals(true, updated.contains(catMedia2));

                     catMedia3.setFilePath("/catPics/");
                     assertEquals(true, updated.contains(catMedia3));
                 });
    }

}
