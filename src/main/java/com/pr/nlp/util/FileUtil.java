package com.pr.nlp.util;


import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class FileUtil {

    public static boolean bExistFile(String sFile) {
        boolean rtn = false;
        File file = new File(sFile);
        if (file.exists()) {
            rtn = true;
        }
        return rtn;
    }

    public static boolean copy(InputStream is, OutputStream os) {
        boolean rtn = false;
        if (is != null && os != null) {
            try {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            } catch (IOException e) {
                rtn = false;
                LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
            }
        }
        return rtn;
    }

    public static boolean copyFile(String sFFrom, String sFTo, boolean bOverwrite) {
        boolean rtn = false;
        File fFrom = new File(sFFrom), fTo = new File(sFTo);
        if (bOverwrite || !fTo.exists()) {
            try {
                InputStream is = new FileInputStream(fFrom);
                OutputStream os = new FileOutputStream(fTo);
                rtn = copy(is, os);
            } catch (IOException e) {
                rtn = false;
                LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);            }
        }
        return rtn;
    }

    public static boolean copyFile(InputStream is, String sFTo, boolean bOverwrite) {
        boolean rtn = false;
        File fTo = new File(sFTo);
        if (is != null && bOverwrite || !fTo.exists()) {
            try {
                OutputStream os = new FileOutputStream(fTo);
                rtn = copy(is, os);
            } catch (IOException e) {
                rtn = false;
                LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
            }
        }
        return rtn;
    }

    public static boolean copyFileFromClassPath(Class tClass, String sFFrom, String sFTo, boolean bOverwrite) {
        boolean rtn = false;

        if (tClass != null && sFFrom != null && sFTo != null) {
            InputStream isFrom = tClass.getResourceAsStream(sFFrom);
            rtn = copyFile(isFrom, sFTo, bOverwrite);
        }

        return rtn;
    }

    public static boolean copyFileFromClassPath(Object tObj, String sFFrom, String sFTo, boolean bOverwrite) {
        return copyFileFromClassPath(tObj.getClass(), sFFrom, sFTo, bOverwrite);
    }

    /**
     * Delete file if it exists.
     *
     * @param sFile
     * @return
     */
    public static boolean deleteFile(String sFile) {
        boolean rtn = false;

        if (sFile != null) {
            File file = new File(sFile);
            if (file.exists()) {
                rtn = file.delete();
            }
        }

        return rtn;
    }

    public static int deleleFiles(String regex, String path) {
        int rtn = 0;

        if (path != null && regex != null) {
            File folder = new File(path);
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                String fname = files[i].getName();
                if (fname.matches(regex)) {
                    if (files[i].delete()) {
                        rtn++;
                    }
                }
            }
        }

        return rtn;
    }

    public static int deleleFiles(String regex) {
        return deleleFiles(regex, ".");
    }

    /**
     * @param sFile when sFile==null this function will not create files and will
     *              return null;
     * @return
     */
    public static File createIfNotExist(String sFile) {
        File rtn = null;
        if (sFile != null) {
            rtn = new File(sFile);
            if (!rtn.exists()) {
                try {
                    rtn.createNewFile();
                } catch (IOException e) {
                    rtn = null;
                    LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
                }
            }
        }
        return rtn;
    }

    /**
     * @param sFile
     * @param bAppend
     * @return null if sFile==null;
     */
    public static FileWriter createFileWriter(String sFile, boolean bAppend) {
        FileWriter rtn = null;
        if (sFile != null) {
            try {
                rtn = new FileWriter(createIfNotExist(sFile), bAppend);
            } catch (IOException e) {
                LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
            }
        }
        return rtn;
    }

    /**
     * @param sFile
     * @return null if sFile==null;
     */
    public static FileWriter createFileWriter(String sFile) {
        return createFileWriter(sFile, true);
    }

    /**
     * Flush and close fw if fw!= null.
     *
     * @param fw
     * @return
     */
    public static boolean close(FileWriter fw) {
        boolean rtn = false;
        if (fw != null) {
            rtn = true;
            try {
                fw.flush();
                fw.close();
            } catch (IOException e) {
                LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
            }
        }
        return rtn;
    }

    public static boolean flush(FileWriter fw) {
        boolean rtn = false;
        if (fw != null) {
            rtn = true;
            try {
                fw.flush();
            } catch (IOException e) {
                rtn = false;
                e.printStackTrace();
            }
        }
        return rtn;
    }

    public static boolean insertFile(String sFile, long offset, byte[] content) {
        boolean rtn = false;
        try {
            rtn = true;
            String sFTemp = sFile + "~";
            RandomAccessFile raf;
            raf = new RandomAccessFile(new File(sFile), "rw");
            long fileSize = raf.length();
            if (fileSize >= offset) {
                RandomAccessFile rafTemp = new RandomAccessFile(new File(sFTemp), "rw");
                FileChannel fc = raf.getChannel();
                FileChannel fcTemp = rafTemp.getChannel();
                fc.transferTo(offset, (fileSize - offset), fcTemp);
                fc.truncate(offset);
                raf.seek(offset);
                raf.write(content);
                long newOffset = raf.getFilePointer();
                fcTemp.position(0L);
                fc.transferFrom(fcTemp, newOffset, (fileSize - offset));
                fc.close();
                fcTemp.close();
                raf.close();
                rafTemp.close();
                deleteFile(sFTemp);
            } else {
                LogUtil.getInstance().printLog("Can not insert into a file: offset>filesize!", LogUtil.LEVEL.ERROR);
                rtn = false;
            }
        } catch (FileNotFoundException e) {
            rtn = false;
            LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
        } catch (IOException e) {
            rtn = false;
            LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
        }
        return rtn;
    }

    public static boolean insertFileHead(String sFile, byte[] content) {
        return insertFile(sFile, 0L, content);
    }

    public static boolean insertFileHead(String sFile, String string) {
        return insertFileHead(sFile, string.getBytes(Charset.forName("UTF-8")));
    }

    public static boolean insertFileTail(String sFile, byte[] content) {
        boolean rtn = false;
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(sFile), "r");
            rtn = insertFile(sFile, raf.length(), content);
            raf.close();
        } catch (FileNotFoundException e) {
            rtn = false;
            LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
        } catch (IOException e) {
            rtn = false;
            LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
        }
        return rtn;
    }

    public static boolean insertFileTail(String sFile, String string) {
        return insertFileTail(sFile, string.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Append str to fw.
     *
     * @param fw
     * @param str
     * @return true if fw!=null and str!=null
     */
    public static boolean append(FileWriter fw, String str) {
        boolean rtn = false;
        if (fw != null && str != null) {
            rtn = true;
            try {
                fw.append(str);
            } catch (IOException e) {
                LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
            }
        }
        return rtn;
    }

    public static boolean appendFile(String sFile, String str) {
        boolean rtn = false;
        FileWriter fw = createFileWriter(sFile, true);
        rtn = append(fw, str);
        close(fw);
        return rtn;
    }

    public static ArrayList<String> readFileByLine(String sFile) {
        ArrayList<String> result = new ArrayList<>();

        if (sFile == null) return result;
        File file = new File(sFile);
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                result.add(line.trim());
            }
        } catch (Exception e) {
            LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
        } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {
                        LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
                    }
                }
                if (fileReader != null)  {
                    try {
                        fileReader.close();
                    } catch (Exception e) {
                        LogUtil.getInstance().printLog(e.getMessage(), LogUtil.LEVEL.ERROR);
                    }
                }

            return result;
        }

    }


    /*
     * 函数名：getFile
     * 作用：使用递归，输出指定文件夹内的所有文件
     * 参数：path：文件夹路径   deep：表示文件的层次深度，控制前置空格的个数
     * 前置空格缩进，显示文件层次结构
     */
    public static ArrayList<String> getFiles(String path){
        ArrayList<String> result = new ArrayList<>();

        File file = new File(path);
        File[] array = file.listFiles();

        for(int i=0;i<array.length;i++) {
            if(array[i].isFile()) {
                result.add(array[i].getAbsolutePath());
            }
            else if(array[i].isDirectory()) {
                result.addAll(getFiles(array[i].getPath()));
            }
        }

        return result;
    }

    public static ArrayList<String> getFiles(String path, String p) {
        Pattern pattern = Pattern.compile(p);

        ArrayList<String> result = new ArrayList<>();

        File file = new File(path);
        File[] array = file.listFiles();

        for(int i=0;i<array.length;i++) {
            if(array[i].isFile() && pattern.matcher(array[i].getName()).find()) {
                result.add(array[i].getAbsolutePath());
            }
            else if(array[i].isDirectory()) {
                result.addAll(getFiles(array[i].getPath()));
            }
        }

        return result;
    }

}
