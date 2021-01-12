package com.funi.filemove.po;

import java.math.BigDecimal;
import java.util.Date;

public class FileMoveRecordPo {
    private String uuid;

    private String fileStoreId;

    private String fileUuid;

    private String bizid;

    private String tablename;

    private String dataSource;

    private Date createtime;

    private String moveStatus;

    private String remark;

    private String threadId;

    private BigDecimal fileSize;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public String getFileStoreId() {
        return fileStoreId;
    }

    public void setFileStoreId(String fileStoreId) {
        this.fileStoreId = fileStoreId == null ? null : fileStoreId.trim();
    }

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid == null ? null : fileUuid.trim();
    }

    public String getBizid() {
        return bizid;
    }

    public void setBizid(String bizid) {
        this.bizid = bizid == null ? null : bizid.trim();
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename == null ? null : tablename.trim();
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource == null ? null : dataSource.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getMoveStatus() {
        return moveStatus;
    }

    public void setMoveStatus(String moveStatus) {
        this.moveStatus = moveStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId == null ? null : threadId.trim();
    }

    public BigDecimal getFileSize() {
        return fileSize;
    }

    public void setFileSize(BigDecimal fileSize) {
        this.fileSize = fileSize;
    }
}