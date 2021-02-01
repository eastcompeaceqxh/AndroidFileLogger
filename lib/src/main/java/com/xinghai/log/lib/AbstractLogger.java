package com.xinghai.log.lib;


import com.xinghai.log.lib.clean.CompressIntercept;
import com.xinghai.log.lib.clean.MaxKeepFilesIntercept;
import com.xinghai.log.lib.clean.MaxSizeIntercept;
import com.xinghai.log.lib.engine.Level;
import com.xinghai.log.lib.engine.LogItem;
import com.xinghai.log.lib.format.IMessageProcessorFactory;
import com.xinghai.log.lib.format.LogMessageProcessor;
import com.xinghai.log.lib.format.Processor;
import com.xinghai.log.lib.intercept.FileIntercept;
import com.xinghai.log.lib.intercept.IFileInterceptFactory;
import com.xinghai.log.lib.intercept.LogIntercept;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractLogger implements ILogger{

    private static LogIntercept logIntercept;

    private static IMessageProcessorFactory messageProcessorFactory;

    private static IFileInterceptFactory fileInterceptFactory;

    private static int maxZipFilesInKeep;

    private static long maxLogFileSize;

    private static String folderPath;

    private static boolean isDebug;

    protected static boolean hasInit;

    private static void init(Builder builder) {
        maxZipFilesInKeep = builder.maxZipFilesInKeep;
        maxLogFileSize = builder.maxLogFileSize;
        folderPath = builder.folderPath;
        isDebug = builder.isDebug;
        logIntercept = builder.logIntercept;
        messageProcessorFactory = builder.messageProcessorFactory;
        fileInterceptFactory = builder.fileInterceptFactory;
        hasInit = true;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static LogIntercept getLogIntercept() {
        return logIntercept;
    }

    public static IMessageProcessorFactory getMessageProcessorFactory() {
        return messageProcessorFactory;
    }

    public static IFileInterceptFactory getFileInterceptFactory() {
        return fileInterceptFactory;
    }

    public static int getMaxZipFilesInKeep() {
        return maxZipFilesInKeep;
    }

    public static long getMaxLogFileSize() {
        return maxLogFileSize;
    }

    public static String getFolderPath() {
        return folderPath;
    }

    public static class Builder {

        private static final int DEFAULT_MAX_LOG_FILE_SIZE = 1024 * 1024;

        private static final int DEFAULT_MAX_ZIP_FILE_KEEP = 3;

        private LogIntercept logIntercept;

        private IMessageProcessorFactory messageProcessorFactory;

        private IFileInterceptFactory fileInterceptFactory;

        private int maxZipFilesInKeep;

        private long maxLogFileSize;

        private String folderPath;

        private boolean isDebug;

        public Builder setLogIntercept(LogIntercept logIntercept) {
            this.logIntercept = logIntercept;
            return this;
        }

        public Builder setFileInterceptFactory(IFileInterceptFactory fileInterceptFactory) {
            this.fileInterceptFactory = fileInterceptFactory;
            return this;
        }

        public Builder setMessageProcessorFactory(IMessageProcessorFactory messageProcessorFactory) {
            this.messageProcessorFactory = messageProcessorFactory;
            return this;
        }

        public Builder setMaxZipFilesInKeep(int maxZipFilesInKeep) {
            this.maxZipFilesInKeep = maxZipFilesInKeep;
            return this;
        }

        public Builder setMaxLogFileSize(long maxLogFileSize) {
            this.maxLogFileSize = maxLogFileSize;
            return this;
        }

        public Builder setFolderPath(String folderPath) {
            this.folderPath = folderPath;
            return this;
        }

        public Builder setDebug(boolean debug) {
            isDebug = debug;
            return this;
        }

        public void build() {
            if (folderPath == null || folderPath.length() == 0) {
                throw new IllegalArgumentException("folderPath can not be empty!");
            }

            if (logIntercept == null) {
                logIntercept = new DefaultLogIntercept();
            }

            if (messageProcessorFactory == null) {
                messageProcessorFactory = new DefaultMessageProcessorFactory();
            }

            if (fileInterceptFactory == null) {
                fileInterceptFactory = new DefaultFileInterceptFactory();
            }

            if (maxLogFileSize == 0) {
                maxLogFileSize = DEFAULT_MAX_LOG_FILE_SIZE;
            }

            if (maxZipFilesInKeep == 0) {
                maxZipFilesInKeep = DEFAULT_MAX_ZIP_FILE_KEEP;
            }
            AbstractLogger.init(this);
        }
    }

    static class DefaultMessageProcessorFactory implements IMessageProcessorFactory {

        @Override
        public Processor<String, LogItem> createMessageProcessor() {
            return new LogMessageProcessor();
        }
    }

    static class DefaultFileInterceptFactory implements IFileInterceptFactory {

        @Override
        public List<FileIntercept> createFileIntercepts() {
            List<FileIntercept> list = new ArrayList<>();
            list.add(new MaxSizeIntercept());
            list.add(new CompressIntercept());
            list.add(new MaxKeepFilesIntercept());
            return list;
        }
    }

    static class DefaultLogIntercept implements LogIntercept {

        @Override
        public void intercept(int level, String tag, String logMsg) {
            if (AppLogger.isDebug())
                switch (level) {
                    case Level.DEBUG:
                        android.util.Log.d(tag, logMsg);
                        break;
                    case Level.INFO:
                        android.util.Log.i(tag, logMsg);
                        break;
                    case Level.WARN:
                        android.util.Log.w(tag, logMsg);
                        break;
                    case Level.ERROR:
                        android.util.Log.e(tag, logMsg);
                        break;
                    default:
                        break;
                }
        }
    }
}
