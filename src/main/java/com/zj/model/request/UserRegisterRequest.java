package com.zj.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请全体的实体类
 *
 * author 1720400789
 */
@Data
public class UserRegisterRequest implements Serializable {
    public static final long serialVersionUID = 2L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
