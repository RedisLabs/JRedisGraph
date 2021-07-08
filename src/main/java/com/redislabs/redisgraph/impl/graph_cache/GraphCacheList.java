package com.redislabs.redisgraph.impl.graph_cache;

import com.redislabs.redisgraph.Record;
import com.redislabs.redisgraph.RedisGraph;
import com.redislabs.redisgraph.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Represents a local cache of list of strings. Holds data from a specific procedure, for a specific graph.
 */
class GraphCacheList {

    private final String graphId;
    private final String procedure;
    private final List<String> data;

    /**
     *
     * @param graphId - graph id
     * @param procedure - exact procedure command
     */
    public GraphCacheList(String graphId, String procedure) {
        this.graphId = graphId;
        this.procedure = procedure;
        this.data = new CopyOnWriteArrayList<>();
    }


    /**
     * A method to return a cached item if it is in the cache, or re-validate the cache if its invalidated
     * @param index index of data item
     * @return The string value of the specific procedure response, at the given index.
     */
    public String getCachedData(int index, RedisGraph redisGraph) {
        if (index >= data.size()) {
            long startTime = System.nanoTime();
            synchronized (data){
                if (index >= data.size()) {
                    getProcedureInfo(redisGraph);
                }
            }
            long stopTime = System.nanoTime();
            System.out.println("Schema retrival" + (stopTime - startTime)/1000000);
        }
        return data.get(index);

    }

    /**
     * Auxiliary method to parse a procedure result set and refresh the cache
     */
    private void getProcedureInfo(RedisGraph redisGraph) {
        long startTime = System.nanoTime();
        ResultSet resultSet = redisGraph.callProcedure(graphId, procedure);
        long stopTime = System.nanoTime();
        System.out.println("getProcedureInfo " + procedure + " " + (stopTime - startTime)/1000000);
        List<String> newData = new ArrayList<>();
        int i = 0;
        while (resultSet.hasNext()) {
            Record record = resultSet.next();
            if(i >= data.size()){
                newData.add(record.getString(0));
            }
            i++;
        }
        data.addAll(newData);

    }
}
