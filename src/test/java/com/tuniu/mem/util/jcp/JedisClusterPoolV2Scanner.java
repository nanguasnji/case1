package com.tuniu.mem.util.jcp;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import redis.clients.jedis.JedisCluster;

public class JedisClusterPoolV2Scanner extends Thread {

	private static final long SCAN_INTERVAL = 5;

	private static final long USE_TIMEOUT = 50;

	private static final long ABANDONED_TIMEOUT = 500;

	private Map<JedisCluster, JedisClusterExtV2> allClients;

	private JedisClusterPoolV2 jcp;

	private JedisClusterPoolV2Scanner(LinkedBlockingDeque<JedisClusterExtV2> idleClients,
			Map<JedisCluster, JedisClusterExtV2> allClients, JedisClusterPoolV2 jcp) {
		this.allClients = allClients;
		this.jcp = jcp;
		this.setDaemon(true);
	}

	public void run() {
		while (true) {
			Set<Entry<JedisCluster, JedisClusterExtV2>> set = allClients.entrySet();
			for (Entry<JedisCluster, JedisClusterExtV2> entry : set) {
				JedisClusterExtV2 jce = entry.getValue();
				if (jce.idle()) {
					if ((System.currentTimeMillis() - jce.getLastUsed()) > ABANDONED_TIMEOUT) {
						System.out.println("close..." + System.currentTimeMillis() + "-" + jce.getLastUsed() + "="
								+ (System.currentTimeMillis() - jce.getLastUsed()));
						jcp.closeResource(entry.getKey());
					} else {
						jcp.returnResource(entry.getKey());
					}
				} else if (jce.occupied() && (System.currentTimeMillis() - jce.getLastUsed()) > USE_TIMEOUT) {
					jcp.returnResource(entry.getKey());
				}
			}
			try {
				Thread.sleep(SCAN_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void scan(LinkedBlockingDeque<JedisClusterExtV2> idleClients,
			Map<JedisCluster, JedisClusterExtV2> allClients, JedisClusterPoolV2 jcp) {
		new JedisClusterPoolV2Scanner(idleClients, allClients, jcp).start();
	}

}
