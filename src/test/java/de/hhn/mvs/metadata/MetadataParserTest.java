package de.hhn.mvs.metadata;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test of {@link MetadataParser}
 */
public class MetadataParserTest {

    @Test
    public void testAudioMp3() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.mp3"));
        assertFalse(metadata.isEmpty());
        assertEquals("Anthem", metadata.get("title"));
        assertEquals("00:01:03", metadata.get("duration"));
        assertEquals("44100", metadata.get("sample rate"));
        assertEquals("441.6 kB", metadata.get("size"));
        assertEquals("audio/mpeg", metadata.get("Content-Type"));
    }

    @Test
    public void testDocumentPdf() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.pdf"));
        assertEquals("Manuel", metadata.get("creator"));
        assertEquals("Manuel", metadata.get("Author"));
        assertEquals("2018-12-06T20:51:08Z", metadata.get("Creation-Date"));
        assertEquals("2018-12-06T20:51:08Z", metadata.get("modified"));
        assertEquals("181.7 kB", metadata.get("size"));
        assertEquals("application/pdf", metadata.get("Content-Type"));
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void testVideoMp4() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample_1280x720.mp4"));
        assertFalse(metadata.isEmpty());
        assertEquals("720", metadata.get("height"));
        assertEquals("1280", metadata.get("width"));
        assertEquals("1970-01-01T00:00:00Z", metadata.get("Created"));
        assertEquals("1970-01-01T00:00:00Z", metadata.get("Modified"));
        assertEquals("1.0 MB", metadata.get("size"));
        assertEquals("video/mp4", metadata.get("Content-Type"));
    }

    @Test
    public void testImageJpg() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.jpg"));
        assertFalse(metadata.isEmpty());
        assertEquals("300", metadata.get("height"));
        assertEquals("300", metadata.get("width"));
        assertEquals("2017-09-12T00:15:46", metadata.get("Created"));
        assertEquals("2017-09-12T00:15:46", metadata.get("Modified"));
        assertEquals("49.9 kB", metadata.get("size"));
        assertEquals("image/jpeg", metadata.get("Content-Type"));
    }

    private Path getPathFromResource(String filename) throws FileNotFoundException {
        return ResourceUtils.getFile("classpath:metadatasamplefiles/" + filename).toPath();
    }
}
