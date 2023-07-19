package com.zj.controller;

import com.zj.common.BaseResponse;
import com.zj.common.ErrorCode;
import com.zj.common.ResultUtils;
import com.zj.exception.BussinessException;
import com.zj.model.domain.User;
import com.zj.model.request.UserLoginRequest;
import com.zj.model.request.UserRegisterRequest;
import com.zj.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.zj.constant.Userconstant.ADMIN_ROLE;
import static com.zj.constant.Userconstant.USER_LOGIN_STATE;

/**
 * 用户控制器
 *
 * @author 1720400789
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

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
        if(!isAdmin(request)){
            throw new BussinessException(ErrorCode.NO_AUTH, "仅管理员可访问");
        }
        return ResultUtils.success(userService.searchUsers(username));
    }

    @DeleteMapping("/delUser")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BussinessException(ErrorCode.NO_AUTH, "仅管理员可访问");
        }
        if(id <= 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "id错误");
        }
        return ResultUtils.success(userService.delUserById(id));
    }

    /**
     * 鉴权方法
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        //1.鉴权，仅管理员可以调用
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if(user == null || user.getUserRole() != ADMIN_ROLE){
            return false;
        }
        return true;
    }
}
