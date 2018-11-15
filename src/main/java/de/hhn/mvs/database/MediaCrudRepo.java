package de.hhn.mvs.database;

import de.hhn.mvs.model.Media;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MediaCrudRepo extends ReactiveMongoRepository<Media, String> {
    Mono<Media> save(Media media);
    Flux<Media> findAll();

    Flux<Media> findAllByFilePathContains(String searchString);

    Flux<Media> findAllByFilePath(String filePath);

    @Query("{ 'id': ?0, 'ownerId': ?1}")
    Mono<Media> findByIdAndUser(String id, String userId);

    @Query("{ 'ownerId': ?0}")
    Flux<Media> findByUser(String userId);

}
