package de.hhn.mvs.model;

import java.util.List;

/**
 * Media-metadata
 */
public interface Media {

    String getId();

    String getName();

    void setName(String name);

    String getFile();

    void setFile(String file);

    String getFileExtension();

    void setFileExtension(String fileExtension);

    String getFilePath();

    void setFilePath(String filePath);

    List<Tag> getTags();

    void setTags(List<Tag> tags);

    boolean addTag(Tag tag);

    boolean removeTag(Tag tag);

}
