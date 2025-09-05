package com.flow.exception;

import com.flow.tool.MapTool;
import com.google.gson.Gson;

public enum ErrorCodeEnum {

    /*** －－－－－－基础错误码 错误码范围 401-599－－－－－－*/
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(999, "系统错误"),
    PLEASE_LOGIN(1001, "请登录"),
    PARAMETERS_ERROR(1002, "参数错误"),
    ACCESS_DENY(1003, "无权限"),
    TOO_FREQUENT(1004, "操作过于频繁"),
    NOT_OPEN(1005, "服务未开放"),
    NETWORK_ERROR(1006, "网络错误"),
    ;
    private int errorNo;

    private String errorMsg;


    public String getErrorMsg() {
        return errorMsg;
    }


    public int getErrorNo() {
        return errorNo;
    }

    ErrorCodeEnum(int errorNo, String errorMsg) {
        this.errorNo = errorNo;
        this.errorMsg = errorMsg;
    }

    public ErrCodeException generalException(String msg) {
        String newMsg = String.format("[%s]:%s", this.errorMsg, msg);
        return new ErrCodeException(this.errorNo, newMsg);
    }

    public ErrCodeException generalException() {
        return new ErrCodeException(this.errorNo, this.errorMsg);
    }

    public ErrCodeException generalException(Throwable throwable) {
        return new ErrCodeException(this.errorNo, this.errorMsg, throwable);
    }

    public void throwExtendMsgException(String msg) {
        throw this.generalException(msg);
    }

    public void throwException() {
        throw this.generalException();
    }

    public void throwException(Throwable throwable) {
        throw this.generalException(throwable);
    }

    public static void throwException(String msg) {
        throw new ErrCodeException(110, msg);
    }

    public static void throwException(int code, String msg) {
        throw new ErrCodeException(code, msg);
    }

    public String toJson() {
        return new Gson().toJson(MapTool.Map().put("code", errorNo).put("msg", errorMsg).put("time", System.currentTimeMillis()));
    }

}