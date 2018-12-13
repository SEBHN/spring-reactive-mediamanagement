package de.hhn.mvs.metadata;

import java.util.Map;

public interface MetadataTranslator {

    MetadataTranslator collect(String metadataKey, String metadataValue);

    Map<String, String> getMetadata();


}
