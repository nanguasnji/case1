package com.tuniu.mem.util.jcp;

import java.lang.Thread.State;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.TrackedUse;

import redis.clients.jedis.JedisCluster;

public class JedisClusterExtV2 implements TrackedUse {

	private JedisCluster jedisCluster;

	private Thread currentThread;

	private long lastUsed = Long.MAX_VALUE;

	/**
	 * 0-未使用；1-使用
	 */
	private AtomicInteger state;

	public JedisClusterExtV2(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
		this.state = new AtomicInteger(0);
	}

	public boolean occupied() {
		if (state.get() == 1 && currentThread != null) {
			return !State.TERMINATED.equals(currentThread.getState());
		} else {
			return false;
		}
	}

	public boolean idle() {
		return !occupied();
	}

	public Thread getThread() {
		return currentThread;
	}

	public boolean use() {
		// if(this.currentThread!=null) {
		// System.out.println("Set Error");
		// }
		if (state.incrementAndGet() == 1) {
			this.currentThread = Thread.currentThread();
			this.lastUsed = System.currentTimeMillis();
			return true;
		} else {
			state.decrementAndGet();
			return false;
		}
	}

	public void clear() {
		// if(this.currentThread!=null && this.currentThread.isAlive()) {
		// System.out.println("clear Error");
		// }
		if (state.decrementAndGet() == 0) {
			this.currentThread = null;
		} else {
			state.incrementAndGet();
		}
	}

	public JedisCluster get() {
		return this.jedisCluster;
	}

	public long getLastUsed() {
		return lastUsed;
	}
}
