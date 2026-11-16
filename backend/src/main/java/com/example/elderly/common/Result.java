package com.example.elderly.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Result<T> {
    private int code;
    private String message;
    private T data;
    private String timestamp;

    private static final DateTimeFormatter Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String nowTime(){

        return LocalDateTime.now().format(Formatter);
    }

    public static<T> Result<T> success(T data){
        Result<T> r = new Result<>();
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        r.setData(data);
        r.setTimestamp(nowTime());
        return r;
    }

    public static<T> Result<T> success(){
        return success(null);
    }

    public static<T> Result<T> fail( Integer code,String  message){
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage( message);
        r.setData( null);
        r.setTimestamp(nowTime());
        return r;
    }
    public static<T> Result<T> fail(){
        return fail(ResultCodeEnum.FAIL.getCode(),ResultCodeEnum.FAIL.getMessage());
    }

}
