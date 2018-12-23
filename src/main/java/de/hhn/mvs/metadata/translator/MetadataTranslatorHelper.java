package de.hhn.mvs.metadata.translator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;

class MetadataTranslatorHelper {

    private final Map<String, String> metadata;
    private final Map<String, String> translations;
    private final Logger logger;

    MetadataTranslatorHelper(Logger logger) {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        translations = new HashMap<>();
        this.logger = logger;
    }

    String addMetadata(String key, String value) {
        return metadata.put(key, value);
    }

    void addMetadataOrder(String... keys) {
        for (String key : keys) {
            addMetadata(key, "");
        }
    }

    String addTranslation(String key, String value) {
        return translations.put(key, value);
    }

    void collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            metadata.put(translations.get(metadataKey), metadataValue);
        } else {
            logger.info("Ignored metadata property: " + metadataKey);
        }
    }

    Map<String, String> getTranslations() {
        return translations;
    }

    Map<String, String> getMetadata() {
        metadata.values().removeIf(String::isEmpty);
        return metadata;
    }
}
