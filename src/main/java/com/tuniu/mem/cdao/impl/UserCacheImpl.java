package com.tuniu.mem.cdao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tuniu.mem.bean.User;
import com.tuniu.mem.cdao.UserCache;
import com.tuniu.mem.util.RedisClusterImpl;

@Component
public class UserCacheImpl implements UserCache{

	@Autowired
	private RedisClusterImpl redisImpl;
	
	public void insert(User user) {
		redisImpl.setCache(user.getUserName(), user.getId(), 1000*60*60*24);
		redisImpl.setCache(user.getEmail(), user.getId(), 1000*60*60*24);
		redisImpl.setCache(user.getMobile(), user.getId(), 1000*60*60*24);
	}
	
}
