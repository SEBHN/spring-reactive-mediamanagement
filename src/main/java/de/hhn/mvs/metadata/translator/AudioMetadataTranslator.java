package de.hhn.mvs.metadata.translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AudioMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;
    private final Map<String, String> translations;
    private Logger logger = LoggerFactory.getLogger(MetadataTranslator.class);


    AudioMetadataTranslator() {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("title", "");
        metadata.put("artist", "");
        metadata.put("album", "");
        metadata.put("year", "");
        metadata.put("track number", "");
        metadata.put("duration", "");
        metadata.put("sample rate", "");
        metadata.put("channel type", "");
        metadata.put("Content-Type", "");

        translations = new HashMap<>();
        translations.put("xmpDM:genre", "genre");
        translations.put("creator", "creator");
        translations.put("xmpDM:album", "album");
        translations.put("xmpDM:artist", "artist");
        translations.put("xmpDM:audioChannelType", "channel type");
        translations.put("title", "title");
        translations.put("samplerate", "sample rate");
        translations.put("xmpDM:duration", "duration");
        translations.put("Content-Type", "Content-Type");
        translations.put("xmpDM:releaseDate","year");
        translations.put("xmpDM:trackNumber","track number");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            String translatedKey = translations.get(metadataKey);
            if (translatedKey.equals("duration")) {
                metadata.put(translatedKey, Duration.toHumanFromMillis(metadataValue));
            } else {
                metadata.put(translatedKey, metadataValue);
            }
        }else{
            logger.info("Ignored audio metadata property: " + metadataKey);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

}
