package de.hhn.mvs.metadata;

import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        assertEquals("720", metadata.get("tiff:ImageLength"));
        assertEquals("1280", metadata.get("tiff:ImageWidth"));
        assertEquals("1970-01-01T00:00:00Z", metadata.get("Creation-Date"));
        assertEquals("1.0 MB", metadata.get("size"));
        assertEquals("application/mp4", metadata.get("Content-Type"));
    }

    @Test
    public void testImageJpg() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.jpg"));
        assertFalse(metadata.isEmpty());
        assertEquals("300 pixels", metadata.get("Image Height"));
        assertEquals("300 pixels", metadata.get("Image Width"));
        assertEquals("300", metadata.get("tiff:ImageLength"));
        assertEquals("300", metadata.get("tiff:ImageWidth"));
        assertEquals("2017-09-11T22:15:46", metadata.get("Creation-Date"));
        assertEquals("2017-09-11T22:15:46", metadata.get("modified"));
        assertEquals("Top, left side (Horizontal / normal)", metadata.get("Orientation"));
        assertEquals("1 (Adobe Photoshop, Adobe Photoshop CS5) 1", metadata.get("Version Info"));
        assertEquals("49.9 kB", metadata.get("size"));
        assertEquals("image/jpeg", metadata.get("Content-Type"));
    }

    private Path getPathFromResource(String filename) throws URISyntaxException {
        return Paths.get(getClass().getClassLoader().getResource("metadatasamplefiles/" + filename).toURI());
    }
}
