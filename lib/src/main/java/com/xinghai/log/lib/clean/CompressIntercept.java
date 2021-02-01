package com.xinghai.log.lib.clean;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 fileName需要带时间戳

 1、检查是否有fileName_backup的文件存在，这类文件都是需要压缩的
 3、进行压缩，生成一个临时文件，以fileName_inCompressing结尾
 4、压缩完成后，删除对应的fileName_beforeCompress fileName_inCompressing，并
 修改为正式的压缩文件名称。

 意外情况：如果正在压缩时，线程被异常终止，如何判断
 1、在该intercept初始化时，或者压缩线程启动时，先检查下是否还有fileName_beforeCompress
 fileName_inCompressing存在。
 2、如果存在，删除fileName_inCompressing，将fileName_beforeCompress加入队列中，开始压缩工作

 */
public class CompressIntercept extends CleanIntercept{

    private static BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(5);

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            1, 5, 5, TimeUnit.SECONDS, queue,
            new ThreadFactory() {
                AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    int index = count.getAndIncrement();
                    Log.e("CompressIntercept", "new Thread index =" + index);
                    Thread thread = new Thread(r, "Thread-CompressIntercept : " + index);
                    thread.setDaemon(false);
                    return thread;
                }
            }
    );

    private boolean isStart = false;

    @Override
    public boolean intercept(String folderPath, String fileName, String logMsg) {
        if (!isStart) {
            isStart = true;
            checkUnCompletedCompressOnStart(folderPath, fileName);
        }
        checkNeedCompress(folderPath, fileName);
        return false;
    }

    // appinfo.txt_backup_inCompressing

    /**
     * 检查是否有需要压缩的文件
     * @param folderPath
     * @param fileName
     * @return
     */
    private void checkNeedCompress(String folderPath, String fileName) {
        File folder = new File(folderPath);
        final File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().endsWith(getBackupName(fileName))) {
                File inCompressingFile = new File(folderPath, generateTempCompressingFileName(fileName));
                file.renameTo(inCompressingFile);
                file.delete();
                enqueueItem(folderPath, inCompressingFile.getName(), generateZipFileName(inCompressingFile.getName()));
            }
        }
    }

    /**
     * 启动时检查是否有未压缩完成的文件
     * @param folderPath
     * @param fileName
     */
    private void checkUnCompletedCompressOnStart(String folderPath, String fileName) {
        File folder = new File(folderPath);
        final File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(getUniqueFileId(fileName)) &&
                        !file.getName().endsWith(getBackupName(fileName)) &&
                        !file.getName().endsWith(".zip")) {
                    // 正在压缩的文件
                    File unCompleted = new File(folder, file.getName() + ".zip");
                    if (unCompleted.exists()) {
                        unCompleted.delete();
                    }
                    enqueueItem(folderPath, file.getName(), file.getName() + ".zip");
                }
            }
        }
    }

    private void enqueueItem(String folderPath, String srcFileName, String targetFileName) {
        Log.e("CompressIntercept", "srcFileName = " + srcFileName + ", targetFileName = " + targetFileName);
        CompressItem item = new CompressItem();
        item.folder = folderPath;
        item.srcFileName = srcFileName;
        item.targetName = targetFileName;
        threadPoolExecutor.execute(new CompressTask(item));
    }

    static class CompressTask implements Runnable {

        private CompressItem compressItem;

        public CompressTask(CompressItem item) {
            this.compressItem = item;
        }

        @Override
        public void run() {
                try {
                    Log.e("CompressTask", "thread = " + Thread.currentThread().getName() + ", srcFileName = " + compressItem.srcFileName + ", targetFileName = " + compressItem.targetName);
                    File file = new File(compressItem.folder + compressItem.srcFileName);
                    ZipOutputStream zipOutputSteam = new ZipOutputStream(new FileOutputStream(new File(compressItem.folder, compressItem.targetName)));
                    if (file.isFile()) {
                        ZipEntry zipEntry = new ZipEntry(compressItem.srcFileName);
                        FileInputStream inputStream = new FileInputStream(file);
                        zipOutputSteam.putNextEntry(zipEntry);
                        int len;
                        byte[] buffer = new byte[4096];
                        while ((len = inputStream.read(buffer)) != -1) {
                            zipOutputSteam.write(buffer, 0, len);
                        }
                        zipOutputSteam.closeEntry();
                        file.delete();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    static class CompressItem {
        String folder;
        String srcFileName;
        String targetName;
    }

}
