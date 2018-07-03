package com.tuniu.mem.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tuniu.mem.service.PerformanceService;
import com.tuniu.mem.util.RedisImpl;
import com.tuniu.mem.util.RedisImpl2;

@Service
public class PerformanceServiceImpl implements PerformanceService{
	
	@Autowired
	private RedisImpl redisImpl;

	@Autowired
	private RedisImpl2 redisImpl2;

	public void setBySingle(String key, String value) {
		redisImpl2.setCache(key, value);
	}

	public void setByPool(String key, String value) {
		redisImpl.setCache(key, value);
	}
}
