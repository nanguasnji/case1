package com.tuniu.mem.util.jcp;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

/**
 * version 2 实现自动归还、超时归还（考虑多线程复用的可能性），考虑自动减少池内资源
 * 
 * 通过封装JedisCluster,包含一个调用的Thread，来判断该Thread状态是否存活，如果已经被销毁，则会认为该资源可释放
 * 在create方法中，如果已经无法增加池对象，则遍历所有对象，直到找到最近的可用对象，释放
 * 
 * @author zhengfanming
 *
 */
public class JedisClusterPoolV2 {

	private final static int MAX_WAIT_TIME = 50;
	private volatile int min;
	private volatile int max;
	private volatile AtomicInteger count;
	private LinkedBlockingDeque<JedisClusterExtV2> idleClients;
	private Map<JedisCluster, JedisClusterExtV2> allClients;
	private Set<HostAndPort> hostAndPorts;

	public JedisClusterPoolV2(int min, int max, Set<HostAndPort> hostAndPorts) {
		this.min = min;
		this.max = max;
		count = new AtomicInteger(0);
		this.hostAndPorts = hostAndPorts;
		init();
	}

	public JedisCluster getResource() {
		JedisClusterExtV2 resource = idleClients.poll();
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
		synchronized (resource) {
			resource.use();
		}
		return resource.get();
	}

	public void returnResource(JedisCluster jedisCluster) {
		// TODO 这个方法的调用应该由独立线程去执行，与扫描一样，同一时间内只允许一个归还线程操作，可能需要用到队列
		synchronized (jedisCluster) {
			if (jedisCluster != null) {
				JedisClusterExtV2 value = allClients.get(jedisCluster);
				if (null != value) {
					if (!idleClients.contains(value)) {
						value.clear();
						idleClients.add(value);
					}
				}
			}
		}
	}

	public void closeResource(JedisCluster jedisCluster) {
		// TODO 这个方法的调用应该由独立线程去执行，与扫描一样，同一时间内只允许一个归还线程操作，可能需要用到队列
		synchronized (jedisCluster) {
			JedisClusterExtV2 value;
			if (count.decrementAndGet() < min) {
				count.incrementAndGet();
				return;
			}
			if (jedisCluster != null && ((value = allClients.get(jedisCluster)) != null)
					&& idleClients.remove(value)) {
				allClients.remove(jedisCluster);
				try {
					jedisCluster.close();
				} catch (IOException e) {
					throw new JedisException("close JedisCluster exception ", e);
				}
			}else {
				System.out.println("fucking...");
			}
		}
	}

	private void init() {
		allClients = new ConcurrentHashMap<JedisCluster, JedisClusterExtV2>(max);
		idleClients = new LinkedBlockingDeque<JedisClusterExtV2>();
		while (count.get() < min) {
			create();
		}
		scan();
	}

	private void create() {
		int value = count.incrementAndGet();
		if (value > max) {
			count.decrementAndGet();
		} else {
			JedisCluster jc = new JedisCluster(hostAndPorts);
			JedisClusterExtV2 jedisClusterExtV2 = new JedisClusterExtV2(jc);
			allClients.put(jc, jedisClusterExtV2);
			idleClients.add(jedisClusterExtV2);
		}
	}

	private void scan() {
		JedisClusterPoolV2Scanner.scan(idleClients, allClients, this);
	}

	public String getCount() {
		return "count:" + count + "\tall:" + allClients.size() + "\tidle:" + idleClients.size();
	}

	public static void main(String[] args) {
		final ReentrantLock lock = new ReentrantLock();
		for (int i = 0; i < 10; i++) {
			new Thread() {
				public void run() {
					if (lock.tryLock()) {
						System.out.println("yeah!!");
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						lock.unlock();
					} else {
						System.out.println("fuck");
					}
				}
			}.start();
		}
	}
}
