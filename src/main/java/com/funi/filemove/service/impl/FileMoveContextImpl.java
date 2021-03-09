package com.funi.filemove.service.impl;

import com.funi.filemove.Constants;
import com.funi.filemove.dao.FileMoveRecordPoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.FileMoveCurrentContext;
import com.funi.filemove.service.FileMoveContext;
import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName DataSourceChangeUtil
 * @Description 数据源修改
 * @Author Feng.Yang
 * @Date 2020/11/10 19:04
 * @Version 1.0
 */
@Service
public class FileMoveContextImpl implements FileMoveContext {
    @Resource
    private FileMoveRecordPoMapper fileMoveRecordPoMapper;

    /**
     * @return void
     * @Author Feng.Yang
     * @Description //遍历查询所有数据源，设置第一未迁移完成的数据源，，为今日 迁移数据源
     * @Date 19:38 2020/11/10
     * @Param []
     **/
    @Override
    public FileMoveCurrentContext getCurrentFileMoveContextInfo(String tableName) {
        List<Map<String, String>> dataSourceList = Constants.dataSourceList;
        Map<String, Object> queryMap = new HashMap<>();
        for (Map<String, String> dataSourceItem : dataSourceList) {
            ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
            queryMap.put("tableName", tableName);
            queryMap.put("dataSource", dataSourceItem.get("dataSource"));
            queryMap.put("MoveStatus", Constants.MOVE_RECORD_STATUS);
            int unfinishCount = fileMoveRecordPoMapper.queryMoveRecordCount(queryMap);
            if (unfinishCount > 0) {
                FileMoveCurrentContext fileMoveCurrentContext = new FileMoveCurrentContext();
                fileMoveCurrentContext.setCurMoveDataSource(dataSourceItem.get("dataSource"));
                fileMoveCurrentContext.setCurMoveDataSourceName(dataSourceItem.get("dataSourceName"));
                fileMoveCurrentContext.setCurMovetableName(tableName);
                fileMoveCurrentContext.setMoveStatus(Constants.MOVE_RECORD_STATUS);
                fileMoveCurrentContext.setTransitionDBName(Constants.DEFAULT_DATA_SOURCE_NAME);
                return fileMoveCurrentContext;
            }
        }
        return null;
    }
}
