package de.hhn.mvs.metadata.translator;

import org.apache.tika.metadata.TIFF;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VideoMetadataTranslator implements MetadataTranslator {


    private final Map<String, String> metadata;
    private final Map<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(MetadataTranslator.class);

    VideoMetadataTranslator(MediaType type) {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("width", "");
        metadata.put("height", "");
        metadata.put("Modified", "");
        metadata.put("Created", "");
        // override content-type (for some reason tika thinks its application/mp4 instead of video/mp4)
        metadata.put("Content-Type", type.toString());

        translations = new HashMap<>();
        translations.put(TIFF.IMAGE_LENGTH.getName(), "height");
        translations.put(TIFF.IMAGE_WIDTH.getName(), "width");
        translations.put("Creation-Date", "Created");
        translations.put("modified", "Modified");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            metadata.put(translations.get(metadataKey), metadataValue);
        } else {
            logger.info("Ignored video metadata property: " + metadataKey);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
