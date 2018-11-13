package de.hhn.mvs.rest;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.connection.Server;
import com.oracle.webservices.internal.api.message.ContentType;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.asMediaType;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
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
                                media.map(p -> new MediaImpl(id.toString(), p.getName(),
                                        p.getFileId(), p.getFileExtension(), p.getFilePath(), p.getTags()))
                                        .flatMap(mediaRepo::save), Media.class));
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

    return request.body(
                BodyExtractors.toMultipartData()
        ).flatMap(parts -> {

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
                                            .flatMap(mediaRepo::save), Media.class))

                    .onErrorMap(IllegalStateException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))

//                        .onErrorResume(e -> Mono.just("Error occured: " + e.getMessage())        //testwise error handling
                    .onErrorResume(e -> Mono.just(badRequest().build())        //testwise error handling
                            .flatMap(s -> ServerResponse.status(HttpStatus.BAD_REQUEST)
                                    .contentType(MediaType.TEXT_PLAIN)
                                    .syncBody(s)));
        });

    }

    //for error handling see: https://stackoverflow.com/questions/48711872/handling-exceptions-and-returning-proper-http-code-with-webflux

    @Bean
    @ExceptionHandler({IllegalStateException.class})
    public Mono<ServerResponse> handleException() {
        return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(
                fromObject(
                        Mono.just(new BadResponse("Hello from Exception Handler"))
                )
        );
    }

    @ExceptionHandler
    public Mono<ServerResponse> handle(IllegalStateException ex) {
        return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(
                fromObject(
                        Mono.just(new BadResponse("Hello from Exception Handler"))
                )
        );
    }

    public HandlerFilterFunction<ServerResponse, ServerResponse> illegalStateToBadRequest() {
        return (request, next) -> next.handle(request)
//                .onErrorReturn(
////                        exceptionHandler()
//                        Mono.just(badRequest().build()) badRequest().build()
////                        "Hello"
//                )

                .onErrorResume(
//                        exceptionHandler()
                        IllegalStateException.class, e -> ServerResponse.badRequest().build()
                )
//                .onErrorReturn(
//                        Mono.just(ServerResponse.badRequest()))
//                )
//                .onErrorReturn(
//                        IllegalStateException.class, e -> ServerResponse.badRequest().build()
//                )

                ;
    }


    public HandlerFilterFunction<ServerResponse, ServerResponse> illegalStateToBadRequest_test() {
        return (request, next) -> next.handle(request)

//                .onErrorResume(
//                        IllegalStateException.class, e -> ServerResponse.badRequest().build()
//                )
                .onErrorMap(IllegalStateException.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()))
                ;

    }


    @Bean
    public WebExceptionHandler exceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            if (ex instanceof IllegalStateException) {
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }
            return Mono.error(ex);
        };
    }


    class BadResponse {
        private String developerMessageString;

        public BadResponse(String msg) {
            developerMessageString = msg;
        }
    }


}
