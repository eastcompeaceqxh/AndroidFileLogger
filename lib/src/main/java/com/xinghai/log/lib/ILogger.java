package com.xinghai.log.lib;

public interface ILogger {

    void info(String tag, String msg);

    void debug(String tag, String msg);

    void warn(String tag, String msg);

    void error(String tag, String msg);
}
