package de.hhn.mvs.metadata.translator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenDocumentMetadataTranslator implements MetadataTranslator {
    private final Map<String, String> metadata;
    private final HashMap<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(OpenDocumentMetadataTranslator.class);

    OpenDocumentMetadataTranslator() {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("creator", "");
        metadata.put("author", "");
        metadata.put("pages", "");
        metadata.put("words", "");
        metadata.put("characters", "");
        metadata.put("modified", "");
        metadata.put("created", "");
        metadata.put("content-type", "");

        translations = new HashMap<>();
        translations.put("creator", "creator");
        translations.put("meta:author", "author");
        translations.put("nbPage", "pages");
        translations.put("nbWord", "words");
        translations.put("nbCharacter", "characters");
        translations.put("modified", "modified");
        translations.put("meta:creation-date", "created");
        translations.put("Content-Type", "content-type");
    }


    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            metadata.put(translations.get(metadataKey), metadataValue);
        } else {
            logger.info("Ignored open document metadata property: " + metadataKey);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        metadata.values().removeIf(String::isEmpty);
        return metadata;
    }
}
