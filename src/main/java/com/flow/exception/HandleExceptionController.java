package com.flow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestControllerAdvice
public class HandleExceptionController {

    @ExceptionHandler(value = {Exception.class})
    public Map resolveException(HttpServletRequest request, Exception e) {
        Map<String, Object> arr = new HashMap<>();
        if (e instanceof ErrCodeException) {
            arr.put("code", ((ErrCodeException) e).getErrcode());
            arr.put("msg", e.getMessage());
        } else if (Exceptions.getE().containsKey(e.getClass())) {
            arr.put("code", Exceptions.getE().get(e.getClass()).getErrcode());
            String message = Exceptions.getE().get(e.getClass()).getMessage();
            arr.put("msg", Exceptions.getE().get(e.getClass()).getMessage());
        } else {
            arr.put("code", "107");
            arr.put("msg", "系统繁忙");
            arr.put("enMsg", "System busy");
        }
        ExceptionUtils.printStackTrace(e);
        e.printStackTrace();
        arr.put("time", System.currentTimeMillis());
        return arr;
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public Result paramExceptionHandler(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        if(bindingResult.hasErrors()){
            List<ObjectError> errors = bindingResult.getAllErrors();
            if(!errors.isEmpty()){
                FieldError fieldError = (FieldError) errors.get(0);
                Result result = Result.instance();
                result.setCode(ErrorCodeEnum.PARAMETERS_ERROR.getErrorNo()+"");
                String defaultMessage = fieldError.getDefaultMessage();
                result.setMsg(defaultMessage);
                return result;
            }
        }
        return Result.fail(ErrorCodeEnum.PARAMETERS_ERROR);
    }

}
