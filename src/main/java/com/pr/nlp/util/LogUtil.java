package com.pr.nlp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogUtil {

    public enum LEVEL {
        ERROR, WARN, INFO
    }

    public static String PREFIX = "person";

    public boolean isOpenLog = true;
    public boolean isWriteFile = false;

    public String fileName = "";

    private Logger log;

    public void init(String fileName, boolean isOpenLog, boolean isWriteFile) {
        this.fileName = fileName;
        this.isOpenLog = isOpenLog;
        this.isWriteFile = isWriteFile;
        if (isWriteFile && (fileName == null || fileName.isEmpty())) {
            this.isWriteFile = false;
        }

        log = LoggerFactory.getLogger(LogUtil.class);
    }

    private static LogUtil instance = new LogUtil();

    public static LogUtil getInstance() {
        return instance;
    }

    private LogUtil() {
        init("", true, false);
    }

    public void printLog(String info, LEVEL klevel) {
        if (isOpenLog && klevel.equals(LEVEL.ERROR)) log.error(getPrefix() + info);
        if (isOpenLog && klevel.equals(LEVEL.WARN)) log.warn(getPrefix() + info);
        if (isOpenLog && klevel.equals(LEVEL.INFO)) log.info(getPrefix() + info);
    }

    private String getPrefix() {
        String time = DateUtil.now();
        return "[" + time + "][" + PREFIX + "] ";
    }

    public static void setPREFIX(String PREFIX) {
        LogUtil.PREFIX = PREFIX;
    }

    public void setOpenLog(boolean openLog) {
        isOpenLog = openLog;
    }

    public void setWriteFile(boolean writeFile) {
        isWriteFile = writeFile;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private void error(String info) {
        printLog(info, LEVEL.ERROR);
    }

    private void info(String info) {
        printLog(info, LEVEL.INFO);
    }

    private void warn(String info) {
        printLog(info, LEVEL.WARN);
    }

}
