package com.funi.filemove.po;

public class FileMoveCurrentContext {
    //当前迁移数据源编码
    private String curMoveDataSource;
    //当前迁移数据源名称
    private String curMoveDataSourceName;
    //当前迁移 表名称
    private String curMovetableName;
    //中间表名称
    private String transitionDBName;
    //被迁移文件 在任务表中的状态
    private String MoveStatus;

    public FileMoveCurrentContext() {
        this.transitionDBName="zj";
    }

    public String getCurMoveDataSource() {
        return curMoveDataSource;
    }

    public void setCurMoveDataSource(String curMoveDataSource) {
        this.curMoveDataSource = curMoveDataSource;
    }

    public String getCurMoveDataSourceName() {
        return curMoveDataSourceName;
    }

    public void setCurMoveDataSourceName(String curMoveDataSourceName) {
        this.curMoveDataSourceName = curMoveDataSourceName;
    }

    public String getCurMovetableName() {
        return curMovetableName;
    }

    public void setCurMovetableName(String curMovetableName) {
        this.curMovetableName = curMovetableName;
    }

    public String getTransitionDBName() {
        return transitionDBName;
    }

    public void setTransitionDBName(String transitionDBName) {
        this.transitionDBName = transitionDBName;
    }

    public String getMoveStatus() {
        return MoveStatus;
    }

    public void setMoveStatus(String moveStatus) {
        MoveStatus = moveStatus;
    }
}
