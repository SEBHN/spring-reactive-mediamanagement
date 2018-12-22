package de.hhn.mvs.metadata.translator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tika.metadata.TIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;
    private final HashMap<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(MetadataTranslator.class);

    ImageMetadataTranslator() {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("width", "");
        metadata.put("height", "");
        metadata.put("modified", "");
        metadata.put("created", "");
        metadata.put("content-type", "");

        translations = new HashMap<>();
        translations.put(TIFF.IMAGE_LENGTH.getName(), "height");
        translations.put(TIFF.IMAGE_WIDTH.getName(), "width");
        translations.put("Creation-Date", "created");
        translations.put("modified", "modified");
        translations.put("Content-Type", "content-type");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            metadata.put(translations.get(metadataKey), metadataValue);
        } else {
            logger.info("Ignored image metadata property: " + metadataKey);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
