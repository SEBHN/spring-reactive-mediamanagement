package de.hhn.mvs.metadata.translator;

import java.util.Map;

/**
 * Im responsible for translating metadata keys and sometimes its values into a human readable format
 */
public interface MetadataTranslator {

    /**
     * Collecting the needed keys/values and translate them
     * @param metadataKey - original metadata parsed key
     * @param metadataValue - original metadata value
     * @return this
     */
    MetadataTranslator collect(String metadataKey, String metadataValue);

    /**
     * Returns the translated metadata which were collected with {@link #collect(String, String)}
     * @return Map with all metadata
     */
    Map<String, String> getMetadata();


}
