package com.zork.flink.monitor.utils;

import com.zork.flink.monitor.model.Resp;

/**
 * @author xiese
 * @Description 响应工具类 0: 成功; 1: 失败
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:37
 */
public class RespHelper {
    public static <T> Resp<T> createEmptyRespDto() {
        return new Resp<>();
    }

    public static <T> Resp<T> fail(int code) {
        Resp<T> t = createEmptyRespDto();
        t.setCode(code);
        t.setMessage(null);
        return t;
    }

    public static <T> Resp<T> fail(int code, String message) {
        Resp<T> t = createEmptyRespDto();
        t.setCode(code);
        t.setMessage(message);
        return t;
    }

    public static <T> Resp<T> fail(int code, T data, String message) {
        Resp<T> t = createEmptyRespDto();
        t.setCode(code);
        t.setData(data);
        t.setMessage(message);
        return t;
    }

    public static <T> Resp<T> ok(T data) {
        Resp<T> t = createEmptyRespDto();
        t.setCode(0);
        t.setData(data);
        return t;
    }

    public static <T> Resp<T> ok(boolean flag) {
        Resp<T> t = createEmptyRespDto();
        if (flag) {
            t.setCode(0);
            t.setMessage("成功");
        } else {
            t.setCode(1);
            t.setMessage("失败");
        }
        return t;
    }

    public static <T> Resp<T> ok(T data, String message) {
        Resp<T> t = createEmptyRespDto();
        t.setCode(0);
        t.setData(data);
        t.setMessage(message);
        return t;
    }
}
