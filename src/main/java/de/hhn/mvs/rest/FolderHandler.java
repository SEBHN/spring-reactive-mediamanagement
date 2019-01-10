package de.hhn.mvs.rest;

import java.security.Principal;

import de.hhn.mvs.database.MediaCrudRepo;
import de.hhn.mvs.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static de.hhn.mvs.rest.FolderUtils.parseFolderPathFormat;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class FolderHandler {

    @Autowired
    private MediaCrudRepo mediaRepo;

    Mono<ServerResponse> updateFolder(ServerRequest request) {
        String oldPath = request.pathVariable("oldPath");
        Mono<String> newPath = request.bodyToMono(String.class);

        Flux<String> paths = Flux.from(newPath);
        Mono<String> principalNameMono = request.principal().map(Principal::getName);

        Flux<Media> renamedMedia1 = principalNameMono.flatMapMany(name -> paths
                .flatMap(newPathString ->
                {
                    Flux<Media> mediaFlux = mediaRepo.findAllByOwnerIdAndFilePathIsStartingWith(name, parseFolderPathFormat(oldPath));
                    return mediaFlux.flatMap(media ->
                    {
                        String pathOfOldMedium = media.getFilePath();
                        String pathOfReanamedMedium = pathOfOldMedium
                                .replaceFirst(parseFolderPathFormat(oldPath), parseFolderPathFormat(newPathString));
                        media.setFilePath(pathOfReanamedMedium);
                        return mediaRepo.save(media);
                    })
                                    .onErrorMap(error ->
                                            new Exception("Error in Mapping to flux " + error.getMessage()));
                }));

        return ok().body(renamedMedia1
                        .onErrorMap(Exception.class, e ->
                                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()))
                , Media.class);
    }
}
