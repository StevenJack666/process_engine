package com.process.util;

public class ResultFactory {

    public static Result buildFailResult(String desc) {
        return new Result(desc);
    }

    public static Result buildSuccessResult(String desc) {
        return new Result(desc);
    }
}
