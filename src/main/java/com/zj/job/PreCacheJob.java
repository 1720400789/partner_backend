package com.zj.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zj.model.domain.User;
import com.zj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //设置重点用户
    private List<Long> mainUserList = Arrays.asList(1L, 2L);

    //每天执行，预热缓存
    @Scheduled(cron = "0 00 22 * * *")//cron表达式
    public void doCacheRecommend(){
        RLock rLock = redissonClient.getLock("zj:precachejob:docache:lock");
        //参数1：服务器等待锁的时间 这里设置等待时间为0，是因为该定时任务只要求每天执行依次，所以只要有一个服务器的线程执行了就可以了
        //参数2：如果请求到锁，锁的过期时间
        //参数3：单位
        //只有一个线程获取锁
        try {//如果请求到锁了就执行
            if (rLock.tryLock(0, 30000L, TimeUnit.MILLISECONDS)){
                log.info("getLock", Thread.currentThread().getId());
                for(Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = new Page<>(1, 10);
                    userService.page(userPage, queryWrapper);
                    List<User> userList = userPage.getRecords();
                    userList = userList.stream().map((user) -> {
                        return userService.getSafetyUser(user);
                    }).collect(Collectors.toList());
                    userPage.setRecords(userList);
                    String redisKey = String.format("zj:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.info("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            //判断当前锁是否是该线程设置的锁
            if(rLock.isHeldByCurrentThread()){
                log.info("unLock", Thread.currentThread().getId());
                rLock.unlock();//释放锁
            }
        }
    }

}