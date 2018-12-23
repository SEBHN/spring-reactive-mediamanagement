package de.hhn.mvs.database;

import com.mongodb.client.result.DeleteResult;
import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import de.hhn.mvs.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class MediaTemplateOperations {

    @Autowired
    ReactiveMongoTemplate template;

    @Autowired
    MediaCrudRepo repo;

    public Flux<Media> findAllByOwnerIdAndFilePathRegexAndTagsContainingAll_notCaseSensitive(String ownerId, String regex, List<Tag> tags){
        Criteria c = Criteria.where("ownerId").is(ownerId).and("filePath").regex(regex).and("tags").all(tags);
        Query query = new Query(c);
        query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary())); //find with incasesensitive index
        return template.find(query, Media.class);
    }


    public Mono<DeleteResult> deleteAllByOwnerIdAndFilePathStartingWith(String ownerId, String folderPath){
        String preparedRegex = "^" + folderPath;
        Criteria c = Criteria.where("ownerId").is(ownerId).and("filePath").regex(preparedRegex);
        Query query = new Query(c);
        return template.remove(query, Media.class);
    }

}
