package com.zork.flink.monitor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xiese
 * @Description 返回给请求方的响应通用结构
 * @Email xiesen310@163.com
 * @Date 2020/7/4 16:30
 */
@Getter
@Setter
public class Resp<T> {
    /**
     * 错误码
     */
    public int code;
    /**
     * 错误描述
     */
    public String message;

    /**
     * 此项当有值时才会返回
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}
