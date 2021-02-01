package com.funi.filemove.po;

import java.math.BigDecimal;
import java.util.Date;

public class MgDoorImgPo {
    private BigDecimal id;

    private String property;

    private String imgstyle;

    private String imgdes;

    private BigDecimal status;

    private String inman;

    private Date regidate;

    private String imgfilename;

    private BigDecimal imgfilesize;

    private String tempid;

    private String filename;

    private String description;

    private String creater;

    private Date createtime;

    private String note;

    private BigDecimal isdele;

    private BigDecimal resulsid;

    private String uuid;

    private String localDataSource;

    private String resultsUuid;

    private byte[] image;

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property == null ? null : property.trim();
    }

    public String getImgstyle() {
        return imgstyle;
    }

    public void setImgstyle(String imgstyle) {
        this.imgstyle = imgstyle == null ? null : imgstyle.trim();
    }

    public String getImgdes() {
        return imgdes;
    }

    public void setImgdes(String imgdes) {
        this.imgdes = imgdes == null ? null : imgdes.trim();
    }

    public BigDecimal getStatus() {
        return status;
    }

    public void setStatus(BigDecimal status) {
        this.status = status;
    }

    public String getInman() {
        return inman;
    }

    public void setInman(String inman) {
        this.inman = inman == null ? null : inman.trim();
    }

    public Date getRegidate() {
        return regidate;
    }

    public void setRegidate(Date regidate) {
        this.regidate = regidate;
    }

    public String getImgfilename() {
        return imgfilename;
    }

    public void setImgfilename(String imgfilename) {
        this.imgfilename = imgfilename == null ? null : imgfilename.trim();
    }

    public BigDecimal getImgfilesize() {
        return imgfilesize;
    }

    public void setImgfilesize(BigDecimal imgfilesize) {
        this.imgfilesize = imgfilesize;
    }

    public String getTempid() {
        return tempid;
    }

    public void setTempid(String tempid) {
        this.tempid = tempid == null ? null : tempid.trim();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? null : filename.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater == null ? null : creater.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }

    public BigDecimal getIsdele() {
        return isdele;
    }

    public void setIsdele(BigDecimal isdele) {
        this.isdele = isdele;
    }

    public BigDecimal getResulsid() {
        return resulsid;
    }

    public void setResulsid(BigDecimal resulsid) {
        this.resulsid = resulsid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public String getLocalDataSource() {
        return localDataSource;
    }

    public void setLocalDataSource(String localDataSource) {
        this.localDataSource = localDataSource == null ? null : localDataSource.trim();
    }

    public String getResultsUuid() {
        return resultsUuid;
    }

    public void setResultsUuid(String resultsUuid) {
        this.resultsUuid = resultsUuid == null ? null : resultsUuid.trim();
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}