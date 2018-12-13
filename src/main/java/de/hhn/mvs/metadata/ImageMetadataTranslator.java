package de.hhn.mvs.metadata;

import java.util.HashMap;
import java.util.Map;

public class ImageMetadataTranslator implements MetadataTranslator {

    private final HashMap<String, String> metadata;

    ImageMetadataTranslator() {
        metadata = new HashMap<>();
    }

    @Override
    public Map<String, String> translate(String metadataKey, String metadataValue) {
        metadata.put(metadataKey, metadataValue);
        return metadata;
    }
}
