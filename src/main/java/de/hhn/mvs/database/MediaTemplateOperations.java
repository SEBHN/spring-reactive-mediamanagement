package de.hhn.mvs.database;

import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class MediaTemplateOperations {

    @Autowired
    ReactiveMongoTemplate template;

    @Autowired
    MediaCrudRepo repo;

    public Flux<Media> findByIncasesenstitiveTags(String ownerId, String regex, List<Tag> tags){
        //filter { 'ownerId': ?0, filePath: {$regex: ?1}, tags: { $all : ?2}}
//        Query query = new Query(filter);
//        query.collation(Collation.of("en").
//                strength(Collation.ComparisonLevel.secondary()));
//        template.find(query,clazz,collection);
        return repo.findAllByOwnerIdAndFilePathRegexAndTagsContainingAll(ownerId, regex,  tags);
    }

}
