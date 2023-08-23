package com.zj.service;

import com.zj.UserCenterApplication;
import com.zj.mapper.UserMapper;
import com.zj.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 用户服务测试
 */
@SpringBootTest(classes = UserCenterApplication.class)
@Slf4j
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Test
    public void testAddUser(){
        User user = new User();
        user.setUsername("test");
        user.setUserAccount("test");
        user.setAvatarUrl("testUrl");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("1212");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
//        String userAccount = "admin";
//        String userPassword = "";
//        String checkPassword = "12345678";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "zj";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "admin";
//        userPassword = "123456";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "ad min";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "admin";
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "test";
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "admin";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertTrue(result > 0);
    }

    @Test
    public void searchUserByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUserByTags(tagNameList);
        Assert.assertNotNull(userList);
    }

    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for(int i = 0; i < INSERT_NUM; i ++){
            User user = new User();
            user.setUsername("假zj");
            user.setUserAccount("fakezj" + i);
            user.setAvatarUrl("https://tvax1.sinaimg.cn/crop.0.0.664.664.180/008lF2Naly8gsq6wa54jnj30ig0igdge.jpg?KID=imgbed,tva&Expires=1689315902&ssig=aAqj6JIIIu");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("15212212121");
            user.setEmail("1212231134@qq.com");
            user.setUserStatus(0);
            user.setIsDelete(0);
            user.setUserRole(0);
            user.setPlanetCode("1212");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 1000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void doConcurrencyInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for(int i = 0; i < 10; i ++){
            List<User> userList = Collections.synchronizedList(new ArrayList<>());//ArrayList是线程不安全的，要转为线程安全的类
            while(true){
                j ++;
                User user = new User();
                user.setUsername("假zj");
                user.setUserAccount("fakezj" + i);
                user.setAvatarUrl("https://tvax1.sinaimg.cn/crop.0.0.664.664.180/008lF2Naly8gsq6wa54jnj30ig0igdge.jpg?KID=imgbed,tva&Expires=1689315902&ssig=aAqj6JIIIu");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("15212212121");
                user.setEmail("1212231134@qq.com");
                user.setUserStatus(0);
                user.setIsDelete(0);
                user.setUserRole(0);
                user.setPlanetCode("1212");
                user.setTags("[]");
                userList.add(user);
                if(j % 10000 == 0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}