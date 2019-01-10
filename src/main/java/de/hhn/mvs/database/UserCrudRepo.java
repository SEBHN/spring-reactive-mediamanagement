package de.hhn.mvs.database;

import de.hhn.mvs.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserCrudRepo extends ReactiveMongoRepository<User, String> {

    Mono<User> findById(String id);
    Mono<User> findByEmail(String email);
}