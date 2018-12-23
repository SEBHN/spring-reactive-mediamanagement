package de.hhn.mvs.metadata;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Test;
import org.springframework.util.ResourceUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        String size = metadata.get("size");
        assertTrue(size.equals("441.6 kB") || size.equals("441,6 kB"));
        assertEquals("audio/mpeg", metadata.get("content-type"));
    }

    @Test
    public void testDocumentPdf() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.pdf"));
        assertEquals("Manuel", metadata.get("creator"));
        assertEquals("Manuel", metadata.get("author"));
        assertEquals("2018-12-06T20:51:08Z", metadata.get("created"));
        assertEquals("2018-12-06T20:51:08Z", metadata.get("modified"));
        String size = metadata.get("size");
        assertTrue(size.equals("181.7 kB") || size.equals("181,7 kB"));
        assertEquals("de-CH", metadata.get("language"));
        assertEquals("false", metadata.get("encrypted"));
        assertEquals("1", metadata.get("pages"));
        assertEquals("Microsoft® Word 2016", metadata.get("producer"));
        assertEquals("application/pdf", metadata.get("content-type"));
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void testDocumentTxt() throws Exception{
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.txt"));
        assertEquals("ISO-8859-1", metadata.get("encoding"));
        assertEquals("18 B", metadata.get("size"));
        assertEquals("text/plain", metadata.get("content-type"));
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void testDocumentXML() throws Exception{
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.xml"));
        String size = metadata.get("size");
        assertTrue(size.equals("170 B") || size.equals("170 b"));
        assertEquals("application/xml", metadata.get("content-type"));
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void testDocumentJSON() throws Exception{
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.json"));
        String size = metadata.get("size");
        assertTrue(size.equals("603 B") || size.equals("603 b"));
        assertEquals("windows-1252", metadata.get("encoding"));
        assertEquals("application/json", metadata.get("content-type"));
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void testDocumentDocX() throws Exception{
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.docx"));
        assertEquals("Manuel", metadata.get("creator"));
        assertEquals("Manuel", metadata.get("author"));
        assertEquals("2018-12-23T18:14:00Z", metadata.get("created"));
        assertEquals("2018-12-23T18:18:00Z", metadata.get("modified"));
        String size = metadata.get("size");
        assertTrue(size.equals("11.6 kB") || size.equals("11,6 kB"));
        assertEquals("1", metadata.get("pages"));
        assertEquals("1", metadata.get("words"));
        assertEquals("7", metadata.get("characters"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", metadata.get("content-type"));
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void testDocumentOdt() throws Exception{
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.odt"));
        assertEquals("Jürgen Schmidt", metadata.get("creator"));
        assertEquals("Jürgen Schmidt", metadata.get("author"));
        assertEquals("2002-12-18T12:28:35", metadata.get("created"));
        assertEquals("2002-12-18T12:31:15", metadata.get("modified"));
        String size = metadata.get("size");
        assertTrue(size.equals("6.6 kB") || size.equals("6,6 kB"));
        assertEquals("1", metadata.get("pages"));
        assertEquals("77", metadata.get("words"));
        assertEquals("511", metadata.get("characters"));
        assertEquals("application/vnd.oasis.opendocument.text", metadata.get("content-type"));
        assertFalse(metadata.isEmpty());
    }


    @Test
    public void testVideoMp4() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample_1280x720.mp4"));
        assertFalse(metadata.isEmpty());
        assertEquals("00:00:05", metadata.get("duration"));
        assertEquals("720", metadata.get("height"));
        assertEquals("1280", metadata.get("width"));
        assertEquals("1970-01-01T00:00:00Z", metadata.get("created"));
        assertEquals("1970-01-01T00:00:00Z", metadata.get("modified"));
        String size = metadata.get("size");
        assertTrue(size.equals("1.0 MB") || size.equals("1,0 MB"));
        assertEquals("video/mp4", metadata.get("content-type"));
    }

    @Test
    public void testImageJpg() throws Exception {
        Map<String, String> metadata = MetadataParser.parse(getPathFromResource("sample.jpg"));
        assertFalse(metadata.isEmpty());
        assertEquals("300", metadata.get("height"));
        assertEquals("300", metadata.get("width"));
        String size = metadata.get("size");
        assertTrue(size.equals("49.9 kB") || size.equals("49,9 kB"));
        assertEquals("image/jpeg", metadata.get("content-type"));
    }

    private Path getPathFromResource(String filename) throws FileNotFoundException {
        return ResourceUtils.getFile("classpath:metadatasamplefiles/" + filename).toPath();
    }
}
