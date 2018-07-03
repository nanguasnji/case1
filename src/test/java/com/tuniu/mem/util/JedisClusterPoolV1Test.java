package com.tuniu.mem.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tuniu.mem.util.jcp.JedisClusterPoolV1;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JedisClusterPoolV1Test {

	private JedisClusterPoolV1 jcp;

	private Set<HostAndPort> getHosts() {
		Set<HostAndPort> sets = new HashSet<HostAndPort>();
		 HostAndPort hap = new HostAndPort("47.88.61.71", 7000);
//		HostAndPort hap = new HostAndPort("172.31.176.81", 6379);

		sets.add(hap);
		return sets;
	}

	private void initJedisClusterPool() {
		if (jcp == null) {
			synchronized (JedisClusterPoolV1Test.class) {
				jcp = new JedisClusterPoolV1(10, 10, getHosts());
			}
		}
	}

	@Test
	public void testConstuctor() {
		initJedisClusterPool();
		System.out.println("testConstuctor >>>>> " + jcp.getCount());
	}

	@Test
	public void testGetResource2SetValue() {
		initJedisClusterPool();
		JedisCluster jc = jcp.getResource();
		jc.set("random", Integer.toString((int) (Math.random() * 100)));
		jcp.returnResource(jc);
		System.out.println("testGetResource2SetValue >>>>> " + jcp.getCount());
	}

	@Test
	public void testGetResourceAndReturned() {
		initJedisClusterPool();
		for (int i = 0; i < 10; i++) {
			try {
				JedisCluster jc = jcp.getResource();
				System.out.println("testGetResourceAndReturned >>>>> " + jcp.getCount());
				jcp.returnResource(jc);
			} catch (Exception e) {
				System.out.println("testGetResourceAndReturned >>>>> " + e.toString());
			}
		}
	}

	@Test
	public void testGetResource4WaitAndException() {
		initJedisClusterPool();
		int count = 100;
		final CountDownLatch latch = new CountDownLatch(count);
		final CyclicBarrier cb = new CyclicBarrier(count);
		for (int i = 0; i < count; i++) {
			new Thread() {
				public void run() {
					try {
						cb.await();
						long start = System.currentTimeMillis();
						JedisCluster jc = jcp.getResource();
						long end = System.currentTimeMillis();
						System.out.println("T1 >>>>> " + jcp.getCount() + "\t during:" + (end - start));
						Thread.sleep(10);
						jcp.returnResource(jc);
					} catch (Exception e) {
						System.out.println("T1 >>>>> " + e.toString());
					} finally {
						latch.countDown();
					}
				}
			}.start();
		}
		try {
			latch.await();
			System.out.println("T1 final >>>>> " + jcp.getCount());
		} catch (InterruptedException e) {
			System.out.println("testGetResource4WaitAndException >>>>> " + jcp.getCount());
		}
	}

	@Test
	public void testReturnResource4null() {
		initJedisClusterPool();
		jcp.returnResource(null);
		System.out.println("testReturnResource4null >>>>> " + jcp.getCount());
	}

	@Test
	public void testReturnResource4Others() {
		initJedisClusterPool();
		JedisCluster jc = new JedisCluster(getHosts());
		jcp.returnResource(jc);
		System.out.println("testReturnResource4Others >>>>> " + jcp.getCount());
	}

	@Test
	public void testReturnResource4Twice() {
		initJedisClusterPool();
		JedisCluster jc = jcp.getResource();
		jcp.returnResource(jc);
		jcp.returnResource(jc);
		System.out.println("testReturnResource4Twice >>>>> " + jcp.getCount());
	}

	@Test
	public void testGetResource4Exception() throws InterruptedException {
		initJedisClusterPool();
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
						jcp.getResource().set("random", "ok");
						end = System.currentTimeMillis();
						System.out.println(
								"testGetResource4ManyConnections >>>>> " + jcp.getCount() + " during:" + (end - start));
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

}
