package com.redislabs.redisgraph;

import redis.clients.jedis.Jedis;

public interface RedisGraphContext extends RedisGraph {


    /**
     * Returns implementing class connection context
     * @return Jedis connection
     */
    Jedis getConnectionContext();

    /**
     * Returns a Redis transactional object, over the connection context, with graph API capabilities
     * @return Redis transactional object, over the connection context, with graph API capabilities
     */
    RedisGraphTransaction multi();
    
    /**
     * Returns a Redis pipeline object, over the connection context, with graph API capabilities
     * @return Redis pipeline object, over the connection context, with graph API capabilities
     */
    RedisGraphPipeline pipelined();

    /**
     * Perform watch over given Redis keys
     * @param keys
     * @return "OK"
     */
    String watch(String... keys);

    /**
     * Removes watch from all keys
     * @return
     */
    String unwatch();
}
