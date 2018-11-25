package de.hhn.mvs.rest;

class FolderUtils {

    static final String SLASH = "/";

    /**
     * parse to required format: /foo/bar/
     *
     * @return path in correct format
     */
    static String parseFolderPathFormat(String folderPath) {
        if (folderPath == null || folderPath.isEmpty())
            return SLASH;
        String newPath = folderPath;
        if (!folderPath.endsWith(SLASH))
            newPath = newPath + SLASH;
        if (newPath.length() > 1 && !newPath.startsWith(SLASH))
            newPath = SLASH + newPath;
        return newPath;
    }
}
