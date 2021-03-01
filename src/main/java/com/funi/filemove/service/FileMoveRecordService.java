package com.funi.filemove.service;

import java.util.Map;

public interface FileMoveRecordService {

    Map<String, Object> fileMoveRecordList(Map<String, Object> queryParams);

    Map<String, Object> remigrateMgMapFigure(Map<String, Object> queryParams);

    void importTaskTable(Map<String, Object> queryParams);

    void clearData(Map<String, Object> queryParams);

    Map<String, Object> remigrateMgDoorImg(Map<String, Object> queryParams);

    Map<String, Object> remigrateImgimages(Map<String, Object> queryParams);
}
