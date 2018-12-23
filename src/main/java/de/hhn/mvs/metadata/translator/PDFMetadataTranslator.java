package de.hhn.mvs.metadata.translator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;
    private final HashMap<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(PDFMetadataTranslator.class);

    PDFMetadataTranslator() {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("creator", "");
        metadata.put("author", "");
        metadata.put("pages", "");
        metadata.put("language", "");
        metadata.put("encrypted", "");
        metadata.put("producer", "");
        metadata.put("modified", "");
        metadata.put("created", "");
        metadata.put("content-type", "");

        translations = new HashMap<>();
        translations.put("creator", "creator");
        translations.put("Author", "author");
        translations.put("xmpTPg:NPages", "pages");
        translations.put("language", "language");
        translations.put("pdf:encrypted", "encrypted");
        translations.put("producer", "producer");
        translations.put("modified", "modified");
        translations.put("Creation-Date", "created");
        translations.put("Content-Type", "content-type");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            metadata.put(translations.get(metadataKey), metadataValue);
        } else {
            logger.info("Ignored pdf metadata property: " + metadataKey);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        metadata.values().removeIf(String::isEmpty);
        return metadata;
    }
}
