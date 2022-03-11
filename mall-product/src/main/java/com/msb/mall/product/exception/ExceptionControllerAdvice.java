package com.msb.mall.product.exception;

import com.msb.common.exception.BizCodeEnum;
import com.msb.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*@ResponseBody
@ControllerAdvice*/
@RestControllerAdvice(basePackages = "com.msb.mall.product.controller")
@Slf4j
public class ExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerValidException(MethodArgumentNotValidException e) {
        Map<String, String> map = new HashMap<>();
        e.getFieldErrors().forEach((fieldError) -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
//        return R.error(400, "提交的数据不合法").put("data", map);
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(Throwable.class)
    public R handlerException(Throwable throwable) {
        log.error("错误信息：", throwable);
//        return R.error(400, "未知异常信息").put("data", throwable.getMessage());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg()).put("data", throwable.getMessage());
    }

}