package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.slf4j.LoggerFactory;

public class OpenDocumentMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    OpenDocumentMetadataTranslator() {
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("creator", "author", "pages", "words", "characters", "modified", "created", "content-type");

        helper.addTranslation("creator", "creator");
        helper.addTranslation("meta:author", "author");
        helper.addTranslation("nbPage", "pages");
        helper.addTranslation("nbWord", "words");
        helper.addTranslation("nbCharacter", "characters");
        helper.addTranslation("modified", "modified");
        helper.addTranslation("meta:creation-date", "created");
        helper.addTranslation("Content-Type", "content-type");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        helper.collect(metadataKey, metadataValue);
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return helper.getMetadata();
    }
}
