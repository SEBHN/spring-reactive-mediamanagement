package de.hhn.mvs.database;

import de.hhn.mvs.model.Media;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MediaCrudRepo extends ReactiveMongoRepository<Media, String> {
    Mono<Media> save(Media media);
    Flux<Media> findAll();

    Flux<Media> findAllByFilePath(String filePath);

    Flux<Media> findAllByOwnerIdAndFilePathContains(String userId, String filePath);

    Flux<Media> findAllByOwnerIdAndFilePathEndsWith(String userId, String filePath);

    Flux<Media> findAllByOwnerIdAndFilePath(String userId, String filePath);

    Mono<Media> findByIdAndOwnerId(String id, String ownerId);

    Flux<Media> findByOwnerId(String ownerId);

}
