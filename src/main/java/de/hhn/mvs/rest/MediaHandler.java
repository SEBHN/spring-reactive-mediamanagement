package de.hhn.mvs.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.database.MediaTemplateOperations;
import de.hhn.mvs.metadata.MetadataParser;
import de.hhn.mvs.model.FolderElements;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Subfolder;
import de.hhn.mvs.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
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

import static de.hhn.mvs.rest.FolderUtils.SLASH;
import static de.hhn.mvs.rest.FolderUtils.parseFolderPathFormat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class MediaHandler {

    @Autowired
    private MediaCrudRepo mediaRepo;
    @Autowired
    private MongoDbFactory mongoDbFactory;

    private final GridFsTemplate gridFsTemplate;

    @Autowired
    private MediaTemplateOperations mediaTemplateOps;

    public MediaHandler(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    Mono<ServerResponse> get(ServerRequest request) {
        String mediaId = request.pathVariable("id");
        return request.principal()
                .map(Principal::getName)
                .flatMap(name -> mediaRepo.findByIdAndOwnerId(mediaId, name)
                        .flatMap(media -> ok().contentType(APPLICATION_JSON).body(fromObject(media)))
                        .switchIfEmpty(notFound().build()));
    }

    Mono<ServerResponse> list(ServerRequest request) {
        return request.principal()
                .map(Principal::getName)
                .flatMap(name -> ok().contentType(MediaType.APPLICATION_JSON)
                        .body(mediaRepo.findAllByOwnerId(name), Media.class)
                        .switchIfEmpty(notFound().build()));
    }

    Mono<ServerResponse> listFolderContent(ServerRequest request) {
        String folderPath = request.pathVariable("folderPath");
        String parsedfolderPath = parseFolderPathFormat(folderPath);

        Mono<List<Media>> monoMedias = request.principal()
                .map(Principal::getName)
                .flatMap(name -> mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(name, parsedfolderPath)
                        .collectList());
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
        Mono<String> principalNameMono = request.principal().map(Principal::getName);
        List<Tag> tagList = new ArrayList<>();
        MultiValueMap<String, String> params = request.queryParams();

        params.forEach((name, values) -> {
            if (name.equals("tag")) {
                tagList.addAll(values.stream().map(Tag::new).collect(Collectors.toList()));
            }
        });

        Flux<Media> media;
        if (tagList.isEmpty())
            media = principalNameMono.flatMapMany(name -> mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(name, parsedFolderPath));
        else {
            String preparedRegex = "^" + parsedFolderPath;
            media = principalNameMono.flatMapMany(name -> mediaTemplateOps.findAllByOwnerIdAndFilePathRegexAndTagsContainingAll_notCaseSensitive(name, preparedRegex, tagList));
        }
        return ok().contentType(APPLICATION_JSON).body(fromPublisher(media, Media.class));
    }

    Mono<ServerResponse> create(ServerRequest request) {
        Mono<Media> mediaMono = request.bodyToMono(Media.class);
        UUID id = UUID.randomUUID();

        Mono<String> principalNameMono = request.principal().map(Principal::getName);
        Mono<MediaImpl> createdMediaMono = principalNameMono.flatMap(name -> mediaMono.map(media -> createValidatedMediaCopy(id, name, media)));

        return ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(createdMediaMono
                        .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                        .onErrorMap(DecodingException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                        .flatMap(mediaRepo::save), Media.class)
                )
                .onErrorMap(RuntimeException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    private MediaImpl createValidatedMediaCopy(UUID id, String name, Media media) {
        MediaImpl createdMedia = new MediaImpl(id.toString(), media.getName(),
                media.getFileId(), media.getFileExtension(), parseFolderPathFormat(media.getFilePath()), name, media.getTags());
        createdMedia.validate();
        return createdMedia;
    }

    Mono<ServerResponse> download(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.principal()
                .map(Principal::getName)
                .flatMap(name -> mediaRepo
                        .findByIdAndOwnerId(id, name)
                        .flatMap(existingMedia -> {
                            GridFSFile gridFsfile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(existingMedia.getFileId())));
                            if (gridFsfile == null) {
                                return notFound().build();
                            }
                            Resource rs = new GridFsResource(gridFsfile, getGridFsBucket().openDownloadStream(gridFsfile.getObjectId()));
                            return ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fromObject(rs));
                        })
                        .switchIfEmpty(notFound().build()));
    }

    Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        String fileKey = "file";

        return request.principal()
                .map(Principal::getName)
                .flatMap(name -> request.body(BodyExtractors.toMultipartData())
                        .flatMap(parts -> {
                            Map<String, Part> parameterFileMap = parts.toSingleValueMap();
                            if (!parameterFileMap.containsKey(fileKey)) {
                                return ServerResponse.status(HttpStatus.BAD_REQUEST).body(fromObject("File for upload required. Key name must be '" + fileKey + "'."));
                            }
                            return handleUpload(id, name, (FilePart) parameterFileMap.get(fileKey));
                        }));
    }

    private Mono<? extends ServerResponse> handleUpload(String id, String userId, FilePart part) {
        try {
            Path upload = Files.createTempFile("mvs_", "_upload_" + part.filename());
            Mono<Void> fileTransferedMono = part.transferTo(upload.toFile());
            String fileId = gridFsTemplate.store(Files.newInputStream(upload), part.filename()).toString();
            String fileName = part.filename();
            String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

            Mono<Media> existingMediaMono = mediaRepo.findByIdAndOwnerId(id, userId);

            return fileTransferedMono.then(
                    ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(fromPublisher(
                                    existingMediaMono.map(existingMedia -> {
                                        existingMedia.setName(fileName);
                                        existingMedia.setFileId(fileId);
                                        existingMedia.setFileExtension(fileExtension);
                                        existingMedia.setFileMetaData(MetadataParser.parse(upload));
                                        return existingMedia;
                                    }).flatMap(mediaRepo::save), Media.class))
                            .switchIfEmpty(notFound().build())
            );
        } catch (IOException e) {
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fromObject(e.getMessage()));
        }
    }

    Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        if (id == null || id.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("Id must not be empty"));
        }
        Mono<Media> media = request.bodyToMono(Media.class);

        return request.principal()
                .map(Principal::getName)
                .flatMap(name ->
                        mediaRepo.findByIdAndOwnerId(id, name)
                                .flatMap(existingMedia ->
                                        ok().contentType(MediaType.APPLICATION_JSON).body(
                                                fromPublisher(media.map(p -> {
                                                    Media recreatedMedia = new MediaImpl(id, p.getName(),
                                                            p.getFileId(), p.getFileExtension(), parseFolderPathFormat(p.getFilePath()), name, p.getTags());
                                                    recreatedMedia.setFileMetaData(p.getFileMetaData());
                                                    return recreatedMedia;
                                                }).flatMap(mediaRepo::save), Media.class)))
                                .switchIfEmpty(notFound().build()));
    }

    Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        if (id == null || id.isEmpty()) {
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("Id must not be empty"));
        }
        return request.principal()
                .map(Principal::getName)
                .flatMap(name ->
                        mediaRepo.findByIdAndOwnerId(id, name)
                                .flatMap(existingMedia -> {
                                    Query deleteFromGridFs = new Query(GridFsCriteria.where("_id").is(existingMedia.getFileId()));
                                    gridFsTemplate.delete(deleteFromGridFs);
                                    return noContent().build(mediaRepo.delete(existingMedia));
                                })
                                .switchIfEmpty(notFound().build()));
    }

    Mono<ServerResponse> deleteFolder(ServerRequest request) {
        String filePath = request.pathVariable("folderPath");

        return request.principal()
                .map(Principal::getName)
                .flatMap(name ->
                        mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(name, parseFolderPathFormat(filePath))
                                .map(toDelete -> {
                                    gridFsTemplate.delete(new Query(Criteria.where("_id").is(toDelete.getFileId())));
                                    return noContent().build(); // will not be returned to user
                                })
                                .onErrorReturn(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                                .then(mediaTemplateOps.deleteAllByOwnerIdAndFilePathStartingWith(name, parseFolderPathFormat(filePath))
                                        .flatMap(deleteResult -> {
                                            if (deleteResult.getDeletedCount() == 0) {
                                                return notFound().build();
                                            } else {
                                                return noContent().build();
                                            }
                                        })));
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
