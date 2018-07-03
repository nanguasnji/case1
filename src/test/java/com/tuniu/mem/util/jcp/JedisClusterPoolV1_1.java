package com.tuniu.mem.util.jcp;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

/**
 * version 1.1 通过等待唤醒机制，使得类的分工纯粹，容器与容器控制器分离，
 * 容器负责对象状态管理和借还，控制器负责上帝视角的指令输出，但是测试后性能较差，后续再重新考虑实现方式
 * @see JedisClusterPoolListener
 * 
 * @author zhengfanming
 *
 */
public class JedisClusterPoolV1_1 {

	private final static int STATE_ACTIVITY = 1;
	private final static int MAX_WAIT_TIME = 50;
	private volatile int min;
	private volatile int max;
	private volatile AtomicInteger count;
	private LinkedBlockingDeque<JedisCluster> idleClients;
	private Map<JedisCluster, Integer> allClients;
	private Set<HostAndPort> hostAndPorts;
	private AtomicInteger waitClients;
	private JedisClusterPoolListener listener;

	public JedisClusterPoolV1_1(int min, int max, Set<HostAndPort> hostAndPorts, JedisClusterPoolListener listener) {
		this.min = min;
		this.max = max;
		count = new AtomicInteger(0);
		waitClients = new AtomicInteger(0);
		this.hostAndPorts = hostAndPorts;
		this.listener = listener;
		init();
	}

	public JedisCluster getResource() {
		JedisCluster resource = idleClients.poll();
		if (resource == null) {
			queueUp();
			try {
				synchronized (idleClients) {
					idleClients.wait(MAX_WAIT_TIME);
				}
				resource = idleClients.poll();
				if (resource == null) {
					throw new JedisException("could not get resource from the pool");
				}
			} catch (Exception e) {
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
		listener.init(this, waitClients);
		listener.start();
		allClients = new ConcurrentHashMap<JedisCluster, Integer>(max);
		idleClients = new LinkedBlockingDeque<JedisCluster>();
		while (count.get() < min) {
			create();
		}
	}

	void create() {
		int value = count.incrementAndGet();
		if (value > max) {
			count.decrementAndGet();
		} else {
			JedisCluster jedisCluster = new JedisCluster(hostAndPorts);
			allClients.put(jedisCluster, STATE_ACTIVITY);
			idleClients.add(jedisCluster);
			synchronized (idleClients) {
				idleClients.notify();
			}
		}
	}

	private void queueUp() {
		waitClients.incrementAndGet();
		synchronized (waitClients) {
			waitClients.notify();
		}

	}

	public String getCount() {
		return "count:" + count + "\tall:" + allClients.size() + "\tidle:" + idleClients.size();
	}

}
