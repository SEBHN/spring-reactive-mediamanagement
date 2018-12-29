package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.slf4j.LoggerFactory;

public class AudioMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    AudioMetadataTranslator() {
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("title", "artist", "album", "year", "track number", "genre", "duration",
                "sample rate", "channel type", "creator", "content-type");

        helper.addTranslation("xmpDM:genre", "genre");
        helper.addTranslation("creator", "creator");
        helper.addTranslation("xmpDM:album", "album");
        helper.addTranslation("xmpDM:artist", "artist");
        helper.addTranslation("xmpDM:audioChannelType", "channel type");
        helper.addTranslation("title", "title");
        helper.addTranslation("samplerate", "sample rate");
        helper.addTranslation("xmpDM:duration", "duration");
        helper.addTranslation("Content-Type", "content-type");
        helper.addTranslation("xmpDM:releaseDate", "year");
        helper.addTranslation("xmpDM:trackNumber", "track number");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        String translatedKey = helper.getTranslations().get(metadataKey);
        if ("duration".equals(translatedKey)) {
            helper.addMetadata(translatedKey, Duration.toHumanFromMillis(metadataValue));
        } else {
            helper.collect(metadataKey, metadataValue);
        }
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return helper.getMetadata();
    }
}
