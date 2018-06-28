package com.tuniu.mem.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tuniu.mem.bean.User;
import com.tuniu.mem.cdao.UserCache;
import com.tuniu.mem.mapper.UserMapper;
import com.tuniu.mem.service.UserService;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private UserCache userCache;
	
	public void insert(User user) {
		userMapper.insert(user);
		userCache.insert(user);
	}
}
