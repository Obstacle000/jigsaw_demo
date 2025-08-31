package com.xuexian.jigsaw.handler;

import com.xuexian.jigsaw.entity.Code;
import com.xuexian.jigsaw.exception.BusinessException;
import com.xuexian.jigsaw.exception.SystemException;
import com.xuexian.jigsaw.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;


@RestControllerAdvice
public class ProjectExceptionAdvice {
    @ExceptionHandler(SystemException.class)
    public Result doSystemException(SystemException ex) {
        return new Result(ex.getCode(), ex.getMessage(),null);
    }

    @ExceptionHandler(BusinessException.class)
    public Result doBusinessException(BusinessException ex) {
        return new Result(ex.getCode(),ex.getMessage(),null);
    }

    @ExceptionHandler(Exception.class)
    public Result doException(Exception ex) {
        return new Result(Code.SYSTEM_UNKNOWN_ERR, "系统繁忙，请稍后再试！",null);
    }
}
