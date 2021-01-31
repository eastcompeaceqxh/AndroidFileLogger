package com.xinghai.log.lib.writer;


import com.xinghai.log.lib.engine.LogItem;
import com.xinghai.log.lib.format.Processor;
import com.xinghai.log.lib.intercept.FileIntercept;

import java.util.List;

public class FileLogger implements ILogger {

    private String folderPath;

    private String fileName;

    private SimpleWriter simpleWriter;

    private List<FileIntercept> fileInterceptList;

    private Processor<String, LogItem> messageProcessor;

    private String printMsg = "";

    private boolean resetFileWriter = false;

    public FileLogger(String folderPath, String fileName) {
        this.folderPath = folderPath;
        this.fileName = fileName;
        simpleWriter = new SimpleWriter(folderPath, fileName);
    }

    public void setMessageProcessor(Processor<String, LogItem> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public void setFileInterceptList(List<FileIntercept> fileInterceptList) {
        this.fileInterceptList = fileInterceptList;
    }

    @Override
    public void log(LogItem item) {
        // 生成写入文件的日志信息
        if (messageProcessor != null) {
            printMsg = messageProcessor.process(item);
        } else {
            printMsg = item.tag + " " + item.msg;
        }

        // 老化机制
        if (fileInterceptList != null && fileInterceptList.size() > 0) {
            for (FileIntercept intercept : fileInterceptList) {
                boolean fileSourceChanged = intercept.intercept(folderPath, fileName, printMsg);
                if (fileSourceChanged) {
                    // MaxSizeIntercept会删除日志文件，需要重新打开输入流
                    resetFileWriter = true;
                }
            }
        }

        if (resetFileWriter) {
            simpleWriter.close();
            resetFileWriter = false;
        }

        // 写入文件
        simpleWriter.write(printMsg);
    }

}
