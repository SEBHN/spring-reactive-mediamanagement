package de.hhn.mvs.rest;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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


    private static final String ANY_USER_ID = "1";
    private static final String ANY_OTHER_USER_ID = "2";
    private static final String NOT_EXISTING_MEDIA_ID = "66666";

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
    private Media anotherDog;
    private Mono<Media> anotherDogMediaSave;

    @Before
    public void setUp() {
        Tag cats = new Tag("cats");
        Tag doge = new Tag("doge");
        Tag cute = new Tag("cute");
        Tag meme = new Tag("meme");

        catMedia = new MediaImpl(UUID.randomUUID().toString(), "My fabulous cat", "123462345", ".jpg", "", ANY_USER_ID, cats, cute);
        dogMedia = new MediaImpl(UUID.randomUUID().toString(), "Such Wow", "1337", ".jpg", "", ANY_USER_ID, doge, meme);
        anotherDog = new MediaImpl(UUID.randomUUID().toString(), "Such fabulous", "1338", ".png", "", ANY_OTHER_USER_ID, doge, meme);
        catMediaSave = mediaRepo.save(catMedia);
        dogMediaSave = mediaRepo.save(dogMedia);
        anotherDogMediaSave = mediaRepo.save(anotherDog);
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
    public void getExistingFromAnotherUser() {
        anotherDogMediaSave.block();
        webClient.get().uri("/users/{userId}/media/{id}", ANY_USER_ID, anotherDog.getId()).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void list() {
        catMediaSave.block(); // ensure is saved to db
        dogMediaSave.block();
        anotherDogMediaSave.block();

        webClient.get().uri("/users/{userId}/media", ANY_USER_ID).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Media.class)
                .hasSize(2).contains(catMedia, dogMedia).doesNotContain(anotherDog);
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
                    assertEquals(ANY_USER_ID, returnedMedia.getOwnerId());
                });
    }

    @Test
    public void postInvalidMedia_WithString() {
        webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject("hi:|"))
                .exchange()
                .expectStatus().is4xxClientError();
    }


    @Test
    public void postInvalidMedia() {
        webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(new MediaImpl(null, null, null, null, null, null)))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    public void uploadValidFile() throws Exception {
        FileSystemResource resource = loadFileFromResource();
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
                    assertEquals(resource.getFilename(), returnedMedia.getName());
                    assertNotEquals("", returnedMedia.getFileId());
                });
    }


    @Test
    public void uploadInvalidFile_WithKeyNotFile() throws Exception {
        FileSystemResource resource = loadFileFromResource();
        MultiValueMap<String, Object> multipartDataMap = new LinkedMultiValueMap<>();
        multipartDataMap.set("not_file", resource);
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        assertNotEquals(null, mediaId);

        webClient.post()
                .uri("/users/{userId}/media/{id}/upload/", ANY_USER_ID, mediaId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartDataMap))
                .exchange()
                .expectStatus().isBadRequest();

        multipartDataMap.clear();
    }

    @Test
    public void uploadInvalidFile_WithoutFile() {
        MultiValueMap<String, Object> multipartDataMap = new LinkedMultiValueMap<>();
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        assertNotEquals(null, mediaId);

        webClient.post()
                .uri("/users/{userId}/media/{id}/upload/", ANY_USER_ID, mediaId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartDataMap))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void updateValidMedia() {
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        dogMedia = getMedia(mediaId).getResponseBody();
        assertEquals(mediaId, dogMedia.getId());

        dogMedia.setName("newDogName");
        dogMedia.setFilePath("dog/newDogPath");

        webClient.put().uri("/users/{userId}/media/{id}", ANY_USER_ID, mediaId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(dogMedia))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Media.class)
                .consumeWith(returnedMediaResult -> {
                    Media returnedMedia = returnedMediaResult.getResponseBody();
                    assertNotEquals(null, returnedMedia);
                    assertEquals(dogMedia.getId(), returnedMedia.getId());
                    assertEquals(dogMedia.getName(), returnedMedia.getName());
                    assertEquals(dogMedia.getTags(), returnedMedia.getTags());
                    assertEquals(dogMedia.getFileExtension(), returnedMedia.getFileExtension());
                    assertEquals(dogMedia.getFilePath(), returnedMedia.getFilePath());
                });
    }

    @Test
    public void updateNonExistingMedia() {
        webClient.post().uri("/users/{userId}/media/{id}", ANY_USER_ID, NOT_EXISTING_MEDIA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(dogMedia))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void deleteValidMedia() {
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        webClient.delete().uri("/users/{userId}/media/{id}", ANY_USER_ID, mediaId)
                .exchange()
                .expectStatus().isNoContent();
    }


    @Test
    public void deleteNonExistingMedia() {
        webClient.delete().uri("/users/{userId}/media/{id}", ANY_USER_ID, NOT_EXISTING_MEDIA_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void deleteValidMediaFromOtherUser() {
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        webClient.delete().uri("/users/{userId}/media/{id}", ANY_OTHER_USER_ID, mediaId)
                .exchange()
                .expectStatus().isNotFound();
    }


    private EntityExchangeResult<Media> createMedia(Media media) {
        return webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(media))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Media.class).returnResult();
    }

    private EntityExchangeResult<Media> getMedia(String mediaId) {
        return webClient.get().uri("/users/{userId}/media/{id}/", ANY_USER_ID, mediaId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Media.class).returnResult();
    }

    private FileSystemResource loadFileFromResource() throws Exception {
        String fileName = "uploadTest.txt";
        FileSystemResource resource = new FileSystemResource(folderRule.newFile(fileName));
        return resource;
    }

}
