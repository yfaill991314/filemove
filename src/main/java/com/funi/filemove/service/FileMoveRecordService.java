package com.funi.filemove.service;

import java.util.Map;

public interface FileMoveRecordService {

    Map<String, Object> fileMoveRecordList(Map<String, Object> queryParams);

    Map<String, Object> remigrate(Map<String, Object> queryParams);
}
