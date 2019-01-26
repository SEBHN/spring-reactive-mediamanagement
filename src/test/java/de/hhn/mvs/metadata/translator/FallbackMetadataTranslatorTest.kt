package de.hhn.mvs.metadata.translator

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FallbackMetadataTranslatorTest {

    private lateinit var translator: FallbackMetadataTranslator

    @Before
    fun setUp() {
        translator = FallbackMetadataTranslator()
    }

    @Test
    fun testFallBack(){
        val metadata = translator.collect("something", "someValue").metadata
        assertEquals(1, metadata.size)
        assertEquals("someValue", metadata.entries.first().value)
        assertEquals("something", metadata.entries.first().key)
    }
}
