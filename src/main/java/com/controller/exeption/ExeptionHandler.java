package com.controller.exeption;

import com.exeption.ServerRequestExeption;
import lombok.Data;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by dima on 11.12.17.
 */
@ControllerAdvice
@Order(100)
public class ExeptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ServerExceptionHandlers.ErrorResponse serverError(final ServerRequestExeption ex) {
        return new ServerExceptionHandlers.ErrorResponse("Any connection server problem", "Server is not available, try again latter");
    }

    @Data
    public static class ErrorResponse {
        private final String code;
        private final String message;
    }
}
