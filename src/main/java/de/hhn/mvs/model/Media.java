package de.hhn.mvs.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Media-metadata
 */
@JsonDeserialize(as = MediaImpl.class)
public interface Media {

    String getId();

    String getName();

    void setName(String name);

    String getFileId();

    void setFileId(String fileId);

    String getFileExtension();

    void setFileExtension(String fileExtension);

    String getFilePath();

    void setFilePath(String filePath);

    List<Tag> getTags();

    void setTags(List<Tag> tags);

    boolean addTag(Tag tag);

    boolean removeTag(Tag tag);

}
