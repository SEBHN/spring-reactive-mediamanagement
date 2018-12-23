package de.hhn.mvs.metadata.translator;

import java.util.Map;

import org.apache.tika.metadata.TIFF;
import org.apache.tika.mime.MediaType;
import org.slf4j.LoggerFactory;

public class VideoMetadataTranslator implements MetadataTranslator {

    private final MetadataTranslatorHelper helper;

    VideoMetadataTranslator(MediaType type) {
        helper = new MetadataTranslatorHelper(LoggerFactory.getLogger(getClass()));
        helper.addMetadataOrder("duration", "width", "height", "modified", "created");
        // override content-type (for some reason tika thinks its application/mp4 instead of video/mp4)
        helper.addMetadata("content-type", type.toString());

        helper.addTranslation(TIFF.IMAGE_LENGTH.getName(), "height");
        helper.addTranslation(TIFF.IMAGE_WIDTH.getName(), "width");
        helper.addTranslation("xmpDM:duration", "duration");
        helper.addTranslation("Creation-Date", "created");
        helper.addTranslation("modified", "modified");
    }

    @Override
    public MetadataTranslator collect(String metadataKey, String metadataValue) {
        String translatedKey = helper.getTranslations().get(metadataKey);
        if ("duration".equals(translatedKey)) {
            helper.addMetadata(translatedKey, Duration.toHumanFromSeconds(metadataValue));
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
