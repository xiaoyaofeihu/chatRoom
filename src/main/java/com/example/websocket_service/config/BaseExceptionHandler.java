package com.example.websocket_service.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public RestResponse handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return RestResponse.error(HttpStatus.NOT_FOUND.value(), "路径不存在，请检查路径是否正确");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public RestResponse handleDuplicateKeyException(DuplicateKeyException e) {
        log.error(e.getMessage(), e);
        return RestResponse.error("数据库中已存在该记录");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(value = Exception.class)
    public RestResponse returnRunningException(Exception e) {
        if (e instanceof MaxUploadSizeExceededException){
            log.error(e.getMessage(), e);
            return RestResponse.error("文件太大，超过最大限制");
        }
        if (e instanceof IOException){
            log.error(e.getMessage(), e);
            return RestResponse.error("文件出现问题，请稍后！");
        }
        log.error(e.getMessage(), e);
        return RestResponse.error(e.getMessage());
    }

}