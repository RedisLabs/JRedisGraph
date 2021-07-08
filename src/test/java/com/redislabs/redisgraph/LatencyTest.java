package com.redislabs.redisgraph;

import com.redislabs.redisgraph.impl.api.ContextedRedisGraph;
import com.redislabs.redisgraph.impl.api.RedisGraph;
import com.redislabs.redisgraph.impl.graph_cache.GraphCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;

public class LatencyTest {

    private RedisGraphContextGenerator api;

//    @Before
//    public void createApi(){
//        api = new RedisGraph("172.26.81.242", 7000);
//    }
//    @After
//    public void deleteGraph() {
//
//        api.deleteGraph("social");
//        api.close();
//    }

    @Test
    public void testLatency(){
        api.query("social", "CREATE(:N {a:1})-[:E]->(:N2{b:2})");

        long startTime = System.nanoTime();
        api.query("social", "MATCH(n1)-[e]->(n2) RETURN *");
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);


        startTime = System.nanoTime();
        api.query("social", "MATCH(n1)-[e]->(n2) RETURN *");
        stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);
    }

    @Test
    public void testContextLatency() {
        api.query("social", "CREATE(:N {a:1})-[:E]->(:N2{b:2})");
        ContextedRedisGraph context = (ContextedRedisGraph) api.getContext();
        long startTime = System.nanoTime();
        context.query("social", "MATCH(n1)-[e]->(n2) RETURN *");
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);

        startTime = System.nanoTime();
        context.query("social", "MATCH(n1)-[e]->(n2) RETURN *");
        stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);

        api.query("social2", "CREATE(:N {a:1})-[:E]->(:N2{b:2})");
        context = (ContextedRedisGraph) api.getContext();
        startTime = System.nanoTime();
        context.query("social2", "MATCH(n1)-[e]->(n2) RETURN *");
        stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);

        startTime = System.nanoTime();
        context.query("social2", "MATCH(n1)-[e]->(n2) RETURN *");
        stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);
    }

    @Test
    public void testCacheCreationLatency(){
        long startTime = System.nanoTime();
//        CopyOnWriteArrayList[] lists = new CopyOnWriteArrayList[6];
//        for (int i=0; i < 3; i++){
//            lists[i] = new CopyOnWriteArrayList<>();
//        }
        new GraphCache("1");
        long stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);

        startTime = System.nanoTime();
//        for (int i=0; i < 3; i++){
//            lists[i+3] = new CopyOnWriteArrayList<>();
//        }
        new GraphCache("2");

        stopTime = System.nanoTime();
        System.out.println((stopTime - startTime)/1000000);
    }

}
