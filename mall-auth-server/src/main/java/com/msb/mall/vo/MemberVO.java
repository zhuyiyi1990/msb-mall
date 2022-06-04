package com.msb.mall.vo;

import lombok.Data;

import java.util.Date;

@Data
public class MemberVO {

    private Long id;

    private Long levelId;

    private String username;

    private String password;

    private String nickname;

    private String mobile;

    private String email;

    private String header;

    private Integer gender;

    private Date birth;

    private String city;

    private String job;

    private String sign;

    private Integer sourceType;

    private Integer integration;

    private Integer growth;

    private Integer status;

    private Date createTime;

    private String socialUid;
    private String accessToken;
    private long expiresIn;

}