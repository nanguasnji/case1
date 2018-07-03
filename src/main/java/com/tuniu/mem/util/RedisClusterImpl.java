package com.tuniu.mem.util;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Component
public class RedisClusterImpl {

	private volatile JedisCluster jedisCluster;

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Resource
	private JedisPoolConfig jedisPoolConfig;

	public JedisCluster getJedisCluster() {
		if (jedisCluster == null) {
			synchronized (RedisClusterImpl.class) {
				if (jedisCluster == null) {
					Set<HostAndPort> set = new HashSet<HostAndPort>();
					HostAndPort hap = new HostAndPort(host, port);
					set.add(hap);
					jedisCluster = new JedisCluster(set, jedisPoolConfig);
				}
			}
		}
		return jedisCluster;
	}

	public void setCache(String key, Object value) {
		try {
			getJedisCluster().set(key, String.valueOf(value));
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	/**
	 *
	 * 此方法描述的是：设置缓存
	 *
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param expire
	 *            有效时间
	 *
	 */
	public void setCache(String key, Object value, int expire) {
		try {
			getJedisCluster().setex(key, expire, String.valueOf(value));
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	/**
	 *
	 * @param key
	 *            缓存的key
	 * @return String
	 */
	public String getCache(String key) {
		try {
			return getJedisCluster().get(key);
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	/**
	 *
	 * 此方法描述的是：缓存删除
	 *
	 * @param key
	 */
	public void delCache(String key) {
		try {
			getJedisCluster().del(key);
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	public Long setNx(String key, String value) {
		try {
			Long flag = getJedisCluster().setnx(key, value);
			return flag;
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	/**
	 * @param key
	 * @param value
	 * @param expire
	 *            毫秒 [EX seconds] [PX milliseconds]
	 * @return
	 */
	public boolean setTransNx(String key, String value, int expire) {
		try {
			String result = getJedisCluster().set(key, value, "NX", "PX", expire);
			if ("OK".equals(result)) {
				return true;
			} else {
				return false;
			}
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	/**
	 * REDIS事物
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object getTrans(String key) {
		try {
			return getJedisCluster().get(key);
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	public void setCache(String key, String value) {
		try {
			getJedisCluster().set(key, value);
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	public Long getIncr(String key) {
		try {
			return getJedisCluster().incr(key);
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}

	public void expire(String key, int expire) {
		try {
			getJedisCluster().expire(key, expire);
		} catch (JedisConnectionException e) {
			throw e;
		} finally {
		}
	}
}
