package de.hhn.mvs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MediaImpl implements Media{
    @Id
    private String id;
    private String name;
    private String file;
    private String fileExtension;
    private String filePath;
}
