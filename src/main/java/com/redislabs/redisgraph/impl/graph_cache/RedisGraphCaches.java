package com.redislabs.redisgraph.impl.graph_cache;

import com.redislabs.redisgraph.RedisGraph;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisGraphCaches {

    private final Map<String, GraphCache> graphCaches = new ConcurrentHashMap<>();

    public GraphCache getGraphCache(String graphId){
        if (!graphCaches.containsKey(graphId)){
            long startTime = System.nanoTime();
            graphCaches.putIfAbsent(graphId, new GraphCache(graphId));
            long stopTime = System.nanoTime();
            System.out.println("Graph Cache Create" + (stopTime - startTime)/1000000);
        }
        return graphCaches.get(graphId);
    }

    /**
     * Returns a String which represents the name of the label mapped to the label id
     * @param graphId graph to perform the query
     * @param index label index
     * @param redisGraph RedisGraphAPI implementation
     * @return label name
     */
    public String getLabel(String graphId, int index, RedisGraph redisGraph) {
        return getGraphCache(graphId).getLabel(index, redisGraph);
    }

    /**
     * Returns a String which represents the name of the relationship mapped to the label id
     * @param graphId graph to perform the query
     * @param index relationship index
     * @param redisGraph RedisGraphAPI implementation
     * @return relationship name
     */
    public String getRelationshipType(String graphId, int index, RedisGraph redisGraph){
        return getGraphCache(graphId).getRelationshipType(index, redisGraph);
    }

    /**
     * Returns a String which represents the name of the property mapped to the label id
     * @param graphId graph to perform the query
     * @param index property index
     * @param redisGraph RedisGraphAPI implementation
     * @return property name
     */
    public String getPropertyName(String graphId, int index, RedisGraph redisGraph){
        return getGraphCache(graphId).getPropertyName(index, redisGraph);
    }

    /**
     * Removes a graph meta data cache
     * @param graphId
     */
    public void removeGraphCache(String graphId){
        graphCaches.remove(graphId);
    }
}
