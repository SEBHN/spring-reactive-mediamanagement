package de.hhn.mvs.rest;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = LocalHostM)
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
    Tag cats;
    Tag doge;
    Tag cute;
    Tag meme;

    private List<Mono<Media>> savedMedia;

    @Autowired
    private GridFsTemplate gridFsTemplate;


    @Before
    public void setUp() {
        cats = new Tag("cats");
        doge = new Tag("doge");
        cute = new Tag("cute");
        meme = new Tag("meme");

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
        gridFsTemplate.delete(new Query());
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

                    GridFSFindIterable filesInDb = gridFsTemplate.find(new Query());
                    AtomicInteger integer = new AtomicInteger();
                    filesInDb.forEach((Consumer<? super GridFSFile>) file -> {
                        integer.addAndGet(1);
                    });
                    assertEquals(1, integer.get());
                });
    }

    @Test
    public void deleteValidMediaWithFile() throws Exception {
        MultiValueMap<String, Object> multipartDataMap = loadSampleFileIntoMap();
        String mediaId = createMedia(dogMedia).getResponseBody().getId();
        Media uploadedMedia = uploadMedia(mediaId, multipartDataMap).getResponseBody();
        assertNotEquals(null, uploadedMedia.getId());

        GridFSFile uploaded = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedMedia.getFileId())));
        assertNotNull(uploaded);

        webClient.delete()
                .uri("/users/{userId}/media/{id}/", ANY_USER_ID, uploadedMedia.getId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(empty -> {
                    GridFSFile deleted = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedMedia.getFileId())));
                    assertNull(deleted);
                });
    }


    @Test
    public void deleteValidFolderWithFiles() throws Exception {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        assertNotEquals(null, cat2InFolder.getId()); //check for one

        MultiValueMap<String, Object> multipartDataMap = loadSampleFileIntoMap();

        Media uploadedCatMedia = uploadMedia(cat.getId(), multipartDataMap).getResponseBody();
        Media uploadedCat2Media = uploadMedia(cat2InFolder.getId(), multipartDataMap).getResponseBody();
        Media uploadedCat3Media = uploadMedia(cat3InFolder.getId(), multipartDataMap).getResponseBody();
        assertNotEquals(null, cat2InFolder.getId()); //check for one

        GridFSFile uploadedCatFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedCatMedia.getFileId())));
        assertNotNull(uploadedCatFile);
        GridFSFile uploadedCat2File = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedCat2Media.getFileId())));
        assertNotNull(uploadedCat2File);
        GridFSFile uploadedCat3File = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedCat3Media.getFileId())));
        assertNotNull(uploadedCat3File);


        webClient.delete().uri("/users/{userId}/folders/{folderPath}", ANY_USER_ID, "catPictures")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(empty -> {
                    GridFSFile catFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedCatMedia.getFileId())));
                    assertNotNull(uploadedCatFile);
                    GridFSFile cat2File = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedCat2Media.getFileId())));
                    assertNull(cat2File);
                    GridFSFile cat3File = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(uploadedCat3Media.getFileId())));
                    assertNull(cat3File);
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
    public void getMediaWithMultipleTagsFromRoot() {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        Media kitten = kittenMediaInFolderMediaSave.block();
        Media dog = dogMediaSave.block();

        String folder = "/";

        //multiple tags
        webClient.get()
                .uri("/users/{userId}/folders/{folderPath}/taggedMedia?tag={tag1}&tag={tag2}"
                        , ANY_USER_ID, folder, cats.getName(), cute.getName())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Media.class)
                .hasSize(1)
                .contains(cat)
                .doesNotContain(cat2InFolder, cat3InFolder, kitten, dog);

    }


    @Test
    public void getMediaWithOneTagFromRoot() {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        Media kitten = kittenMediaInFolderMediaSave.block();
        Media dog = dogMediaSave.block();

        String folder = "/";

        webClient.get().uri("users/{userId}/folders/{folderPath}/taggedMedia?tag={tag1}", ANY_USER_ID, "/", cute.getName())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Media.class)
                .hasSize(3)
                .contains(cat, cat2InFolder, kitten)
                .doesNotContain(cat3InFolder, dog);
    }

    @Test
    public void getMediaWithOneTagFromRoot_upperCase() {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        Media kitten = kittenMediaInFolderMediaSave.block();
        Media dog = dogMediaSave.block();

        String folder = "/";

        webClient.get().uri("users/{userId}/folders/{folderPath}/taggedMedia?tag={tag1}", ANY_USER_ID, "/", new Tag("CUTE").getName())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Media.class)
                .hasSize(3)
                .contains(cat, cat2InFolder, kitten)
                .doesNotContain(cat3InFolder, dog);
    }


    @Test
    public void getMediaWithOneTagFromSubfolder() {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        Media kitten = kittenMediaInFolderMediaSave.block();
        Media dog = dogMediaSave.block();

        String folder = "/catPictures";
        //1 tag, search only in folder
        webClient.get()
                .uri("/users/{userId}/folders/{folderPath}/taggedMedia?tag={tag1}"
                        , ANY_USER_ID, folder, cute.getName())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Media.class)
                .hasSize(1)
                .contains(cat2InFolder)
                .doesNotContain(cat, kitten, cat3InFolder, dog);

    }


    @Test
    public void getMediaWithNoTags() {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        Media kitten = kittenMediaInFolderMediaSave.block();
        Media dog = dogMediaSave.block();

        String folder = "/";
        //not existing tag
        webClient.get()
                .uri("/users/{userId}/folders/{folderPath}/taggedMedia?tag={tag1}"
                        , ANY_USER_ID, folder, "notexisting")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Media.class)
                .hasSize(0);

    }

    @Test
    public void deleteFolder() {
        Media cat = catMediaSave.block();
        Media cat2InFolder = cat2MediaInFolderMediaSave.block();
        Media cat3InFolder = cat3MediaInFolderMediaSave.block();
        Media kitten = kittenMediaInFolderMediaSave.block();
        Media dog = dogMediaSave.block();

        webClient.delete().uri("/users/{userId}/folders/{folderPath}", ANY_USER_ID, "catPictures")
                .exchange()
                .expectStatus().isNoContent();

        webClient.get().uri("/users/{userId}/media", ANY_USER_ID).accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Media.class)
                .hasSize(3)
                .contains(cat, kitten, dog)
                .doesNotContain(cat2InFolder, cat3InFolder);
    }

    private MultiValueMap<String, Object> loadSampleFileIntoMap() throws Exception {
        FileSystemResource resource = loadFileFromResource();
        MultiValueMap<String, Object> multipartDataMap = new LinkedMultiValueMap<>();
        multipartDataMap.set("file", resource);
        return multipartDataMap;
    }

    private EntityExchangeResult<Media> createMedia(Media media) {
        return webClient.post().uri("/users/{userId}/media", ANY_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(media))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Media.class).returnResult();
    }

    private EntityExchangeResult<Media> uploadMedia(String mediaId, MultiValueMap<String, Object> multipartDataMap) {
        return webClient.post()
                .uri("/users/{userId}/media/{id}/upload/", ANY_USER_ID, mediaId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartDataMap))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Media.class).returnResult();
    }

    private EntityExchangeResult<Media> getMedia(String mediaId) {
        return webClient.get().uri("/users/{userId}/media/{id}/", ANY_USER_ID, mediaId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Media.class).returnResult();
    }

    private FileSystemResource loadFileFromResource() throws Exception {
        return new FileSystemResource(ResourceUtils.getFile("classpath:metadatasamplefiles/" + "sample.jpg"));
    }
}
