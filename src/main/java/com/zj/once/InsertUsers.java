package com.zj.once;
import java.util.Date;

import com.zj.mapper.UserMapper;
import com.zj.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

//@Component
public class InsertUsers {
//    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入数据
     */
//    @Scheduled()
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000000;
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
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

//    public static void main(String[] args) {
//
//    }
}