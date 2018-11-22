package de.hhn.mvs.rest;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.FolderElements;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Subfolder;
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class MediaHandler {

    private static final String SLASH = "/";

    @Autowired
    private MediaCrudRepo mediaRepo;
    @Autowired
    private MongoDbFactory mongoDbFactory;

    private final GridFsTemplate gridFsTemplate;


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
        String folderPath = request.queryParam("folder").orElse("/");
        String userId = request.pathVariable("userId");
        String parsedfolderPath = parseFolderPathFormat(folderPath);

        Mono<FolderElements> folderElementsMono =
                mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(userId, parsedfolderPath)
                        .collectList()
                        .map(media -> {
                            Set<Subfolder> subfolders = new HashSet<>();
                            List<Media> mediaInFolder = new ArrayList<>();

                            for (Media medium : media) {
                                //add media in folder to media list
                                if (medium.getFilePath().equals(parsedfolderPath)) {
                                    mediaInFolder.add(medium);
                                }
                                //extract next folder after requested one and add it to subfolders set
                                else if (medium.getFilePath().length() > parsedfolderPath.length()) {
                                    String folder = medium.getFilePath();
                                    String shortened = folder.replaceFirst(parsedfolderPath, "");
                                    int indexOfNextSlash = shortened.indexOf(SLASH);
                                    if (indexOfNextSlash > 0) {
                                        String subfolderName = shortened.substring(0, indexOfNextSlash);
                                        subfolders.add(new Subfolder(subfolderName));
                                    }
                                }
                            }
                            //filter duplicates
                            ArrayList<Subfolder> filteredSubfolders = new ArrayList<>(subfolders);
                            return new FolderElements(filteredSubfolders, mediaInFolder);

                        });
                        //TODO: error handling. e.g. Folder does not exist -> empty return


        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(folderElementsMono, FolderElements.class));
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

    Mono<ServerResponse> updateFolder(ServerRequest request) {
        String userId = request.pathVariable("userId");
        String oldPath = request.pathVariable("oldPath");
        Mono<String> newPath = request.bodyToMono(String.class);

        Flux<String> paths = Flux.from(newPath);
        Flux<Media> renamedMedia1 = paths
                .flatMap(newPathString ->
                {
                    Flux<Media> mediaFlux = mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(userId, parseFolderPathFormat(oldPath));
                    return mediaFlux.flatMap(m ->
                    {
                        String pathOfOldMedium = m.getFilePath();
                        String pathOfReanamedMedium = pathOfOldMedium
                                .replaceFirst(parseFolderPathFormat(oldPath), parseFolderPathFormat(newPathString));
                        m.setFilePath(pathOfReanamedMedium);
                        return mediaRepo.save(m);
                    })
                            .onErrorMap(error ->
                                    new Exception("Error in Mapping to flux " + error.getMessage()));
                });


        return ok().body(renamedMedia1
                        .onErrorMap(Exception.class, e ->
                                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()))
                , Media.class);
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
     * parse to required format: /foo/bar/
     *
     * @param folderPath
     * @return path in correct format
     */
    private String parseFolderPathFormat(String folderPath) {
        if (folderPath == null || folderPath.isEmpty())
            return SLASH;
        String newPath = folderPath;
        if (!folderPath.endsWith(SLASH))
            newPath = newPath + SLASH;
        if (newPath.length() > 1 && !newPath.startsWith(SLASH))
            newPath = SLASH + newPath;
        return newPath;
    }
}
