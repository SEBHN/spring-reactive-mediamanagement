package de.hhn.mvs.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public MediaImpl(String id, String name, String fileId, String fileExtension, String filePath, Tag... tags) {
        this(id, name, fileId, fileExtension, filePath, new ArrayList<>(Arrays.asList(tags)));
    }

    public MediaImpl(String id, String name, String fileId, String fileExtension, String filePath, List<Tag> tags) {
        this.id = id;
        this.name = name;
        this.fileId = fileId;
        this.fileExtension = fileExtension;
        this.filePath = filePath;
        this.tags = tags;
    }

    public boolean validate() {
        if (name == null && fileId == null && fileExtension == null && filePath == null && (tags == null || tags.size() == 0))
            throw new IllegalArgumentException("Parameters for creating a Media Object must not be empty");
        return true;
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
    public void setFileId(String fileId) {
        this.fileId = fileId;
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

    @Override
    public String toString() {
        return "Media{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", fileId='" + fileId + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", filePath='" + filePath + '\'' +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaImpl media = (MediaImpl) o;
        return Objects.equals(id, media.id) &&
                Objects.equals(name, media.name) &&
                Objects.equals(fileId, media.fileId) &&
                Objects.equals(fileExtension, media.fileExtension) &&
                Objects.equals(filePath, media.filePath) &&
                Objects.equals(tags, media.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fileId, fileExtension, filePath, tags);
    }
}
