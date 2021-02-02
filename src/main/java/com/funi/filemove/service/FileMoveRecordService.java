package com.funi.filemove.service;

import java.util.Map;

public interface FileMoveRecordService {

    Map<String, Object> fileMoveRecordList(Map<String, Object> queryParams);

    Map<String, Object> remigrate(Map<String, Object> queryParams);

    void importTaskTable(Map<String, Object> queryParams);

    void clearData(Map<String, Object> queryParams);
}
