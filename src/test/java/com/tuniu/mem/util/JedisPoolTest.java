package com.tuniu.mem.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import redis.clients.jedis.JedisPool;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JedisPoolTest {

	private JedisPool jc;

	private void init() {
		if (jc == null) {
			synchronized (JedisPoolTest.class) {
				GenericObjectPoolConfig config = new GenericObjectPoolConfig();
				config.setMaxTotal(5);
				config.setMaxWaitMillis(50);
				config.setTestWhileIdle(true);
				jc = new JedisPool(config,"127.0.0.1",6379);
			}
		}
	}



	@Test
	public void testGetResource4Exception() throws InterruptedException {
		init();
		final CountDownLatch latch = new CountDownLatch(100);
		final CyclicBarrier cb = new CyclicBarrier(100);

		for (int i = 0; i < 100; i++) {
			new Thread() {
				public void run() {
					long start = 0;
					long end = 0;
					try {
						cb.await();
						start = System.currentTimeMillis();
						jc.getResource();
						end = System.currentTimeMillis();
						System.out.println(
								"testGetResource4ManyConnections >>>>> " +  " during:" + (end - start));
					} catch (Exception e) {
						end = System.currentTimeMillis();
						System.out.println(
								"testGetResource4ManyConnections >>>>> " + e.toString() + " during:" + (end - start));
					}
					latch.countDown();
				}

			}.start();
		}
		latch.await();
	}

	// @Test
	// public void testQueue() throws InterruptedException {
	// final AtomicInteger count = new AtomicInteger(0);
	// final CountDownLatch latch = new CountDownLatch(100);
	// final CyclicBarrier cb = new CyclicBarrier(100);
	//
	// for(int i=0;i<100;i++) {
	// new Thread() {
	// public void run() {
	// try {
	// cb.await();
	// int value = count.incrementAndGet();
	// System.out.println(value);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// latch.countDown();
	// }
	//
	// }.start();
	// }
	//
	// latch.await();
	//
	// }
}
