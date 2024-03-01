package com.dhu.utils.model;

import java.util.HashMap;
import java.util.Map;

public class Result {
    public static final int SAVE_OK = 20011;
    public static final int UPDATE_OK = 20021;
    public static final int DELETE_OK = 20031;
    public static final int GET_OK = 20041;
    public static final int SAVE_ERR = 20010;
    public static final int UPDATE_ERR = 20020;
    public static final int DELETE_ERR = 20030;
    public static final int GET_ERR = 20040;
    public static final int EXCEPTION = 10000;
    public static final int NOT_LOGIN = 20000;

    private final Integer code;
    private final Map<String, Object> data;
    private String message;

    private Result(Integer code, String message) {
        this.code = code;
        this.data = new HashMap<>();
        this.message = message;
    }

    public static Result saveOk() {
        return new Result(SAVE_OK, null).setMsg("数据保存成功");
    }

    public static Result saveErr() {
        return new Result(SAVE_ERR, null).setMsg("数据保存失败");
    }

    public static Result updateOk() {
        return new Result(UPDATE_OK, null).setMsg("数据更新成功");
    }

    public static Result updateErr() {
        return new Result(UPDATE_ERR, null).setMsg("数据更新失败");
    }

    public static Result deleteOk() {
        return new Result(DELETE_OK, null).setMsg("数据删除成功");
    }

    public static Result deleteErr() {
        return new Result(DELETE_ERR, null).setMsg("数据删除失败");
    }

    public static Result getOk() {
        return new Result(GET_OK, null).setMsg("数据获取成功");
    }

    public static Result getErr() {
        return new Result(GET_ERR, null).setMsg("数据获取失败");
    }

    public static Result exception() {
        return new Result(EXCEPTION, null).setMsg("后台系统异常");
    }

    public static Result notLogin() {
        return new Result(NOT_LOGIN, null).setMsg("未登录或登陆过期");
    }


    public static Result verifySave(boolean condition) {
        return condition ? saveOk() : saveErr();
    }

    public static Result verifyDelete(boolean condition) {
        return condition ? deleteOk() : deleteErr();
    }

    public static Result verifyGet(boolean condition) {
        return condition ? getOk() : getErr();
    }

    public static Result verifyUpdate(boolean condition) {
        return condition ? updateOk() : updateErr();
    }

    public static Result nullFilterData(String key, Object value) {
        return value == null ? Result.getErr() : Result.getOk().setData(key, value);
    }

    public Result appendMsg(String message) {
        this.message += '|' + message;
        return this;
    }

    public Result setMsg(String message) {
        this.message = message;
        return this;
    }

    public Result setData(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Object getData(String key) {
        return data.get(key);
    }
}
