package com.funi.filemove.po;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

public class ImgImagesPo {
    private BigDecimal id;

    private String imgstyle;

    private String property;

    private String imgmark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date regidate;

    private String note;

    private String uuid;

    private String localDataSource;

    private byte[] image;

    private byte[] imgagePic;

    private String dataSource;

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getImgstyle() {
        return imgstyle;
    }

    public void setImgstyle(String imgstyle) {
        this.imgstyle = imgstyle == null ? null : imgstyle.trim();
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property == null ? null : property.trim();
    }

    public String getImgmark() {
        return imgmark;
    }

    public void setImgmark(String imgmark) {
        this.imgmark = imgmark == null ? null : imgmark.trim();
    }

    public Date getRegidate() {
        return regidate;
    }

    public void setRegidate(Date regidate) {
        this.regidate = regidate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImgagePic() {
        return imgagePic;
    }

    public void setImgagePic(byte[] imgagePic) {
        this.imgagePic = imgagePic;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}