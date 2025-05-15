package org.example.tiktok.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Result {

    private Boolean success;

    private String message;

    private Object data;
    //page helper
    private Long total;

    public Result(Boolean success, String message, Object data, Long total) {
        this.data = data;
        this.message = message;
        this.success = success;
        this.total = total;
    }

    public static Result ok(String message) {return new Result(true, message, null,null); }
    public static Result ok(Object data) {
        return new Result(true,null,data,null);
    }
    public static Result ok(List<?> data, Long total) { return new Result(true,null,data,total);}
    public static Result fail(String message) {
        return new Result(false, message, null,null);
    }

}
