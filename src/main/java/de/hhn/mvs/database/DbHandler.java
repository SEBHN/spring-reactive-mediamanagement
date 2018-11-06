package de.hhn.mvs.database;

import de.hhn.mvs.model.Media;
import de.hhn.mvs.model.MediaImpl;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.io.File;

public class DbHandler {
    private final GridFsTemplate gridFsTemplate;


    public DbHandler(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public boolean saveFile(File file, String mediaId){

        return false;
    }

    public String saveMedia(MediaImpl media){

        String mediaId = null;


        return mediaId;
    }
    

}
