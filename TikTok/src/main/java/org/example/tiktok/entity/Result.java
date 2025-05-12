package org.example.tiktok.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Boolean success;
    private String message;
    private Object data;

    public static Result ok() {
        return new Result(true, null, null);
    }

    public static Result ok(Object data) {
        return new Result(true,null,data);
    }

    public static Result fail(String message) {
        return new Result(false, message, null);
    }

}
