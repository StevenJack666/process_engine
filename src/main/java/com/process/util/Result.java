package com.process.util;

public class Result {

    public Result(String message) {
        this.message = message;
    }
    public Result() {

    }

    private Integer code =0;
    private String message;
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



}
