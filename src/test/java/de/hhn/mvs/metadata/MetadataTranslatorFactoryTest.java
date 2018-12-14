package de.hhn.mvs.metadata;

import de.hhn.mvs.metadata.translator.*;
import org.apache.tika.mime.MediaType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testing {@link MetadataTranslatorFactory}
 */
public class MetadataTranslatorFactoryTest {


    @Test
    public void testGetWithAudioExpectAudioInstance() {
        assertEquals(AudioMetadataTranslator.class, MetadataTranslatorFactory.get(MediaType.audio("mp3")).getClass());
    }

    @Test
    public void testGetWithImageExpectImageInstance() {
        assertEquals(ImageMetadataTranslator.class, MetadataTranslatorFactory.get(MediaType.image("jpeg")).getClass());
    }

    @Test
    public void testGetWithVideoExpectVideoInstance() {
        assertEquals(VideoMetadataTranslator.class, MetadataTranslatorFactory.get(MediaType.video("mp4")).getClass());
    }

    @Test
    public void testGetWithNullExpectFallbackInstance() {
        assertEquals(FallbackMetadataTranslator.class, MetadataTranslatorFactory.get(null).getClass());
    }

    @Test
    public void testGetWithUnknownFormatExpectFallbackInstance() {
        assertEquals(FallbackMetadataTranslator.class, MetadataTranslatorFactory.get(MediaType.application("imamnotexisting")).getClass());
    }


}
