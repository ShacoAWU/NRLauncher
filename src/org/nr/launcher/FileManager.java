package org.nr.launcher;

import java.io.File;

final class FileManager {

    private static final File nrDir = new File(normalize(System.getProperty("user.home")) + ".nr2", File.separator);

    static {
        if (!nrDir.exists()) {
            nrDir.mkdir();
        }
    }

    private static String normalize(String path) {
        return path + File.separator;
    }

    protected static String getDataDirPath() {
        File dataDir = new File(normalize(nrDir.getPath()) + normalize("data"));
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        return normalize(dataDir.getPath());
    }

}