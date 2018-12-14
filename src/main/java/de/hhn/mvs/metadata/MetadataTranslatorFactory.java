package de.hhn.mvs.metadata;

import org.apache.tika.mime.MediaType;

/**
 * Im responsible for getting you the right {@link MetadataTranslator}
 */
public class MetadataTranslatorFactory {

    /**
     * I will return you the {@link MetadataTranslator} for the passed {@link MediaType}
     * @param contentType - the passed content/media type eg. image/png
     * @return a new instance of {@link MetadataTranslator}
     */
    public static MetadataTranslator get(MediaType contentType) {
        if (contentType == null || contentType.getType() == null) {
            return new FallbackMetadataTranslator();
        }
        switch (contentType.getType()) {
            case "audio":
                return new AudioMetadataTranslator();
            case "image":
                return new ImageMetadataTranslator();
        }
        return new FallbackMetadataTranslator();
    }
}
