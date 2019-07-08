package com.hongplayer.httpservice.service;


public class ExceptionApi extends RuntimeException {

    private int code;
    private String msg;

    ExceptionApi(int resultCode, String msg) {
        this.code = resultCode;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
