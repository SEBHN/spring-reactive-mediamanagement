package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.slf4j.LoggerFactory;

public class WordMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    WordMetadataTranslator() {
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("creator","author", "last saved from", "pages", "words", "characters",
                "modified", "created", "content-type");

        helper.addTranslation("creator", "creator");
        helper.addTranslation("meta:author", "author");
        helper.addTranslation("meta:last-author", "last saved from");
        helper.addTranslation("Page-Count", "pages");
        helper.addTranslation("Word-Count", "words");
        helper.addTranslation("Character Count", "characters");
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
