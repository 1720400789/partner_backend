package com.zj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zj.common.ErrorCode;
import com.zj.exception.BussinessException;
import com.zj.model.domain.User;
import com.zj.mapper.UserMapper;
import com.zj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zj.constant.Userconstant.USER_LOGIN_STATE;

/**
 * 用户服务逻辑实现类
* @author 1720400789
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /*
    *封装常量“盐”，让md5加密更复杂
    */
    private static final String SALT = "zj";

    //之所以后端也做出如此的校验，是因为请求可以不通过前端校验就发送给后端，所以前后端都必须做出完整的校验，防止攻击
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验
        if(StringUtils.isAllBlank(userAccount, userPassword, checkPassword, planetCode)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(userAccount.length() < 4){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户账号位数过短");
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户密码位数过短");
        }
        if(planetCode.length() < 2 || planetCode.length() > 5){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "星球编号位数过长");
        }
        //校验账户不能包含特殊字符
//        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        String validPattern = "[^a-zA-Z0-9]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码不能包含特殊字符");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "账号不能重复");
        }
        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "星球编号不能重复");
        }
        //2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "特殊异常");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAllBlank(userAccount, userPassword)){
            // todo 修改为自定义异常
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        if(userAccount.length() < 4){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userPassword.length() < 8){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        //校验账户不能包含特殊字符
        String validPattern = "[^a-zA-Z0-9]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }

        //2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //校验用户名和密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "登录信息错误");
        }

        //3.用户信息脱敏，不脱敏的话数据库中的数据会随着查询出的User全部返回给前端
        //这里我们定义一个脱敏后的实体类，不封装用户密码等敏感信息和无用信息
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    @Override
    public List<User> searchUsers(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username", username);
        }
        List<User> userList =  this.list(queryWrapper);
        return userList.stream().map(user -> {
            return getSafetyUser(user);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean delUserById(long id) {
        if(id <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(id);
    }

    public User getSafetyUser(User originUser){
        if(originUser == null){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());

        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        //方式二，在内存中查询，不同于sql查询，在内存中我们可以根据许多不同的条件进行查询
        //先sql查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //在内存中判断是否包含所有的标签，parallelStream同stream一样，但是是一个并行流
        return userList.stream().filter(user -> {//lambda语法糖，遍历、过滤userList集合，if return true 则不过滤，否则过滤
            String tagsStr = user.getTags();
            if(StringUtils.isBlank(tagsStr)){
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName : tagNameList){
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());//遍历过滤的结果然后saft一遍
    }

    //@Deprecated注释标识该方法过期不建议使用
    @Deprecated
    //修饰为private方法，防止外部调用
    private List<User> searchUserByTagsBySQL(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
//        方式一，利用sql查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接and查询
        for(String tagName : tagNameList){
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




