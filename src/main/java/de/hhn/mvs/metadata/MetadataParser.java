package de.hhn.mvs.metadata;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
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
     * @throws TikaException - thrown from tika metadata parser
     * @throws SAXException  - thrown from tika metadata parser
     */
    public static Map<String, String> parse(Path file) throws IOException, TikaException, SAXException {
        Map<String, String> metaData = new HashMap<>();

        AutoDetectParser parser = new AutoDetectParser();
        Metadata parsedMetaData = new Metadata();

        try (InputStream stream = Files.newInputStream(file)) {
            parser.parse(stream, new BodyContentHandler(), parsedMetaData);

            for (String type : parsedMetaData.names()) {
                metaData.put(type, parsedMetaData.get(type));
            }
        }

        return metaData;
    }
}
