package com.xinghai.log.lib.clean;


import com.xinghai.log.lib.intercept.FileIntercept;

abstract class CleanIntercept implements FileIntercept {

    public String getUniqueFileId(String originFileName) {
        return originFileName + originFileName.hashCode() * 0x33;
    }

    public String getBackupName(String originFileName) {
        return getUniqueFileId(originFileName) + "_backup";
    }

    public String generateTempCompressingFileName(String originFileName) {
        return getUniqueFileId(originFileName) + "_" + System.currentTimeMillis();
    }

    public String generateZipFileName(String originFileName) {
        return originFileName + ".zip";
    }

}
