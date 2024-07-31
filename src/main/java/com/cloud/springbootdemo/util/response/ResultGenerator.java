package com.cloud.springbootdemo.util.response;

import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 响应结果生成工具
 * @author HP
 */
public class ResultGenerator {
    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";
    private static final String DEFAULT_FAIL_MESSAGE = "FAIL";
    private static final int RESULT_CODE_SUCCESS = 200;
    private static final int RESULT_CODE_SERVER_ERROR = 500;
    private static final int RESULT_CODE_CLIENT_ERROR = 400;

    /**
     * 无参数 获取成功结果
     */
    public static Result<Object> genSuccessResult() {
        Result<Object> result = new Result<>();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(DEFAULT_SUCCESS_MESSAGE);
        return result;
    }

    /**
     * 返回成功信息
     */
    public static Result<String> genSuccessResult(String message) {
        Result<String> result = new Result<>();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(DEFAULT_SUCCESS_MESSAGE);
        result.setData(message);
        return result;
    }

    /**
     * 获取成功结果
     */
    public static <T> Result<T> genSuccessResult(T data) {
        Result<T> result = new Result<>();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(DEFAULT_SUCCESS_MESSAGE);
        result.setData(data);
        return result;
    }

    /**
     * 生成失败结果
     *
     * @param message 失败结果
     * @return 失败结果
     */
    public static Result<String> genFailResult(String message) {
        Result<String> result = new Result<>();
        result.setResultCode(RESULT_CODE_SERVER_ERROR);
        if (StringUtils.isEmpty(message)) {
            result.setMessage(DEFAULT_FAIL_MESSAGE);
        } else {
            result.setMessage(message);
        }
        return result;
    }

    /**
     * 返回失败结果
     *
     * @param code    状态码
     * @param message 返回信息
     * @return 结果，有状态码
     */
    public static Result<Object> genErrorResult(int code, String message) {
        Result<Object> result = new Result<>();
        result.setResultCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> genFailResult(T obj) {
        Result<T> result = new Result<>();
        result.setResultCode(RESULT_CODE_CLIENT_ERROR);
        if (Objects.isNull(obj)) {
            result.setMessage(DEFAULT_FAIL_MESSAGE);
        } else {
            result.setMessage(obj.toString());
        }
        return result;
    }
}
