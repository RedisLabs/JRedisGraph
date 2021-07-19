package com.redislabs.redisgraph.impl.api;

import com.redislabs.redisgraph.RedisGraph;
import com.redislabs.redisgraph.ResultSet;
import com.redislabs.redisgraph.impl.Utils;
import com.redislabs.redisgraph.impl.graph_cache.RedisGraphCaches;
import com.redislabs.redisgraph.impl.resultset.ResultSetImpl;
import redis.clients.jedis.Builder;
import redis.clients.jedis.BuilderFactory;
import redis.clients.jedis.Client;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.List;
import java.util.Map;

/**
 * This class is extending Jedis Pipeline
 */
public class RedisGraphPipeline extends Pipeline implements com.redislabs.redisgraph.RedisGraphPipeline, RedisGraphCacheHolder {

    private final RedisGraph redisGraph;
    private RedisGraphCaches caches;


    public RedisGraphPipeline(Client client, RedisGraph redisGraph){
        super.setClient(client);
        this.redisGraph = redisGraph;
    }

    /**
     * Execute a Cypher query.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @return a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> query(String graphId, String query) {
        client.sendCommand(RedisGraphCommand.QUERY, graphId, query, "--COMPACT");
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Execute a Cypher read-oly query.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @return a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> readOnlyQuery(String graphId, String query) {
        client.sendCommand(RedisGraphCommand.RO_QUERY, graphId, query, "--COMPACT");
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Execute a Cypher query with timeout.
     *
     * NOTE: timeout is simply sent to DB. Socket timeout will not be changed.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param timeout
     * @return a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> query(String graphId, String query, long timeout) {
        client.sendCommand(RedisGraphCommand.QUERY, graphId, query, "--COMPACT", "TIMEOUT", Long.toString(timeout));
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Execute a Cypher read-only query with timeout.
     *
     * NOTE: timeout is simply sent to DB. Socket timeout will not be changed.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param timeout
     * @return a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> readOnlyQuery(String graphId, String query, long timeout) {
        client.sendCommand(RedisGraphCommand.RO_QUERY, graphId, query, "--COMPACT", "TIMEOUT", Long.toString(timeout));
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Execute a Cypher query with arguments
     *
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param args
     * @return response with a result set
     * @deprecated use {@link #query(String, String, Map)} instead.
     */
    @Deprecated
    @Override
    public Response<ResultSet> query(String graphId, String query, Object ...args){
        String preparedQuery = Utils.prepareQuery(query, args);
        client.sendCommand(RedisGraphCommand.QUERY, graphId, preparedQuery, "--COMPACT");
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Executes a cypher query with parameters.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @return  a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> query(String graphId, String query, Map<String, Object> params) {
        String preparedQuery = Utils.prepareQuery(query, params);
        client.sendCommand(RedisGraphCommand.QUERY, graphId, preparedQuery, "--COMPACT");
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Executes a cypher read-only query with parameters.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @return  a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> readOnlyQuery(String graphId, String query, Map<String, Object> params) {
        String preparedQuery = Utils.prepareQuery(query, params);
        client.sendCommand(RedisGraphCommand.RO_QUERY, graphId, preparedQuery, "--COMPACT");
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Executes a cypher query with parameters and timeout.
     *
     * NOTE: timeout is simply sent to DB. Socket timeout will not be changed.
     * timeout.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @param timeout
     * @return  a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> query(String graphId, String query, Map<String, Object> params, long timeout) {
        String preparedQuery = Utils.prepareQuery(query, params);
        client.sendCommand(RedisGraphCommand.QUERY, graphId, preparedQuery, "--COMPACT", "TIMEOUT", Long.toString(timeout));
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Executes a cypher read-only query with parameters and timeout.
     *
     * NOTE: timeout is simply sent to DB. Socket timeout will not be changed.
     * timeout.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @param timeout
     * @return  a response which builds the result set with the query answer.
     */
    @Override
    public Response<ResultSet> readOnlyQuery(String graphId, String query, Map<String, Object> params, long timeout) {
        String preparedQuery = Utils.prepareQuery(query, params);
        client.sendCommand(RedisGraphCommand.RO_QUERY, graphId, preparedQuery, "--COMPACT", "TIMEOUT", Long.toString(timeout));
        return getResponse(new Builder<ResultSet>() {
            @Override
            public ResultSet build(Object o) {
                return new ResultSetImpl((List<Object>)o, redisGraph, caches.getGraphCache(graphId));
            }
        });
    }

    /**
     * Invokes stored procedures without arguments
     * @param graphId a graph to perform the query on
     * @param procedure procedure name to invoke
     * @return response with result set with the procedure data
     */
    public Response<ResultSet> callProcedure(String graphId, String procedure){
        return callProcedure(graphId, procedure, Utils.DUMMY_LIST, Utils.DUMMY_MAP);
    }

    /**
     * Invokes stored procedure with arguments
     * @param graphId a graph to perform the query on
     * @param procedure procedure name to invoke
     * @param args procedure arguments
     * @return response with result set with the procedure data
     */
    public Response<ResultSet> callProcedure(String graphId, String procedure, List<String> args  ){
        return callProcedure(graphId, procedure, args, Utils.DUMMY_MAP);
    }


    /**
     * Invoke a stored procedure
     * @param graphId a graph to perform the query on
     * @param procedure - procedure to execute
     * @param args - procedure arguments
     * @param kwargs - procedure output arguments
     * @return response with result set with the procedure data
     */
    public Response<ResultSet> callProcedure(String graphId, String procedure, List<String> args,
                                                  Map<String, List<String>> kwargs) {
        String preparedProcedure = Utils.prepareProcedure(procedure, args, kwargs);
        return query(graphId, preparedProcedure);
    }


    /**
     * Deletes the entire graph
     * @param graphId graph to delete
     * @return response with the deletion running time statistics
     */
    public Response<String> deleteGraph(String graphId){

        client.sendCommand(RedisGraphCommand.DELETE, graphId);
        Response<String> response =  getResponse(BuilderFactory.STRING);
        caches.removeGraphCache(graphId);
        return response;
    }

    @Override
    public void setRedisGraphCaches(RedisGraphCaches caches) {
        this.caches = caches;
    }
}
