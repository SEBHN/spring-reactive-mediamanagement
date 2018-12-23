package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.slf4j.LoggerFactory;

public class PDFMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    PDFMetadataTranslator() {
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("creator", "author", "pages", "language", "encrypted", "producer",
                "modified", "created", "content-type");

        helper.addTranslation("creator", "creator");
        helper.addTranslation("Author", "author");
        helper.addTranslation("xmpTPg:NPages", "pages");
        helper.addTranslation("language", "language");
        helper.addTranslation("pdf:encrypted", "encrypted");
        helper.addTranslation("producer", "producer");
        helper.addTranslation("modified", "modified");
        helper.addTranslation("Creation-Date", "created");
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
