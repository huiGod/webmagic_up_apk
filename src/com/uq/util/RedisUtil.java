package com.uq.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

public class RedisUtil {

	private static JedisPool jedisPool = null;
	/**
	 * 获取连接池.
	 * 
	 * @return 连接池实例
	 */
	
	static{
		if (jedisPool == null) {
			JedisPoolConfig config = new JedisPoolConfig();

			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；

			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。

			config.setMaxActive(RedisConfig.getInt("redis.maxActive"));
			// 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。

			config.setMaxIdle(RedisConfig.getInt("redis.maxIdle"));

			// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；

			config.setMaxWait(RedisConfig.getInt("redis.maxWait"));

			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；

			config.setTestOnBorrow(true);
			

			jedisPool = new JedisPool(config, RedisConfig.getString("redis.host"), RedisConfig.getInt("redis.port"));

		}
		
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}
	
	public static JedisPool getPool() {
		return jedisPool;
	}

/*	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}*/
	
	public static Jedis getJedis(){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
//            jedis.select(1);
        } catch (JedisException e) {
        	System.out.println("chucuo");
            if(jedis!=null){
            	System.out.println("redisutil 释放资源");
                jedisPool.returnBrokenResource(jedis);
            }
           throw e; 
        }
        return jedis;
    }
	
	
	  /**
     * 销毁连接
     * @param jedis
     */
    public static void returnBrokenResource(Jedis jedis) {
        if (jedis == null) {
            return;
        }
        try {
            //容错
            jedisPool.returnBrokenResource(jedis);
        } catch (Exception e) {
            System.out.println("销毁redis连接出错!");
        }
    }
    
    /**    
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {
        if (jedis == null) {
            return;
        }
        try {
            jedisPool.returnResource(jedis);
        } catch (Exception e) {
            System.out.println("返回redis资源出错!");
        }
    }

	
}
