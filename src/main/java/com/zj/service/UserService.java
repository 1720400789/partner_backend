package com.zj.service;

import com.zj.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 1720400789
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册逻辑
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录逻辑，用户登录后一般要返回用户信息，但是要注意保护这些用户信息，要脱敏
     * @param userAccount 账户
     * @param userPassword 密码
     * @return 返回脱敏后的信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);


    List<User> searchUsers(String username);

    boolean delUserById(long id);

    /**
     * 用户注销
     * @param request
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);
}
