package com.iflytek.ossp.commonutils;

import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author weigao
 * @since 16/2/22
 */
public class ReadFile {

    /**
     * 读取文件；文件读入到Properties对象中
     *
     * @param file 需要读取的文件
     * @return Properties
     */
    public static Properties readProperties(String file) {
        Properties prop = new Properties();
        File f = new File(file);
        if (!f.exists()) {
            return null;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            prop.load(in);
            return prop;
        } catch (Exception e) {
            LoggerFactory.getLogger(ReadFile.class).error("can't get file :" + file, e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(ReadFile.class).error("can't close FileInputStream", e);
            }
        }
    }

    /**
     * 一次性读取整个文件
     *
     * @param file File
     * @param encoding 编码
     * @return String
     */
    public static String readAll(String file, String encoding) {
        File f = new File(file);
        byte[] fileBuff = new byte[(int) f.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            in.read(fileBuff);
            return new String(fileBuff, encoding);//"EncodingGuess.getEncoding(fileBuff));
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 一次性读取整个文件
     *
     * @param file file
     * @return bytes
     */
    public static byte[] readAllByte(String file) {
        File f = new File(file);
        byte[] fileBuff = new byte[(int) f.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            in.read(fileBuff);
            return fileBuff;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 按行读取文件
     *
     * @param file File
     * @param encoding 编码
     * @param skipNum 跳过行数
     * @param readNum 读取行数
     * @return List
     */
    public static List<String> getAllLine2Array(String file, String encoding, int skipNum, int readNum) {
        ArrayList<String> res = new ArrayList<String>();
        BufferedReader readConf = null;
        FileInputStream in = null;
        InputStreamReader redar = null;
        try {
            in = new FileInputStream(file);
            redar = new InputStreamReader(in, encoding);
            readConf = new BufferedReader(redar);
            String line;
            int lineNum = 0;
            int rnum = 0;
            while (null != (line = readConf.readLine())) {
                if (!line.startsWith("#") && !"".equals(line.trim())) {
                    lineNum++;
                    if (lineNum > skipNum) {
                        if (readNum == -1 || rnum < readNum) {
                            rnum++;
                            res.add(line);
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ReadFile.class).error(e.getMessage(), e);
        } finally {
            try {
                if (readConf != null) {
                    readConf.close();
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(ReadFile.class).error(e.getMessage(), e);
            }
            try {
                if (redar != null) {
                    redar.close();
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(ReadFile.class).error(e.getMessage(), e);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(ReadFile.class).error(e.getMessage(), e);
            }
        }
        return res;
    }

    /**
     * 读取所有行到List中
     *
     * @param file File
     * @return List
     */
    public static List<String> getAllLine2Array(String file) {
        return getAllLine2Array(file, "utf-8", 0, -1);
    }

}
