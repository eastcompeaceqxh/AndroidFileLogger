package com.xinghai.fileLog;

import android.app.Application;

import com.xinghai.log.annotation.LogFilesDefine;
import com.xinghai.log.lib.AppLogger;
import com.xinghai.log.lib.AppLoggerUtil;

import java.io.File;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @LogFilesDefine(defaultLogFile = "appDefault", value = {"media", "user"})
    @Override
    public void onCreate() {
        super.onCreate();
        new AppLogger.Builder()
                .setDebug(true)
                .setFolderPath(getFilesDir() + File.separator + "logs" + File.separator)
                .setMaxLogFileSize(1024 * 1024)
                .setMaxZipFilesInKeep(3)
                .build();

        AppLoggerUtil.info(TAG, "onCreate");  // this will write to file "appDefault"
        AppLoggerUtil.mediaLogger().info(TAG, "onCreate"); // this will write to file "media"
        AppLoggerUtil.userLogger().info(TAG, "onCreate");  // this will write to file "user"
    }
}
