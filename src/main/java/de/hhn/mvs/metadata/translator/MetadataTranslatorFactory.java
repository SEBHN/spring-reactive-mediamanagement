package de.hhn.mvs.metadata.translator;

import java.util.logging.Logger;

import org.apache.tika.mime.MediaType;

/**
 * Im responsible for getting you the right {@link MetadataTranslator}
 */
public class MetadataTranslatorFactory {

    private static final Logger logger = Logger.getLogger(MetadataTranslatorFactory.class.getName());

    /**
     * I will return you the {@link MetadataTranslator} for the passed {@link MediaType}
     *
     * @param contentType - the passed content/media type eg. image/png
     * @return a new instance of {@link MetadataTranslator}
     */
    public static MetadataTranslator get(MediaType contentType) {
        if (contentType == null) {
            logger.warning("contentType is null, returning fallback-translator");
            return new FallbackMetadataTranslator();
        }
        switch (contentType.getType()) {
            case "audio":
                return new AudioMetadataTranslator();
            case "image":
                return new ImageMetadataTranslator();
            case "video":
                return new VideoMetadataTranslator(contentType);
            case "application":
                switch (contentType.getSubtype()) {
                    case "pdf":
                        return new PDFMetadataTranslator();
                    case "xml":
                        return new TextMetadataTranslator(contentType);
                    case "json":
                        return new TextMetadataTranslator(contentType);
                }
                break;
            case "text":
                return new TextMetadataTranslator(contentType);
        }
        logger.info("No Translator found for conent-type '" + contentType + "', returning fallback");
        return new FallbackMetadataTranslator();
    }
}
