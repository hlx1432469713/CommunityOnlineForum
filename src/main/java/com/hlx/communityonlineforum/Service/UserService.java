package com.hlx.communityonlineforum.Service;

import com.hlx.communityonlineforum.Dao.UserMapper;
import com.hlx.communityonlineforum.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService  {
    @Autowired
    private UserMapper userMapper;


    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

}
