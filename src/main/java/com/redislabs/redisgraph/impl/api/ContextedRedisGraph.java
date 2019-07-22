package com.redislabs.redisgraph.impl.api;

import com.redislabs.redisgraph.RedisGraphContexted;
import com.redislabs.redisgraph.ResultSet;
import com.redislabs.redisgraph.impl.Utils;
import com.redislabs.redisgraph.impl.graph_cache.RedisGraphCaches;
import com.redislabs.redisgraph.impl.resultset.ResultSetImpl;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.SafeEncoder;

import java.util.List;

/**
 * An implementaion of RedisGraphContexted. Allows sending RedisGraph and some Redis commands,
 * within a specific connection context
 */
public class ContextedRedisGraph extends AbstractRedisGraph implements RedisGraphContexted, RedisGraphCacheHolder {

    private Jedis connectionContext;
    private RedisGraphCaches caches;

    /**
     * Generates a new instance with a specific Jedis connection
     * @param connectionContext
     */
    public ContextedRedisGraph(Jedis connectionContext){
        this.connectionContext = connectionContext;
    }

    /**
     * Overrides the abstract method. Return the instance only connection
     * @return
     */
    @Override
    protected Jedis getConnection() {
        return this.connectionContext;
    }

    /**
     * Sends the query over the instance only connection
     * @param graphId graph to be queried
     * @param preparedQuery prepared query
     * @return Result set with the query answer
     */
    @Override
    protected ResultSet sendQuery(String graphId, String preparedQuery) {
        Jedis conn = getConnection();
        List<Object> rawResponse;
        try {
            rawResponse = (List<Object>) conn.sendCommand(RedisGraphCommand.QUERY, graphId, preparedQuery, Utils.compactString);
        }
        catch (Exception e) {
            conn.close();
            throw e;
        }
        return new ResultSetImpl(rawResponse, this, caches.getGraphCache(graphId));
    }

    /**
     * @return Returns the instance Jedis connection.
     */
    @Override
    public Jedis getConnectionContext() {
        return this.connectionContext;
    }

    /**
     * Creates a new RedisGraphTransaction transactional object
     * @return new RedisGraphTransaction
     */
    @Override
    public RedisGraphTransaction multi() {
        Jedis jedis = getConnection();
        Client client = jedis.getClient();
        client.multi();
        client.getOne();
        RedisGraphTransaction transaction =  new RedisGraphTransaction(client,this);
        transaction.setRedisGraphCaches(caches);
        return transaction;
    }

    /**
     * Perfrom watch over given Redis keys
     * @param keys
     * @return "OK"
     */
    @Override
    public String watch(String... keys) {
        return this.getConnection().watch(keys);
    }

    /**
     * Removes watch from all keys
     * @return
     */
    @Override
    public String unwatch() {
        return this.getConnection().unwatch();
    }

    /**
     * Deletes the entire graph
     * @param graphId graph to delete
     * @return delete running time statistics
     */
    @Override
    public String deleteGraph(String graphId) {
        Jedis conn = getConnection();
        Object response;
        try {
            response = conn.sendCommand(RedisGraphCommand.DELETE, graphId);
        }
        catch (Exception e) {
            conn.close();
            throw e;
        }
        //clear local state
        caches.removeGraphCache(graphId);
        return SafeEncoder.encode((byte[]) response);
    }

    /**
     * closes the Jedis connection
     */
    @Override
    public void close() {
        this.connectionContext.close();

    }

    @Override
    public void setRedisGraphCaches(RedisGraphCaches caches) {
        this.caches = caches;
    }

}
