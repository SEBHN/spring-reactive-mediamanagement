package de.hhn.mvs.database;

import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.io.File;

@Repository
public interface MediaCrudRepo
        extends ReactiveCrudRepository<MediaImpl, String> {

//    public void saveFile(File file, String mediaId);

//    Mono<MediaImpl> saveMedia();
    //public Mono<String> saveMedia(MediaImpl media);

    Mono<MediaImpl> findMediaImplById (String id);

    //Mono<MediaImpl> save(MediaImpl medium);
    //.save()
    //.findAll()
    //Flux<MediaImpl> findAllByFolderName(String folderName);
    //https://www.baeldung.com/spring-data-mongodb-reactive
    //https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/reactive/ReactiveCrudRepository.html


    //repeat for file



}
