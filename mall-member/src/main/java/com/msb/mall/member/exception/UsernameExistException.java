package com.msb.mall.member.exception;

public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("账号已存在");
    }

}