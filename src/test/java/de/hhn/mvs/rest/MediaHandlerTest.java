package de.hhn.mvs.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.FolderElements;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Subfolder;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

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

    private List<Mono<Media>> savedMedia;

    @Before
    public void setUp() {
        Tag cats = new Tag("cats");
        Tag doge = new Tag("doge");
        Tag cute = new Tag("cute");
        Tag meme = new Tag("meme");

        catMedia = new MediaImpl(UUID.randomUUID().toString(), "My fabulous cat", "123462345", ".jpg", "/", ANY_USER_ID, cats, cute);
        cat2MediaInFolder = new MediaImpl(UUID.randomUUID().toString(), "Cute cate", "123", ".png", "/catPictures/", ANY_USER_ID, cute);
        cat3MediaInFolder = new MediaImpl(UUID.randomUUID().toString(), "Cute cat", "987", ".png", "/catPictures/", ANY_USER_ID);
        kittenMediaInFolder = new MediaImpl(UUID.randomUUID().toString(), "Cute kitten :)", "852", ".gif", "/kitten/", ANY_USER_ID, cute);
        dogMedia = new MediaImpl(UUID.randomUUID().toString(), "Such Wow", "1337", ".jpg", "/", ANY_USER_ID, doge, meme);
        anotherDog = new MediaImpl(UUID.randomUUID().toString(), "Such fabulous", "1338", ".png", "/", ANY_OTHER_USER_ID, doge, meme);

        catMediaSave = mediaRepo.save(catMedia);
        cat2MediaInFolderMediaSave = mediaRepo.save(cat2MediaInFolder);
        cat3MediaInFolderMediaSave = mediaRepo.save(cat3MediaInFolder);
        kittenMediaInFolderMediaSave = mediaRepo.save(kittenMediaInFolder);
        dogMediaSave = mediaRepo.save(dogMedia);
        anotherDogMediaSave = mediaRepo.save(anotherDog);

        savedMedia = new ArrayList<>(Arrays.asList(catMediaSave, cat2MediaInFolderMediaSave, cat3MediaInFolderMediaSave, kittenMediaInFolderMediaSave, dogMediaSave, anotherDogMediaSave));
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
        savedMedia.forEach(Mono::block); // ensure every media is saved

        webClient.get().uri("/users/{userId}/media", ANY_USER_ID).accept(MediaType.APPLICATION_JSON)
                 .exchange()
                 .expectStatus().isOk()
                 .expectHeader().contentType(MediaType.APPLICATION_JSON)
                 .expectBodyList(Media.class)
                 .hasSize(5)
                 .contains(catMedia, dogMedia, cat2MediaInFolder, cat3MediaInFolder, kittenMediaInFolder)
                 .doesNotContain(anotherDog);
    }

    @Test
    public void listFolderContentRoot() {
        catMediaSave.block();
        dogMediaSave.block();
        anotherDogMediaSave.block();
        cat2MediaInFolderMediaSave.block();
        cat3MediaInFolderMediaSave.block();
        kittenMediaInFolderMediaSave.block();

        String folder = "/";

        webClient.get().uri("/users/{userId}/folders/{folderPath}/media", ANY_USER_ID, folder)
                 .exchange()
                 .expectStatus().isOk()
                 .expectHeader().contentType(MediaType.APPLICATION_JSON)
                 .expectBody(FolderElements.class)
                 .consumeWith(folderElements -> {
                     List<Subfolder> subFolders = folderElements.getResponseBody().getSubfolders();
                     List<Media> media = folderElements.getResponseBody().getMedia();
                     assertEquals("Should have only 2 SubFolders", 2, subFolders.size());
                     assertEquals("'/' contains only 2 media", 2, media.size());
                     assertEquals(true, media.contains(catMedia));
                     assertEquals(true, media.contains(dogMedia));
                     assertEquals(false, media.contains(anotherDog));
                     assertEquals(false, media.contains(cat2MediaInFolder));
                     assertEquals(false, media.contains(cat3MediaInFolder));
                     assertEquals(false, media.contains(kittenMediaInFolder));
                     assertEquals(true, subFolders.contains(new Subfolder("catPictures")));
                     assertEquals(true, subFolders.contains(new Subfolder("kitten")));
                 });
    }

    @Test
    public void listSubFolderContent() {
        catMediaSave.block(); // ensure is saved to db
        cat2MediaInFolderMediaSave.block();
        cat3MediaInFolderMediaSave.block();
        kittenMediaInFolderMediaSave.block();

        String folder = "/catPictures";

        webClient.get().uri("/users/{userId}/folders/{folderPath}/media", ANY_USER_ID, folder)
                 .exchange()
                 .expectStatus().isOk()
                 .expectHeader().contentType(MediaType.APPLICATION_JSON)
                 .expectBody(FolderElements.class)
                 .consumeWith(folderElements -> {
                     List<Subfolder> subFolders = folderElements.getResponseBody().getSubfolders();
                     List<Media> media = folderElements.getResponseBody().getMedia();
                     assertEquals(0, subFolders.size());
                     assertEquals(2, media.size());
                     assertEquals(true, media.contains(cat2MediaInFolder));
                     assertEquals(true, media.contains(cat3MediaInFolder));
                 });
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
        dogMedia.setFilePath("/dog/newDogPath/");

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
        webClient.put().uri("/users/{userId}/media/{id}", ANY_USER_ID, NOT_EXISTING_MEDIA_ID)
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

    @Test
    public void getMediaWithTagsFromSubfolder(){
        //TODO
//        webClient.post()
//                .uri("/users/{userId}/folders/{folderPath}/", ANY_USER_ID, "/")
//                .body(new ArrayList<Tag>(new Tag("a")))
//                ;
        fail();
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
        return new FileSystemResource(folderRule.newFile(fileName));
    }
}
