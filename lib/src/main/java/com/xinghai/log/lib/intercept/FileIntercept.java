package com.xinghai.log.lib.intercept;

public interface FileIntercept {
    /**
     * @param folderPath
     * @param fileName
     * @param logMsg
     * @return true: the origin file has been removed or recreate, we need to
     *               reset the file stream
     *         false: may intercept, but the origin file is not changed
     */
    boolean intercept(String folderPath, String fileName, String logMsg);
}
