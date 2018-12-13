package de.hhn.mvs.metadata;

import org.apache.tika.mime.MediaType;

import java.nio.file.Path;

public class MetadataTranslatorFactory {


    public static MetadataTranslator get(Path file, MediaType contentType) {
        switch (contentType.getType()) {
            case "audio":
                return new AudioMetadataTranslator();
            case "image":
                return new ImageMetadataTranslator();
        }
        return new FallbackMetadataTranslator();
    }
}
