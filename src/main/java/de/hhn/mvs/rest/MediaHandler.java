package de.hhn.mvs.rest;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.result.DeleteResult;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.database.MediaTemplateOperations;
import de.hhn.mvs.model.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static de.hhn.mvs.rest.FolderUtils.SLASH;
import static de.hhn.mvs.rest.FolderUtils.parseFolderPathFormat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class MediaHandler {

    @Autowired
    private MediaCrudRepo mediaRepo;
    @Autowired
    private MongoDbFactory mongoDbFactory;

    private final GridFsTemplate gridFsTemplate;

    @Autowired
    private MediaTemplateOperations mediaTemplateOps;

    @Autowired
    public MediaHandler(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    Mono<ServerResponse> get(ServerRequest request) {
        String mediaId = request.pathVariable("id");
        String userId = request.pathVariable("userId");
        return mediaRepo.findByIdAndOwnerId(mediaId, userId)
                .flatMap(media -> ok().contentType(APPLICATION_JSON).body(fromObject(media)))
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> list(ServerRequest request) {
        String userId = request.pathVariable("userId");

        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(mediaRepo.findAllByOwnerId(userId), Media.class);
    }

    Mono<ServerResponse> listFolderContent(ServerRequest request) {
        String folderPath = request.pathVariable("folderPath");
        String userId = request.pathVariable("userId");
        String parsedfolderPath = parseFolderPathFormat(folderPath);

        Mono<List<Media>> monoMedias = mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(userId, parsedfolderPath)
                .collectList();
        Mono<FolderElements> folderElementsMono =
                monoMedias
                        .map(media -> {
                            Set<Subfolder> subfolders = new HashSet<>();
                            List<Media> mediaInFolder = new ArrayList<>();

                            for (Media medium : media) {
                                String filePath = medium.getFilePath();
                                if (filePath.equals(parsedfolderPath)) {
                                    //add media in folder to media list
                                    mediaInFolder.add(medium);
                                } else if (filePath.length() > parsedfolderPath.length()) {
                                    //extract next folder after requested one and add it to subfolders set
                                    getSubFolderName(parsedfolderPath, filePath).ifPresent(subfolders::add);
                                }
                            }
                            //filter duplicates
                            List<Subfolder> filteredSubfolders = new ArrayList<>(subfolders);
                            return new FolderElements(filteredSubfolders, mediaInFolder);
                        });
        //TODO: error handling. e.g. Folder does not exist -> empty return
        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(folderElementsMono, FolderElements.class));
    }

    Mono<ServerResponse> listTaggedMedia(ServerRequest request) {
        String folderPath = request.pathVariable("folderPath");
        String parsedFolderPath = parseFolderPathFormat(folderPath);
        String userId = request.pathVariable("userId");
        List<Tag> tagList = new ArrayList<>();
        MultiValueMap<String, String> params = request.queryParams();

        params.forEach((name, values) -> {
            if (name.equals("tag")) {
                tagList.addAll(values.stream().map(Tag::new).collect(Collectors.toList()));
            }
        });

        Flux<Media> media;
        if (tagList.isEmpty())
            media = mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(userId, parsedFolderPath);
        else {
            String preparedRegex = "^" + parsedFolderPath;
            media = mediaTemplateOps.findAllByOwnerIdAndFilePathRegexAndTagsContainingAll_notCaseSensitive(userId, preparedRegex, tagList);
        }
        return ok().contentType(APPLICATION_JSON).body(fromPublisher(media, Media.class));
    }


    Mono<ServerResponse> create(ServerRequest request) {
        Mono<Media> media = request.bodyToMono(Media.class);
        UUID id = UUID.randomUUID();
        return ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        fromPublisher(
                                media.map(p ->
                                {
                                    String userId = request.pathVariable("userId");
                                    MediaImpl createdMedia = new MediaImpl(id.toString(), p.getName(),
                                            p.getFileId(), p.getFileExtension(), parseFolderPathFormat(p.getFilePath()), userId, p.getTags());
                                    createdMedia.validate();
                                    return createdMedia;
                                }).onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                                        .onErrorMap(DecodingException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                                        .flatMap(mediaRepo::save), Media.class)

                )
                .onErrorMap(RuntimeException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    Mono<ServerResponse> download(ServerRequest request) {
        String id = request.pathVariable("id");
        String userId = request.pathVariable("userId");
        return mediaRepo
                .findByIdAndOwnerId(id, userId)
                .flatMap(existingMedia -> {
                    GridFSFile gridFsfile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(existingMedia.getFileId())));
                    if (gridFsfile == null) {
                        return notFound().build();
                    }

                    Resource rs = new GridFsResource(gridFsfile, getGridFsBucket().openDownloadStream(gridFsfile.getObjectId()));
                    return ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fromObject(rs));
                })
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        String userId = request.pathVariable("userId");
        String fileKey = "file";

        return request.body(BodyExtractors.toMultipartData())
                .flatMap(parts -> {

                    Map<String, Part> parameterFileMap = parts.toSingleValueMap();
                    if (!parameterFileMap.containsKey(fileKey)) {
                        return ServerResponse.status(HttpStatus.BAD_REQUEST).body(fromObject("File for upload required. Key name must be '" + fileKey + "'."));
                    }

                    FilePart part = (FilePart) parameterFileMap.get(fileKey);

                    ObjectId fileId;
                    try {
                        Path upload = Files.createTempFile("mvs_", "_upload");
                        part.transferTo(upload.toFile());
                        fileId = gridFsTemplate.store(Files.newInputStream(upload), part.filename());
                    } catch (IOException e) {
                        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fromObject(e.getMessage()));
                    }

                    String fileName = part.filename();
                    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
                    String fileIdString = fileId.toString();

                    Mono<Media> media = mediaRepo.findByIdAndOwnerId(id, userId);

                    return ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(
                                    fromPublisher(
                                            media.map(p -> new MediaImpl(p.getId(), fileName,
                                                    fileIdString, fileExtension, p.getFilePath(), userId, p.getTags()))
                                                    .flatMap(mediaRepo::save), Media.class));
                });
    }

    Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        String userId = request.pathVariable("userId");
        if (id == null || id.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("Id must not be empty"));
        } else if (userId == null || userId.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("userId must not be empty"));
        }

        Mono<Media> media = request.bodyToMono(Media.class);

        return mediaRepo
                .findByIdAndOwnerId(id, userId)
                .flatMap(existingMedia -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                fromPublisher(
                                        media.map(p ->
                                                new MediaImpl(id, p.getName(),
                                                        p.getFileId(), p.getFileExtension(), parseFolderPathFormat(p.getFilePath()), userId, p.getTags()))
                                                .flatMap(mediaRepo::save), Media.class)))
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        String userId = request.pathVariable("userId");
        if (id == null || id.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("Id must not be empty"));
        } else if (userId == null || userId.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("userId must not be empty"));
        }

        return mediaRepo
                .findByIdAndOwnerId(id, userId)
                .flatMap(existingMedia -> noContent().build(mediaRepo.delete(existingMedia)))
                .switchIfEmpty(notFound().build());
    }


    Mono<ServerResponse> deleteFolder(ServerRequest request) {
        String userId = request.pathVariable("userId");
        String filePath = request.pathVariable("folderPath");

        return mediaTemplateOps.deleteAllByOwnerIdAndFilePathStartingWith(userId, parseFolderPathFormat(filePath))
                .flatMap(m -> {
                    if (m.getDeletedCount() == 0)
                        return notFound().build();
                    else
                        return noContent().build();
                });
    }

    /**
     * for error handling see: https://stackoverflow.com/questions/48711872/handling-exceptions-and-returning-proper-http-code-with-webflux
     */
    HandlerFilterFunction<ServerResponse, ServerResponse> illegalStateToBadRequest() {
        return (request, next) -> next.handle(request)
                .onErrorMap(IllegalStateException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    private GridFSBucket getGridFsBucket() {
        MongoDatabase db = mongoDbFactory.getDb();
        return GridFSBuckets.create(db);
    }

    /**
     * Checks from two related Folders if the possibleSubFolderfilePath is a subFolder of it and retunrs it
     *
     * @param sourcePath                - /foo/bar
     * @param possibleSubFolderfilePath /foo/bar/subfolder
     * @return Optional<SubFolder> with subFolder
     */
    private Optional<Subfolder> getSubFolderName(String sourcePath, String possibleSubFolderfilePath) {
        Optional<Subfolder> subfolderOptional;
        String folder = possibleSubFolderfilePath;
        String shortened = folder.replaceFirst(sourcePath, "");
        int indexOfNextSlash = shortened.indexOf(SLASH);
        if (indexOfNextSlash > 0) {
            String subfolderName = shortened.substring(0, indexOfNextSlash);
            subfolderOptional = Optional.of(new Subfolder(subfolderName));
        } else {
            subfolderOptional = Optional.empty();
        }
        return subfolderOptional;
    }
}
