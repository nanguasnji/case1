package com.tuniu.mem.util.jcp;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

/**
 * version 1 实现基本功能
 * 
 * @author zhengfanming
 *
 */
public class JedisClusterPoolV1 {

	private final static int STATE_ACTIVITY = 1;
	private final static int MAX_WAIT_TIME = 50;
	private volatile int min;
	private volatile int max;
	private volatile AtomicInteger count;
	private LinkedBlockingDeque<JedisCluster> idleClients;
	private Map<JedisCluster, Integer> allClients;
	private Set<HostAndPort> hostAndPorts;

	public JedisClusterPoolV1(int min, int max, Set<HostAndPort> hostAndPorts) {
		this.min = min;
		this.max = max;
		count = new AtomicInteger(0);
		this.hostAndPorts = hostAndPorts;
		init();
	}

	public JedisCluster getResource() {
		JedisCluster resource = idleClients.poll();
		if (resource == null) {
			if (count.get() < max) {
				create();
			}
			try {
				resource = idleClients.poll(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
				if (resource == null) {
					throw new JedisException("could not get resource from the pool");
				}
			} catch (InterruptedException e) {
				throw new JedisException("could not get resource from the pool", e);
			}
		}
		return resource;
	}

	public void returnResource(JedisCluster jedisCluster) {
		if (jedisCluster != null) {
			Integer value = allClients.get(jedisCluster);
			if (null != value) {
				int i = value;
				if (i == STATE_ACTIVITY) {
					if (!idleClients.contains(jedisCluster)) {
						idleClients.add(jedisCluster);
					}
				} else {
					allClients.remove(jedisCluster);
					idleClients.remove(jedisCluster);
					count.decrementAndGet();
				}
			}
		}
	}

	private void init() {
		allClients = new ConcurrentHashMap<JedisCluster, Integer>(max);
		idleClients = new LinkedBlockingDeque<JedisCluster>();
		while (count.get() < min) {
			create();
		}
	}

	private void create() {
		int value = count.incrementAndGet();
		if (value > max) {
			count.decrementAndGet();
		} else {
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMinIdle(5);
			JedisCluster jedisCluster = new JedisCluster(hostAndPorts,config);
			allClients.put(jedisCluster, STATE_ACTIVITY);
			idleClients.add(jedisCluster);
		}
	}

	public String getCount() {
		return "count:" + count + "\tall:" + allClients.size() + "\tidle:" + idleClients.size();
	}
}
