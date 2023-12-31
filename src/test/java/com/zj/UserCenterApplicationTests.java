package com.zj;

import com.zj.model.domain.User;
import com.zj.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = UserCenterApplication.class)
class UserCenterApplicationTests {

    @Resource
    private UserService userService;

    @Test
    void contextLoads() {
    }

    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
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
        userService.saveBatch(userList, 10);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
