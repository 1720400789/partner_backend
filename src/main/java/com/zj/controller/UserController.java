package com.zj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.zj.common.BaseResponse;
import com.zj.common.ErrorCode;
import com.zj.common.ResultUtils;
import com.zj.exception.BussinessException;
import com.zj.model.domain.User;
import com.zj.model.request.UserLoginRequest;
import com.zj.model.request.UserRegisterRequest;
import com.zj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zj.constant.Userconstant.ADMIN_ROLE;
import static com.zj.constant.Userconstant.USER_LOGIN_STATE;

/**
 * 用户控制器
 *
 * @author 1720400789
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = { "http://localhost:5173" })
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验数据，如果为空就直接退出，而不进入到业务逻辑中
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if(StringUtils.isAllBlank(userAccount, userPassword, checkPassword, planetCode)){
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        //校验数据，如果为空就直接退出，而不进入到业务逻辑中
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAllBlank(userAccount, userPassword)){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        User result = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(result);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw new BussinessException(ErrorCode.COOKIE_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BussinessException(ErrorCode.NO_LOGIN, "未登录");
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        return ResultUtils.success(userService.getSafetyUser(user));
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUserList(String username, HttpServletRequest request){
        //1.鉴权，仅管理员可以调用
        if(!userService.isAdmin(request)){
            throw new BussinessException(ErrorCode.NO_AUTH, "仅管理员可访问");
        }
        return ResultUtils.success(userService.searchUsers(username));
    }

    @DeleteMapping("/delUser")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        if(!userService.isAdmin(request)){
            throw new BussinessException(ErrorCode.NO_AUTH, "仅管理员可访问");
        }
        if(id <= 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "id错误");
        }
        return ResultUtils.success(userService.delUserById(id));
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        log.info(tagNameList.get(0));
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> usersRecommend(long page, long pageSize, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("zj:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();//opsForValue存string
        //如果有缓存，直接读缓存
        Page<User> userListPage = (Page<User>) valueOperations.get(redisKey);
        if(userListPage != null) {
            return ResultUtils.success(userListPage);
        }
        //无缓存，查数据库，存入缓存
        Page<User> userPage = new Page<>(page, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userService.page(userPage, queryWrapper);
        List<User> userList = userPage.getRecords();
        userPage.setRecords(userList.stream().map((user) -> {return userService.getSafetyUser(user);}).collect(Collectors.toList()));
        try {
            //指定键、值、缓存过期时间和时间单位
            valueOperations.set(redisKey, userPage, 10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> userUpdate(@RequestBody User user, HttpServletRequest request){
        if(user == null){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }
}
