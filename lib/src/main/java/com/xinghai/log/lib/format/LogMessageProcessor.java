package com.xinghai.log.lib.format;


import com.xinghai.log.lib.engine.Level;
import com.xinghai.log.lib.engine.LogItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogMessageProcessor implements Processor<String, LogItem>{

    private DateProcessor dateProcessor;

    private TagProcessor tagProcessor;

    private LevelProcessor levelProcessor;

    private MessageProcessor messageProcessor;

    public LogMessageProcessor() {
        dateProcessor = new DateProcessor();
        tagProcessor = new TagProcessor();
        levelProcessor = new LevelProcessor();
        messageProcessor = new MessageProcessor();
    }

    @Override
    public String process(LogItem logItem) {
        String date = dateProcessor.process(logItem);
        String tag = tagProcessor.process(logItem);
        String level = levelProcessor.process(logItem);
        String message = messageProcessor.process(logItem);
        return date + "  "
                + level + "  "
                + tag + "  "
                + message;
    }


    static class DateProcessor implements Processor<String, LogItem> {

        private Date mDate;

        ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINESE);
            }
        };

        @Override
        public String process(LogItem logItem) {
            if (mDate == null) {
                mDate = new Date();
            }
            mDate.setTime(logItem.timeMills);
            SimpleDateFormat dateFormat = threadLocal.get();
            if (dateFormat != null) {
                return dateFormat.format(mDate);
            }
            return "";
        }
    }

    static class LevelProcessor implements Processor<String, LogItem> {

        @Override
        public String process(LogItem logItem) {
            switch (logItem.level) {
                case Level.DEBUG:
                    return "<DEBUG>";
                case Level.INFO:
                    return "<INFO>";
                case Level.WARN:
                    return "<WARN>";
                case Level.ERROR:
                    return "<ERROR>";
                default:
                    return "<unKnow>";
            }
        }
    }

    static class TagProcessor implements Processor<String, LogItem> {

        @Override
        public String process(LogItem logItem) {
            return logItem.tag;
        }
    }

    static class MessageProcessor implements Processor<String, LogItem> {

        @Override
        public String process(LogItem logItem) {
            return logItem.msg;
        }
    }

}
