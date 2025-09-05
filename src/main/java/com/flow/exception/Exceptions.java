package com.flow.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

public class Exceptions {
    private static final Map<Class, ErrCodeException> e = new HashMap<>();

    static {
        e.put(HttpMessageNotReadableException.class, new ErrCodeException(102, "参数错误"));
        e.put(ConstraintViolationException.class, new ErrCodeException(102, "参数错误"));
    }

    public static Map<Class, ErrCodeException> getE() {
        return e;
    }
}
