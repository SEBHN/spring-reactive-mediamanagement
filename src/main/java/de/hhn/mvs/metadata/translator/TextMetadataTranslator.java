package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.apache.tika.mime.MediaType;
import org.slf4j.LoggerFactory;

public class TextMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    TextMetadataTranslator(MediaType type){
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("encoding", "content-type");
        // override content-type (for some reason tika thinks its text/plain instead of application/json)
        helper.addMetadata("content-type", type.toString());

        helper.addTranslation("Content-Encoding", "encoding");
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
