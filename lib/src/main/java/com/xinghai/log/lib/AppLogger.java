package com.xinghai.log.lib;


import com.xinghai.log.lib.engine.FileLoggerEngine;
import com.xinghai.log.lib.engine.Level;

public class AppLogger extends AbstractLogger{

    private FileLoggerEngine engine;

    AppLogger(String fileName) {
        if (!hasInit) {
            throw new IllegalStateException("please init AppLogger first!");
        }
        engine = new FileLoggerEngine(fileName);
    }

    public void info(String tag, String msg) {
        engine.enqueue(Level.INFO, tag, msg);
    }

    public void debug(String tag, String msg) {
        engine.enqueue(Level.DEBUG, tag, msg);
    }

    public void warn(String tag, String msg) {
        engine.enqueue(Level.WARN, tag, msg);
    }

    public void error(String tag, String msg) {
        engine.enqueue(Level.ERROR, tag, msg);
    }

}
