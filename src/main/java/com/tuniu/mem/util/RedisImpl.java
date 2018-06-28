package com.tuniu.mem.util;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Component
public class RedisImpl {

	@Resource
	private JedisPool jedisPool;

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setCache(String key, Object value) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			jedis.set(key, String.valueOf(value));
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
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
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			jedis.setex(key, expire, String.valueOf(value));
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 *
	 * @param key
	 *            缓存的key
	 * @return String
	 */
	public String getCache(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			return jedis.get(key);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 *
	 * 此方法描述的是：缓存删除
	 *
	 * @param key
	 */
	public void delCache(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			jedis.del(key);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	public Long setNx(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			Long flag = jedis.setnx(key, value);
			return flag;
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
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
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			String result = jedis.set(key, value, "NX", "PX", expire);
			if ("OK".equals(result)) {
				return true;
			} else {
				return false;
			}
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
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
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			return jedis.get(key);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	public long getTtl(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			long b = jedis.ttl(key);
			long a = jedis.pttl(key);
			return jedis.ttl(key);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 连接释放
	 *
	 * @param jedis
	 */
	private void release(Jedis jedis) {
		if (jedis != null) {
			getJedisPool().returnResource(jedis);
		}
	}

	public void setCache(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			jedis.set(key, value);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	/**
	 * 连接销毁
	 *
	 * @param jedis
	 */
	private void broken(Jedis jedis) {
		if (jedis != null) {
			getJedisPool().destroy();
			;
		}
	}

	public Long getIncr(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			return jedis.incr(key);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

	public void expire(String key, int expire) {
		Jedis jedis = null;
		try {
			jedis = getJedisPool().getResource();
			jedis.expire(key, expire);
		} catch (JedisConnectionException e) {
			broken(jedis);
			throw e;
		} finally {
			release(jedis);
		}
	}

}
