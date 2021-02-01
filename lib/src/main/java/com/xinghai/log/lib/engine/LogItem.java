package com.xinghai.log.lib.engine;

public class LogItem {
    public long timeMills;
    public int level;
    public String tag;
    public String msg;

    public LogItem(long timeMills, int level, String tag, String msg) {
        this.timeMills = timeMills;
        this.level = level;
        this.tag = tag;
        this.msg = msg;
    }
}
