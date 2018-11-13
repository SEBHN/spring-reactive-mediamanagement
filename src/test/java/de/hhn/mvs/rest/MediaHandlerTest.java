package de.hhn.mvs.rest;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class MediaHandlerTest {


    private static final int ANY_USER_ID = 1;

    @Autowired
    private MediaCrudRepo mediaRepo;
    @Autowired
    private WebTestClient webClient;

    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

    private Media catMedia;
    private Mono<Media> catMediaSave;
    private Media dogMedia;
    private Mono<Media> dogMediaSave;

    @Before
    public void setUp() {
        Tag cats = new Tag("cats");
        Tag doge = new Tag("doge");
        Tag cute = new Tag("cute");
        Tag meme = new Tag("meme");

        catMedia = new MediaImpl(UUID.randomUUID().toString(), "My fabulous cat", "123462345", ".jpg", "", cats, cute);
        dogMedia = new MediaImpl(UUID.randomUUID().toString(), "Such Wow", "1337", ".jpg", "", doge, meme);
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
        webClient.get().uri("/users/{userId}/media/{id}", ANY_USER_ID, catMedia.getId()).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Media.class)
                .isEqualTo(catMedia);
    }

    @Test
    public void getNotExisting() {
        webClient.get().uri("/users/{userId}/media/{id}", ANY_USER_ID, 1234567890).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void list() {
        catMediaSave.block(); // ensure is saved to db
        dogMediaSave.block();

        webClient.get().uri("/users/{userId}/media", ANY_USER_ID).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Media.class)
                .hasSize(2).contains(catMedia, dogMedia);
    }

    @Test
    public void postValidMedia() {
        webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(catMedia))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Media.class)
                .consumeWith(returnedMediaResult -> {
                    Media returnedMedia = returnedMediaResult.getResponseBody();
                    assertNotEquals(null, returnedMedia);
                    assertEquals(catMedia.getName(), returnedMedia.getName());
                    assertEquals(catMedia.getTags(), returnedMedia.getTags());
                    assertEquals(catMedia.getFileExtension(), returnedMedia.getFileExtension());
                    assertEquals(catMedia.getFilePath(), returnedMedia.getFilePath());
                });
    }

    @Test
    //@Ignore // remove @Ignore after #38 is solved
    public void postInvalidMedia() {
        webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(new Tag("something")))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void uploadValidFile() throws Exception {
        String fileName = "uploadTest.txt";
        FileSystemResource resource = new FileSystemResource(folderRule.newFile(fileName));
        MultiValueMap<String, Object> multipartDataMap = new LinkedMultiValueMap<>();
        multipartDataMap.set("file", resource);
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        assertNotEquals(null, mediaId);

        webClient.post()
                .uri("/users/{userId}/media/{id}/upload/", ANY_USER_ID, mediaId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartDataMap))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Media.class)
                .consumeWith(postedMediaResult -> {
                    Media returnedMedia = postedMediaResult.getResponseBody();
                    assertNotEquals(null, returnedMedia);
                    assertEquals(fileName, returnedMedia.getName());
                    assertNotEquals("", returnedMedia.getFileId());
                });
    }

    private EntityExchangeResult<Media> createMedia(Media media) {
        return webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(media))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Media.class).returnResult();
    }

}
