package com.xinghai.log.lib.intercept;

import com.example.filelogger.engine.Level;

public interface LogIntercept {
    void intercept(@Level int level, String tag, String logMsg);
}
