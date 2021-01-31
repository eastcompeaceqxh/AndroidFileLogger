package com.xinghai.log.lib.engine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({Level.INFO,
        Level.DEBUG,
        Level.WARN,
        Level.ERROR})
@Retention(RetentionPolicy.SOURCE)
public @interface Level {
    int INFO = 1;
    int DEBUG = 2;
    int WARN = 3;
    int ERROR = 4;
}
