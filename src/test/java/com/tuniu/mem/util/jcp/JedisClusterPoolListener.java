package com.tuniu.mem.util.jcp;

import java.util.concurrent.atomic.AtomicInteger;

public class JedisClusterPoolListener extends Thread {

	private JedisClusterPoolV1_1 jedisClusterPool;

	private AtomicInteger waitClients;

	public void run() {
		while(true) {
			try {
				synchronized (waitClients) {
					waitClients.wait();
				}
				jedisClusterPool.create();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void init(JedisClusterPoolV1_1 jedisClusterPool, AtomicInteger waitClients) {
		this.jedisClusterPool = jedisClusterPool;
		this.waitClients = waitClients;
		this.setDaemon(true);
	}
}
