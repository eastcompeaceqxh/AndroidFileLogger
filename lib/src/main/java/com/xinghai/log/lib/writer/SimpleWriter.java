package com.xinghai.log.lib.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class SimpleWriter {

    private String folderPath;

    private String fileName;

    BufferedWriter writer;

    SimpleWriter(String folderPath, String fileName) {
        this.folderPath = folderPath;
        this.fileName = fileName;
    }

    void write(String log) {
        File logFile = ensureFile();
        if (logFile == null) {
            return;
        }

        if (writer == null) {
            try {
                writer = new BufferedWriter(new FileWriter(logFile, true));
            } catch (IOException e) {
                e.printStackTrace();
                close();
                return;
            }
        }

        try {
            writer.write(log);
            writer.newLine();
            writer.flush();
        } catch (IOException ignore) {

        }
    }

    File ensureFile() {
        File file = new File(folderPath, fileName);
        if (!file.exists()) {
            if (!createFolder(folderPath)) {
                return null;
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writer = null;
    }

    static boolean createFolder(String folderPath) {
        File folderFile = new File(folderPath);
        if (!folderFile.exists()) {
            synchronized (SimpleWriter.class) {
                if (!folderFile.exists() && !folderFile.mkdirs()) {
                    return false;
                }
            }
        }
        return true;
    }

}
