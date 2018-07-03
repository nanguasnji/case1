package com.tuniu.mem.util;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Component
public class RedisImpl2 {

	@Resource
	private Jedis jedis;

	public void setCache(String key, Object value) {
		try {
			jedis.set(key, String.valueOf(value));
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
			
		}
	}

}
