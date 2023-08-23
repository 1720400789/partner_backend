package com.zj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.model.domain.UserTeam;
import com.zj.mapper.UserTeamMapper;
import com.zj.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 1720400789
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-08-06 11:09:49
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




