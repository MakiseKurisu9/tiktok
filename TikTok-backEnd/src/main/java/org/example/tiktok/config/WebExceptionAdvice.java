package org.example.tiktok.config;

import lombok.extern.slf4j.Slf4j;
import org.example.tiktok.entity.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler
    public Result handleRunTimeException(RuntimeException e) {
        return Result.fail(e.getMessage());
    }

}
