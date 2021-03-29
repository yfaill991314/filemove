package com.funi.filemove.utils;

import com.funi.filemove.Constants;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName Utils
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/1/19 20:49
 * @Version 1.0
 */
public class MyUtils {

    public static String getDataSouceNameByCode(String dataSourceCode) {
        List<Map<String, String>> dataSourceList = Constants.dataSourceList;
        for (Map<String, String> dataSource : dataSourceList) {
            if (dataSource.get("dataSource").equals(dataSourceCode)) {
                return dataSource.get("dataSourceName");
            }
        }
        return null;
    }

    public static String getUuid36() {
        return UUID.randomUUID().toString();
    }

    public static String getFileExe(String imgstyle, String property) throws Exception {
        String fileExe;
        if (imgstyle == null) {
            fileExe = getFileExeByProperty(property);
            if (fileExe == null) {
                return "dwg";
            } else {
                return fileExe;
            }
        } else {
            fileExe = getFileExeByStr(imgstyle);
            if (fileExe == null) {
                fileExe = getFileExeByProperty(property);
                if (fileExe == null) {
                    return "dwg";
                } else {
                    return fileExe;
                }
            } else {
                return fileExe;
            }
        }
    }

    public static String getFileExeByProperty(String property) throws Exception {
        String fileExe;
        if (property == null) {
            return null;
        }
        fileExe = getFileExeByStr(property);
        if (fileExe == null) {
            return null;
        }
        return fileExe;
    }

    public static String getFileExeByStr(String fileNameStr) throws Exception {
        if (fileNameStr == null) {
            throw new Exception("文件名称不能为null");
        }
        fileNameStr = fileNameStr.toLowerCase();

        if (fileNameStr.endsWith("jpg")) {
            return "jpg";
        }

        if (fileNameStr.endsWith("jpeg")) {
            return "jpeg";
        }

        if (fileNameStr.endsWith("png")) {
            return "png";
        }

        if (fileNameStr.endsWith("cad")) {
            return "cad";
        }

        if (fileNameStr.endsWith("dxf")) {
            return "dxf";
        }

        if (fileNameStr.endsWith("bpm")) {
            return "bpm";
        }

        if (fileNameStr.endsWith("pdf")) {
            return "pdf";
        }

        if (fileNameStr.endsWith("zip")) {
            return "zip";
        }

        if (fileNameStr.endsWith("7z")) {
            return "7z";
        }

        if (fileNameStr.endsWith("dwg")) {
            return "dwg";
        }

        return null;
    }
}
