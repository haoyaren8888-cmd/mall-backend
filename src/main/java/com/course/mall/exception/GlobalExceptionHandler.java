package com.course.mall.exception;

import com.course.mall.common.BusinessException;
import com.course.mall.common.Result;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception exception) {
        return Result.fail(400, "参数不正确");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        exception.printStackTrace();
        return Result.fail(500, "服务异常");
    }
}
