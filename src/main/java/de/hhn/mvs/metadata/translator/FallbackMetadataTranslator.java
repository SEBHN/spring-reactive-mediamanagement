package de.hhn.mvs.metadata.translator;

import java.util.LinkedHashMap;
import java.util.Map;

public class FallbackMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;

    FallbackMetadataTranslator() {
        metadata = new LinkedHashMap<>();
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
