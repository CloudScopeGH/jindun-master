package com.cloudspace.jindun.utils;

import com.cloudspace.jindun.log.APPLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public final class ReportWritter {
    private static List<String> publishRecord = new ArrayList<>();
    private static List<String> subscribeRecord = new ArrayList<>();

    public static void addPublishRecord(String aRecord) {
        publishRecord.add(aRecord);
    }

    public static void addSubscribeRecord(String aRecord) {
        subscribeRecord.add(aRecord);
    }

    private static void flushPublishFile() {
        RandomAccessFile recordWriter = null;
        try {
            File rootDir = Utility.getLogDir();
            if (rootDir != null) {
                File recordFile = new File(rootDir, "publish_stat.log");
                if (!recordFile.exists()) {
                    recordFile.createNewFile();
                }
                recordWriter = new RandomAccessFile(recordFile, "rw");
                String content = publishRecord.toString().trim();
                long length = recordWriter.length();
                APPLog.i("rtmp report :origin file legth " + length);
                if (length > 0) {
                    recordWriter.seek(length - 1);
                    recordWriter.writeBytes(",");
                    recordWriter.writeBytes(content.substring(1));
                } else {
                    recordWriter.writeBytes(content.toString());
                }
                publishRecord.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (recordWriter != null) {
                try {
                    recordWriter.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static void flushSubscribeFile() {
        RandomAccessFile recordWriter = null;
        try {
            File rootDir = Utility.getLogDir();
            if (rootDir != null) {
                File recordFile = new File(rootDir, "subscribe_stat.log");
                if (!recordFile.exists()) {
                    recordFile.createNewFile();
                }
                recordWriter = new RandomAccessFile(recordFile, "rw");
                String content = subscribeRecord.toString().trim();
                long length = recordWriter.length();
                APPLog.i("rtmp report :origin file legth " + length);
                if (length > 0) {
                    recordWriter.seek(length - 1);
                    recordWriter.writeBytes(",");
                    recordWriter.writeBytes(content.substring(1));
                } else {
                    recordWriter.writeBytes(content.toString());
                }

                subscribeRecord.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (recordWriter != null) {
                try {
                    recordWriter.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

}
