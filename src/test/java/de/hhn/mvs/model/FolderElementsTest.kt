package de.hhn.mvs.model

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FolderElementsTest {

    private var folder: FolderElements? = null

    @Before
    fun setUp() {
        val subfolder = Subfolder("subfolderName")
        val media: Media = MediaImpl("id", "aname", "afileid", ".kt", "FolderElementsTest", "kotlin")
        val subfolders = mutableListOf(subfolder)
        val medias = mutableListOf(media)
        folder = FolderElements(subfolders, medias)
    }

    @Test
    fun testToString() {
        val expected = "Subfolders: [Subfolder: subfolderName]\n" +
                "Media: [Media{id='id', name='aname', fileId='afileid', fileExtension='.kt', filePath='FolderElementsTest', owner=kotlin, tags=[]}]"
        assertEquals(expected, folder.toString())
    }
}
