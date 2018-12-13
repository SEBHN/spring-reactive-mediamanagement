package de.hhn.mvs.metadata;

import java.util.Map;

public interface MetadataTranslator {

    Map<String, String> translate(String metadataKey, String metadataValue);
}
