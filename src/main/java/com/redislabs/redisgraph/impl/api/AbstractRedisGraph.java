package com.redislabs.redisgraph.impl.api;

import com.redislabs.redisgraph.RedisGraph;
import com.redislabs.redisgraph.ResultSet;
import com.redislabs.redisgraph.impl.Utils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * An abstract class to handle non implementation specific user requests
 */
public abstract class AbstractRedisGraph implements RedisGraph {

    /**
     * Inherited classes should return a Jedis connection, with respect to their context
     * @return Jedis connection
     */
    protected abstract Jedis getConnection();

    /**
     * Sends a query to the redis graph. Implementation and context dependent
     * @param graphId graph to be queried
     * @param preparedQuery prepared query
     * @return Result set
     */
    protected abstract ResultSet sendQuery(String graphId, String preparedQuery);

    /**
     * Sends a read-only query to the redis graph. Implementation and context dependent
     * @param graphId graph to be queried
     * @param preparedQuery prepared query
     * @return Result set
     */
    protected abstract ResultSet sendReadOnlyQuery(String graphId, String preparedQuery);

    /**
     * Sends a query to the redis graph.Implementation and context dependent
     * @param graphId graph to be queried
     * @param preparedQuery prepared query
     * @param timeout
     * @return Result set
     */
    protected abstract ResultSet sendQuery(String graphId, String preparedQuery, long timeout);

    /**
     * Sends a read-query to the redis graph.Implementation and context dependent
     * @param graphId graph to be queried
     * @param preparedQuery prepared query
     * @param timeout
     * @return Result set
     */
    protected abstract ResultSet sendReadOnlyQuery(String graphId, String preparedQuery, long timeout);

    /**
     * Execute a Cypher query.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @return a result set
     */
    public ResultSet query(String graphId, String query) {
        return sendQuery(graphId, query);
    }

    /**
     * Execute a Cypher read-only query.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @return a result set
     */
    public ResultSet readOnlyQuery(String graphId, String query) {
        return sendReadOnlyQuery(graphId, query);
    }

    /**
     * Execute a Cypher query with timeout.
     * @param graphId a graph to perform the query on
     * @param timeout
     * @param query Cypher query
     * @return a result set
     */
    @Override
    public ResultSet query(String graphId, String query, long timeout) {
        return sendQuery(graphId, query, timeout);
    }

    /**
     * Execute a Cypher read-only query with timeout.
     * @param graphId a graph to perform the query on
     * @param timeout
     * @param query Cypher query
     * @return a result set
     */
    @Override
    public ResultSet readOnlyQuery(String graphId, String query, long timeout) {
        return sendReadOnlyQuery(graphId, query, timeout);
    }

    /**
     * Execute a Cypher query with arguments
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param args
     * @return a result set
     * @deprecated use {@link #query(String, String, Map)} instead.
     */
    @Deprecated
    public ResultSet query(String graphId, String query, Object ...args) {
        String preparedQuery = Utils.prepareQuery(query, args);
        return sendQuery(graphId, preparedQuery);
    }

    /**
     * Executes a cypher query with parameters.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @return a result set.
     */
    public ResultSet query(String graphId, String query, Map<String, Object> params) {
        String preparedQuery = Utils.prepareQuery(query, params);
        return sendQuery(graphId, preparedQuery);
    }

    /**
     * Executes a cypher read-only query with parameters.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @return a result set.
     */
    public ResultSet readOnlyQuery(String graphId, String query, Map<String, Object> params) {
        String preparedQuery = Utils.prepareQuery(query, params);
        return sendReadOnlyQuery(graphId, preparedQuery);
    }

    /**
     * Executes a cypher query with parameters and timeout.
     * @param graphId a graph to perform the query on.
     * @param timeout
     * @param query Cypher query.
     * @param params parameters map.
     * @return a result set.
     */
    @Override
    public ResultSet query(String graphId, String query, Map<String, Object> params, long timeout) {
        String preparedQuery = Utils.prepareQuery(query, params);
        return sendQuery(graphId, preparedQuery, timeout);
    }

    /**
     * Executes a cypher read-only query with parameters and timeout.
     * @param graphId a graph to perform the query on.
     * @param timeout
     * @param query Cypher query.
     * @param params parameters map.
     * @return a result set.
     */
    @Override
    public ResultSet readOnlyQuery(String graphId, String query, Map<String, Object> params, long timeout) {
        String preparedQuery = Utils.prepareQuery(query, params);
        return sendReadOnlyQuery(graphId, preparedQuery, timeout);
    }

    public ResultSet callProcedure(String graphId, String procedure){
        return callProcedure(graphId, procedure, Utils.DUMMY_LIST, Utils.DUMMY_MAP);
    }

    public ResultSet callProcedure(String graphId, String procedure, List<String> args){
        return callProcedure(graphId, procedure, args, Utils.DUMMY_MAP);
    }

    public ResultSet callProcedure(String graphId, String procedure, List<String> args  , Map<String, List<String>> kwargs){

        long startTime = System.nanoTime();
        String preparedProcedure = Utils.prepareProcedure(procedure, args, kwargs);
        long stopTime = System.nanoTime();
        System.out.println("Prepare procedure " + procedure + " " + (stopTime - startTime)/1000000);
        return query(graphId, preparedProcedure);
    }
}
