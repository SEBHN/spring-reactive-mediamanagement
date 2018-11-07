package de.hhn.mvs.rest;

import de.hhn.mvs.MediaCreator;
import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
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

    public Mono<ServerResponse> get(ServerRequest request) {
        int id = Integer.parseInt(request.pathVariable("id"));
        Mono<Media> media = Mono.just(MediaCreator.getInstance().getDummyMedia().get(id));
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.APPLICATION_JSON).body(media, Media.class);
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        //Flux<Media> medias = Flux.fromIterable(MediaCreator.getInstance().getDummyMedia());
        return ok().contentType(MediaType.APPLICATION_JSON).body(mediaRepo.findAll(), Media.class);
        //return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).contentType(MediaType.APPLICATION_JSON).body(medias, Media.class);
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
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).body(null); // send file somehow reactive way
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData()).flatMap(parts -> {

            Map<String, Part> parameterFileMap = parts.toSingleValueMap();
            FilePart part = (FilePart) parameterFileMap.get("file");

            try {
                Path upload = Files.createTempFile("mvs_", "_upload");
                part.transferTo(upload.toFile());
            } catch (IOException e) {
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BodyInserters.fromObject(e.getMessage()));
            }

            String fileName = part.filename();
            String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

            int id = Integer.parseInt(request.pathVariable("id")); // maybe use formdata-key instead of pathvariable?
            Mono<Media> media = Mono.just(MediaCreator.getInstance().getDummyMedia().get(id)); // TODO: get media of repository

            // TODO: store media and upload file in database



            return ServerResponse.ok().body(media, Media.class);
        });
    }


}
