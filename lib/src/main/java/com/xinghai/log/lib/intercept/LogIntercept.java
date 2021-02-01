package com.xinghai.log.lib.intercept;

import com.xinghai.log.lib.engine.Level;

public interface LogIntercept {
    void intercept(@Level int level, String tag, String logMsg);
}
