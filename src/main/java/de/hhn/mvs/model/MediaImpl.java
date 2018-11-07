package de.hhn.mvs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document
public final class MediaImpl implements Media {

    @Id
    private String id;
    private String name;
    private String fileId;
    private String fileExtension;
    private String filePath;
    private List<Tag> tags;

    public MediaImpl() {
        // for jackson
    }

    public MediaImpl(String id, String name, String file, String fileExtension, String filePath, Tag... tags) {
        this(id, name, file, fileExtension, filePath, new ArrayList<>(Arrays.asList(tags)));
    }

    public MediaImpl(String id, String name, String file, String fileExtension, String filePath, List<Tag> tags) {
        this.id = id;
        this.name = name;
        this.fileId = file;
        this.fileExtension = fileExtension;
        this.filePath = filePath;
        this.tags = tags;
    }

        @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFileId() {
        return fileId;
    }

    @Override
    public void setFileId(String file) {
        this.fileId = file;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean addTag(Tag tag) {
        return this.tags.add(tag);
    }

    @Override
    public boolean removeTag(Tag tag) {
        return this.tags.remove(tag);
    }

}
