package de.hhn.mvs.metadata.translator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;
    private final HashMap<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(TextMetadataTranslator.class);

    TextMetadataTranslator(MediaType type){
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("encoding", "");
        // override content-type (for some reason tika thinks its text/plain instead of application/json)
        metadata.put("content-type", type.toString());

        translations = new HashMap<>();
        translations.put("Content-Encoding", "encoding");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            metadata.put(translations.get(metadataKey), metadataValue);
        } else {
            logger.info("Ignored text metadata property: " + metadataKey);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        metadata.values().removeIf(String::isEmpty);
        return metadata;
    }
}
