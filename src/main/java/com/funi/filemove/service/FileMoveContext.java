package com.funi.filemove.service;

import com.funi.filemove.po.FileMoveCurrentContext;

/**
 * @ClassName FileMoveService
 * @Description TODO
 * @Author YL
 * @Date 2020/10/29 14:31
 * @Version 1.0
 */
public interface FileMoveContext {
    FileMoveCurrentContext getCurrentFileMoveContextInfo(String tableName);
}
