package com.xinghai.log.lib.clean;


import com.xinghai.log.lib.AppLogger;

import java.io.File;
import java.io.IOException;

public class MaxSizeIntercept extends CleanIntercept{

    private long MAX_FILE_SIZE;

    public MaxSizeIntercept() {
        MAX_FILE_SIZE = AppLogger.getMaxLogFileSize();
    }

    @Override
    public boolean intercept(String folderPath, String fileName, String logMsg) {
        File file = new File(folderPath, fileName);
        if (!file.exists()) {
            return false;
        }

        long size = file.length();
        boolean needIntercept =  size >= MAX_FILE_SIZE || size + logMsg.length() >= MAX_FILE_SIZE;
        if (needIntercept) {
            file.renameTo(new File(folderPath, getBackupName(fileName)));
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return needIntercept;
    }
}
