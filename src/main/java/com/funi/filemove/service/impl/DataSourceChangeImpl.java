package com.funi.filemove.service.impl;

import com.funi.filemove.Constants;
import com.funi.filemove.dao.MgMapFigurePoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.service.DataSourceChange;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public class DataSourceChangeImpl implements DataSourceChange {

    private static volatile String CurrentMoveDataSource;

    @Resource
    private MgMapFigurePoMapper mgMapFigurePoMapper;

    public static String getCurrentMoveDataSource() {
        return CurrentMoveDataSource;
    }

    private static void setCurrentMoveDataSource(String currentMoveDataSource) {
        CurrentMoveDataSource = currentMoveDataSource;
    }

    /**
     * @Author Feng.Yang
     * @Description //遍历查询所有数据源，设置第一未迁移完成的数据源，，为今日 迁移数据源
     * @Date 19:38 2020/11/10
     * @Param []
     * @return void
     **/
    @Override
    public void setTodayMoveDataSource() {
        DataSourceChangeImpl.setCurrentMoveDataSource(null);
        List<Map<String, String>> dataSourceList = Constants.dataSource;
        for (Map<String, String> dataSourceItem : dataSourceList) {
            ContextSynchronizationManager.bindResource("datasource", dataSourceItem.get("dataSourceName"));
            int unfinishCount = mgMapFigurePoMapper.selectUnfinishCount();
            if (unfinishCount > 0) {
                DataSourceChangeImpl.setCurrentMoveDataSource(dataSourceItem.get("dataSourceName"));
                break;
            }
        }
    }
}
