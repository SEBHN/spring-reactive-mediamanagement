package de.hhn.mvs.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AudioMetadataTranslator implements MetadataTranslator {

    private final Map<String, String> metadata;
    private final Map<String, String> translations;


    AudioMetadataTranslator() {
        metadata = new LinkedHashMap<>(); // use linked hash map to keep the following order
        metadata.put("title", "");
        metadata.put("album", "");
        metadata.put("artist", "");
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

    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        if (translations.containsKey(metadataKey)) {
            String translatedKey = translations.get(metadataKey);
            if (translatedKey.equals("duration")) {
                metadata.put(translatedKey, convertDuration(metadataValue));
            } else {
                metadata.put(translatedKey, metadataValue);
            }
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }


    private String convertDuration(String metadataValue) {
        String milliseconds = metadataValue.substring(0, metadataValue.indexOf('.'));
        long millis = Long.valueOf(milliseconds);
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
