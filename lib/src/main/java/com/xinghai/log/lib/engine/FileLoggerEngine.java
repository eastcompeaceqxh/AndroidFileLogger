package com.xinghai.log.lib.engine;


import com.xinghai.log.lib.AppLogger;
import com.xinghai.log.lib.writer.FileLogger;

import java.util.concurrent.LinkedBlockingQueue;

public class FileLoggerEngine implements Runnable{

    private FileLogger fileLogger;

    private LinkedBlockingQueue<LogItem> queue;

    private volatile boolean isStart;

    public FileLoggerEngine(String fileName) {
        fileLogger = new FileLogger(AppLogger.getFolderPath(), fileName);
        fileLogger.setMessageProcessor(AppLogger.getMessageProcessorFactory().createMessageProcessor());
        fileLogger.setFileInterceptList(AppLogger.getFileInterceptFactory().createFileIntercepts());
    }

    public void enqueue(@Level int level, String tag, String msg) {
        if (AppLogger.getLogIntercept() != null) {
            AppLogger.getLogIntercept().intercept(level, tag, msg);
        }

        if (!isStart) {
            isStart = true;
            new Thread(this).start();
        }

        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
        }

        queue.offer(new LogItem(System.currentTimeMillis(),
                level, tag, msg));
    }

    @Override
    public void run() {
        while (isStart) {
            try {
                LogItem item = queue.take();
                if (item != null) {
                    fileLogger.log(item);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
