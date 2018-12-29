package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.apache.tika.metadata.TIFF;
import org.slf4j.LoggerFactory;

public class ImageMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    ImageMetadataTranslator() {
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("width", "height", "modified", "created", "content-type");
        helper.addTranslation(TIFF.IMAGE_LENGTH.getName(), "height");
        helper.addTranslation(TIFF.IMAGE_WIDTH.getName(), "width");
        helper.addTranslation("Creation-Date", "created");
        helper.addTranslation("modified", "modified");
        helper.addTranslation("Content-Type", "content-type");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        helper.collect(metadataKey, metadataValue);
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return helper.getMetadata();
    }
}
