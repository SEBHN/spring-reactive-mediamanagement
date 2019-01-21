package de.hhn.mvs.model

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FolderElementsTest {

    private lateinit var folder: FolderElements
    private lateinit var medias: MutableList<Media>
    private lateinit var subfolders: MutableList<Subfolder>

    @Before
    fun setUp() {
        val subfolder = Subfolder("subfolderName")
        val media: Media = MediaImpl("id", "aname", "afileid", ".kt", "FolderElementsTest", "kotlin")
        subfolders = mutableListOf(subfolder)
        medias = mutableListOf(media)
        folder = FolderElements(subfolders, medias)
    }

    @Test
    fun testToString() {
        val expected = "Subfolders: [Subfolder: subfolderName]\n" +
                "Media: [Media{id='id', name='aname', fileId='afileid', fileExtension='.kt', filePath='FolderElementsTest', owner=kotlin, tags=[]}]"
        assertEquals(expected, folder.toString())
    }

    @Test
    fun twoSameFolderHashCode_ExpectEqualsTrue() {
        assertEquals(FolderElements(subfolders, medias).hashCode(), folder.hashCode())
    }

    @Test
    fun twoSameFolderEquals_ExpectEqualsTrue() {
        assertEquals(FolderElements(subfolders, medias), folder)
    }
}
