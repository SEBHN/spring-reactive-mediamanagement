package de.hhn.mvs.rest;

import com.mongodb.client.gridfs.model.GridFSFile;
import de.hhn.mvs.MediaCreator;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class MediaHandler {

    @Autowired
    private MediaCrudRepo mediaRepo;
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public MediaHandler(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }


    public Mono<ServerResponse> get(ServerRequest request) {
        String id = request.pathVariable("id").toString();
        return ok().contentType(MediaType.APPLICATION_JSON).body(mediaRepo.findById(id), Media.class);
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(mediaRepo.findAll(), Media.class);
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Media> media = request.bodyToMono(Media.class);
        UUID id = UUID.randomUUID();
        return ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        fromPublisher(
                                media.map(p -> new MediaImpl(id.toString(), p.getName(),
                                        p.getFileId(), p.getFileExtension(), p.getFilePath(), p.getTags()))
                                        .flatMap(mediaRepo::save), Media.class));
    }

    public Mono<ServerResponse> download(ServerRequest request) {

        /**
        String id = request.pathVariable("id");
        Mono<Media> media = mediaRepo.findById(id);
        String fileId = "";


        return ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(
                        BodyInserters.fromPublisher(
                                media.map(p ->
                                {
                                    GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(p.getFileId())));
                                    return file;

                                })
                                , FilePart.class));

**/
//        return ok().body(BodyInserters.fromPublisher(
//                gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)))
//        )); // send file somehow reactive way
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).body(null); // send file somehow reactive way
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData()).flatMap(parts -> {

            Map<String, Part> parameterFileMap = parts.toSingleValueMap();
            FilePart part = (FilePart) parameterFileMap.get("file");
            ObjectId fileId = null;
            try {
                Path upload = Files.createTempFile("mvs_", "_upload");
                part.transferTo(upload.toFile());
                fileId = gridFsTemplate.store(Files.newInputStream(upload), part.filename());
            } catch (IOException e) {
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BodyInserters.fromObject(e.getMessage()));
            }

            String fileName = part.filename();
            String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
            String fileIdString = fileId.toString();


            String id = request.pathVariable("id");
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


}
