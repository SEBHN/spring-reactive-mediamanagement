package de.hhn.mvs.rest;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.empty;
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
    public MediaHandler(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }


    Mono<ServerResponse> get(ServerRequest request) {
        String mediaId = request.pathVariable("id");
        return mediaRepo.findById(mediaId)
                .flatMap(person -> ok().contentType(APPLICATION_JSON).body(fromObject(person)))
                .switchIfEmpty(notFound().build());
    }

    Mono<ServerResponse> list(ServerRequest request) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(mediaRepo.findAll(), Media.class);
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
                                    MediaImpl createdMedia = new MediaImpl(id.toString(), p.getName(),
                                            p.getFileId(), p.getFileExtension(), p.getFilePath(), p.getTags());
                                    createdMedia.validate();
                                    return createdMedia;

                                })
                                        .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                                        .onErrorMap(DecodingException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                                        .flatMap(mediaRepo::save), Media.class)

                )
                .onErrorReturn(ServerResponse.badRequest().build().block())
                .onErrorMap(RuntimeException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))

                ;

    }

    Mono<ServerResponse> download(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Media> mediaMono = mediaRepo.findById(id);

        Mono<Resource> rs = mediaMono.map(media -> {
            GridFSFile gridFsfile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(media.getFileId())));

            return new GridFsResource(gridFsfile, getGridFsBucket().openDownloadStream(gridFsfile.getObjectId()));

        });
        return ServerResponse.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(fromPublisher(rs, Resource.class));
    }


    private GridFSBucket getGridFsBucket() {
        MongoDatabase db = mongoDbFactory.getDb();
        return GridFSBuckets.create(db);
    }


    Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
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

                    Mono<Media> media = mediaRepo.findById(id);

                    return ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(
                                    fromPublisher(
                                            media.map(p -> new MediaImpl(p.getId(), fileName,
                                                    fileIdString, fileExtension, p.getFilePath(), p.getTags()))
                                                    .flatMap(mediaRepo::save), Media.class));


                });

    }

    //for error handling see: https://stackoverflow.com/questions/48711872/handling-exceptions-and-returning-proper-http-code-with-webflux


    public HandlerFilterFunction<ServerResponse, ServerResponse> illegalArgumentToBadRequest() {
        return (request, next) -> next.handle(request)
                .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    public HandlerFilterFunction<ServerResponse, ServerResponse> illegalStateToBadRequest() {
        return (request, next) -> next.handle(request)
                .onErrorMap(IllegalStateException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }


    Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        if (id == null || id.isEmpty())
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(fromObject("Id must not be empty"));
        Mono<Media> media = request.bodyToMono(Media.class);

        return mediaRepo
                .findById(id)
                .flatMap(existingMedia -> ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(
                                fromPublisher(
                                        media.map(p -> new MediaImpl(id, p.getName(),
                                                p.getFileId(), p.getFileExtension(), p.getFilePath(), p.getTags()))
                                                .flatMap(mediaRepo::save), Media.class)))
                .switchIfEmpty(notFound().build());
    }


    Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        if (id == null || id.isEmpty())
            return ServerResponse.status(HttpStatus.BAD_REQUEST).body(fromObject("Id must not be empty"));

        return mediaRepo
                .findById(id)
                .flatMap(existingMedia -> noContent().build(mediaRepo.delete(existingMedia)))
                .switchIfEmpty(notFound().build());
    }


}
