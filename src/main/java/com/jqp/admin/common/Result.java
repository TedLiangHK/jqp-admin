package com.jqp.admin.common;

import lombok.Data;

@Data
public class Result <T>{
    private int status;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(T data) {
        this.data = data;
    }

    public Result(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
}
