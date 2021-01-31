package com.xinghai.log.lib.clean;


import com.xinghai.log.lib.AppLogger;

import java.io.File;

/*
 检查压缩文件的个数，超过最大值的话，删除最早生成的一个
 */
public class MaxKeepFilesIntercept extends CleanIntercept{

    private int MAX_KEEP_FILES;

    public MaxKeepFilesIntercept() {
        MAX_KEEP_FILES = AppLogger.getMaxZipFilesInKeep();
    }

    @Override
    public boolean intercept(String folderPath, String fileName, String logMsg) {
        File folderFile = new File(folderPath);
        final File[] files = folderFile.listFiles();
        int number = 0;
        long lastModified = System.currentTimeMillis();
        File earliestFile = null;
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(getUniqueFileId(fileName)) && file.getName().endsWith(".zip")) {
                    // 该日志的压缩文件
                    number ++;
                    if (file.lastModified() < lastModified) {
                        lastModified = file.lastModified();
                        earliestFile = file;
                    }
                }
            }
            if (number > MAX_KEEP_FILES) {
                earliestFile.delete();
            }
        }
        return false;
    }
}
