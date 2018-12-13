package de.hhn.mvs.metadata;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/***
 * Im responsible for parsing metadata of a given {@link Path}.
 */
public class MetadataParser {

    /**
     * Give me a Path and I return you a map with all its metadata.
     *
     * @param file a path to the metadatasamplefiles which you would like to have the metadata
     * @return A map with all found metadata (key = metadata name, value = metadata value)
     * @throws IOException   thrown when no inputstream can be created
     */
    public static Map<String, String> parse(Path file) throws IOException {
        Map<String, String> metaData = new HashMap<>();

        AutoDetectParser parser = new AutoDetectParser();
        Metadata parsedMetaData = new Metadata();
        metaData.put("size", humanReadableByteCount(Files.size(file)));
        String mimeType = Files.probeContentType(file);
        try (InputStream stream = Files.newInputStream(file)) {
            parser.parse(stream, new BodyContentHandler(), parsedMetaData);
            MediaType contentType = MediaType.parse(mimeType);
            MetadataTranslator translator = MetadataTranslatorFactory.get(file, contentType);

            for (String type : parsedMetaData.names()) {
                metaData.putAll(translator.translate(type, parsedMetaData.get(type)));
            }
        } catch (TikaException | SAXException e) {
            throw new IOException(e.getMessage(), e);
        }

        return metaData;
    }


    private static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf("kMGTPE".charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
