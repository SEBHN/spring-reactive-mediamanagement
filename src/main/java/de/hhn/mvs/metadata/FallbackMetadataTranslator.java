package de.hhn.mvs.metadata;

import java.util.HashMap;
import java.util.Map;

public class FallbackMetadataTranslator implements MetadataTranslator {

    private final HashMap<String, String> metadata;

    FallbackMetadataTranslator() {
        metadata = new HashMap<>();
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        metadata.put(metadataKey, metadataValue);
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}
