package com.redislabs.redisgraph;

import redis.clients.jedis.Response;
import redis.clients.jedis.commands.BasicRedisPipeline;
import redis.clients.jedis.commands.BinaryRedisPipeline;
import redis.clients.jedis.commands.BinaryScriptingCommandsPipeline;
import redis.clients.jedis.commands.ClusterPipeline;
import redis.clients.jedis.commands.MultiKeyBinaryRedisPipeline;
import redis.clients.jedis.commands.MultiKeyCommandsPipeline;
import redis.clients.jedis.commands.RedisPipeline;
import redis.clients.jedis.commands.ScriptingCommandsPipeline;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * An interface which aligned to Jedis Pipeline interface
 */
public interface RedisGraphPipeline extends
        MultiKeyBinaryRedisPipeline,
        MultiKeyCommandsPipeline, ClusterPipeline,
        BinaryScriptingCommandsPipeline, ScriptingCommandsPipeline,
        BasicRedisPipeline, BinaryRedisPipeline, RedisPipeline, Closeable {

    /**
     * Execute a Cypher query.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @return a response which builds the result set with the query answer.
     */
    Response<ResultSet> query(String graphId, String query);

    /**
     * Execute a Cypher read-only query.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @return a response which builds the result set with the query answer.
     */
    Response<ResultSet> readOnlyQuery(String graphId, String query);

    /**
     * Execute a Cypher query with timeout.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param timeout
     * @return a response which builds the result set with the query answer.
     */
    Response<ResultSet> query(String graphId, String query, long timeout);

    /**
     * Execute a Cypher read-only query with timeout.
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param timeout
     * @return a response which builds the result set with the query answer.
     */
    Response<ResultSet> readOnlyQuery(String graphId, String query, long timeout);

    /**
     * Execute a Cypher query with arguments
     * @param graphId a graph to perform the query on
     * @param query Cypher query
     * @param args
     * @return a response which builds the result set with the query answer.
     * @deprecated use {@link #query(String, String, Map)} instead.
     */
    @Deprecated
    Response<ResultSet> query(String graphId, String query, Object ...args);

    /**
     * Executes a cypher query with parameters.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @return  a response which builds the result set with the query answer.
     */
    Response<ResultSet> query(String graphId, String query, Map<String, Object> params);

    /**
     * Executes a cypher read-only query with parameters.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @return  a response which builds the result set with the query answer.
     */
    Response<ResultSet> readOnlyQuery(String graphId, String query, Map<String, Object> params);

    /**
     * Executes a cypher query with parameters and timeout.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @param timeout
     * @return  a response which builds the result set with the query answer.
     */
    Response<ResultSet> query(String graphId, String query, Map<String, Object> params, long timeout);

    /**
     * Executes a cypher read-only query with parameters and timeout.
     * @param graphId a graph to perform the query on.
     * @param query Cypher query.
     * @param params parameters map.
     * @param timeout
     * @return  a response which builds the result set with the query answer.
     */
    Response<ResultSet> readOnlyQuery(String graphId, String query, Map<String, Object> params, long timeout);

    /**
     * Invokes stored procedures without arguments
     * @param graphId a graph to perform the query on
     * @param procedure procedure name to invoke
     * @return a response which builds result set with the procedure data
     */
    Response<ResultSet> callProcedure(String graphId, String procedure);

    /**
     * Invokes stored procedure with arguments
     * @param graphId a graph to perform the query on
     * @param procedure procedure name to invoke
     * @param args procedure arguments
     * @return a response which builds result set with the procedure data
     */
    Response<ResultSet> callProcedure(String graphId, String procedure, List<String> args);

    /**
     * Invoke a stored procedure
     * @param graphId a graph to perform the query on
     * @param procedure - procedure to execute
     * @param args - procedure arguments
     * @param kwargs - procedure output arguments
     * @return a response which builds result set with the procedure data
     */
    Response<ResultSet> callProcedure(String graphId, String procedure, List<String> args  , Map<String, List<String>> kwargs);

    /**
     * Deletes the entire graph
     * @param graphId graph to delete
     * @return a response which builds the delete running time statistics
     */
    Response<String> deleteGraph(String graphId);

    
    /**
     * Synchronize pipeline by reading all responses. This operation close the pipeline. Whenever
     * possible try to avoid using this version and use Pipeline.sync() as it won't go through all the
     * responses and generate the right response type (usually it is a waste of time).
     * @return A list of all the responses in the order you executed them.
     */
    List<Object> syncAndReturnAll();
    
    /**
     * Synchronize pipeline by reading all responses. This operation close the pipeline. In order to
     * get return values from pipelined commands, capture the different Response&lt;?&gt; of the
     * commands you execute.
     */
    public void sync();
}
