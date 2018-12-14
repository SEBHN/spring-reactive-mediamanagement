package de.hhn.mvs.metadata.translator;

import org.apache.tika.metadata.TIFF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;
    private final HashMap<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(MetadataTranslator.class);

    ImageMetadataTranslator() {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("width", "");
        metadata.put("height", "");
        metadata.put("Modified", "");
        metadata.put("Created", "");
        metadata.put("Content-Type", "");

        translations = new HashMap<>();
        translations.put(TIFF.IMAGE_LENGTH.getName(), "height");
        translations.put(TIFF.IMAGE_WIDTH.getName(), "width");
        translations.put("Creation-Date", "Created");
        translations.put("modified", "Modified");
        translations.put("Content-Type", "Content-Type");
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
