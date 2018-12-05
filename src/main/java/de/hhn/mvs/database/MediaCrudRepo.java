package de.hhn.mvs.database;

import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.Tag;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface MediaCrudRepo extends ReactiveMongoRepository<Media, String> {

    Flux<Media> findAllByOwnerIdAndFilePathIsStartingWith(String userId, String filePath);

    Mono<Media> findByIdAndOwnerId(String id, String ownerId);

    Flux<Media> findAllByOwnerId(String ownerId);

    @Query("{ 'ownerId': ?0, filePath: {$regex: ?1}, tags: { $all : ?2}}")
    Flux<Media> findAllByOwnerIdAndFilePathStartingWithAndTagsContainingAll(String ownerId, String regex, List<Tag> tags);
}
