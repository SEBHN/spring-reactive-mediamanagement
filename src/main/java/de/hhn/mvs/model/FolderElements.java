package de.hhn.mvs.model;

import java.util.List;
import java.util.Objects;

public class FolderElements {
    private List<Subfolder> subfolders;
    private List<Media> media;

    public FolderElements() {
        // for jackson
    }

    public FolderElements(List<Subfolder> subfolders, List<Media> media) {
        this.subfolders = subfolders;
        this.media = media;
    }


    public List<Subfolder> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(List<Subfolder> subfolders) {
        this.subfolders = subfolders;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }


    @Override
    public String toString() {
        return "Subfolders: " + subfolders.toString() + "\n"
                + "Media: " + media.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FolderElements externalFolderElements = (FolderElements) o;
        List<Subfolder> subFolders = externalFolderElements.getSubfolders();
        List<Media> media = externalFolderElements.getMedia();
        return Objects.equals(this.media, media) && Objects.equals(this.subfolders, subFolders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(media, subfolders);
    }

}


