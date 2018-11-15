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
}
