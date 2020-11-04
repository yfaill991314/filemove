package com.funi.filemove.po;

/**
 * @ClassName ResultVO
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/6/4 16:13
 * @Version 1.0
 */
public class ResultVO<T> {
    private Integer code;

    private String message;

    private T data;

    public ResultVO() {
    }

    public ResultVO(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
