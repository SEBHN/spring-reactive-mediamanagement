package de.hhn.mvs.rest;

import org.junit.Test;

import javax.mail.Folder;

import static org.junit.Assert.assertEquals;

public class FolderUtilsTest {

    @Test
    public void parseFolderPathTest(){
        String original = "path";
        assertEquals("/path/", FolderUtils.parseFolderPathFormat(original));
        original = "hello/world";
        assertEquals("/hello/world/", FolderUtils.parseFolderPathFormat(original));
        original = "/hello/world/";
        assertEquals("/hello/world/", FolderUtils.parseFolderPathFormat(original));
        original = "";
        assertEquals("/", FolderUtils.parseFolderPathFormat(original));



    }
}
