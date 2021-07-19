package com.redislabs.redisgraph;

import com.redislabs.redisgraph.Statistics.Label;
import com.redislabs.redisgraph.graph_entities.Edge;
import com.redislabs.redisgraph.graph_entities.Node;
import com.redislabs.redisgraph.graph_entities.Property;
import com.redislabs.redisgraph.impl.api.RedisGraph;
import com.redislabs.redisgraph.impl.resultset.ResultSetImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.*;

public class PipelineTest {

    private RedisGraphContextGenerator api;

    public PipelineTest() {
    }

    @Before
    public void createApi(){
        api = new RedisGraph();
    }

    @After
    public void deleteGraph() {
        api.deleteGraph("social");
        api.close();
    }

    @Test
    public void testCreateNode() {
        // Create a node    	
        ResultSet resultSet = api.query("social", "CREATE ({name:'roi',age:32})");

        Assert.assertEquals(1, resultSet.getStatistics().nodesCreated());
        Assert.assertNull(resultSet.getStatistics().getStringValue(Label.NODES_DELETED));
        Assert.assertNull(resultSet.getStatistics().getStringValue(Label.RELATIONSHIPS_CREATED));
        Assert.assertNull(resultSet.getStatistics().getStringValue(Label.RELATIONSHIPS_DELETED));
        Assert.assertEquals(2, resultSet.getStatistics().propertiesSet());
        Assert.assertNotNull(resultSet.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));


        Assert.assertFalse(resultSet.hasNext());

        try {
            resultSet.next();
            Assert.fail();
        } catch (NoSuchElementException ignored) {
        }
    }

    @Test
    public void testCreateLabeledNode() {
        // Create a node with a label
        ResultSet resultSet = api.query("social", "CREATE (:human{name:'danny',age:12})");
        Assert.assertFalse(resultSet.hasNext());
        Assert.assertEquals("1", resultSet.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertEquals("2", resultSet.getStatistics().getStringValue(Label.PROPERTIES_SET));
        Assert.assertNotNull(resultSet.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));
    }

    @Test
    public void testConnectNodes() {
        // Create both source and destination nodes
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'amit',age:30})"));

        // Connect source and destination nodes.
        ResultSet resultSet = api.query("social", "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(b)");

        Assert.assertFalse(resultSet.hasNext());
        Assert.assertNull(resultSet.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(resultSet.getStatistics().getStringValue(Label.PROPERTIES_SET));
        Assert.assertEquals(1, resultSet.getStatistics().relationshipsCreated());
        Assert.assertEquals(0, resultSet.getStatistics().relationshipsDeleted());
        Assert.assertNotNull(resultSet.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));
    }

    @Test
    public void testDeleteNodes(){
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'amit',age:30})"));
        ResultSet deleteResult = api.query("social", "MATCH (a:person) WHERE (a.name = 'roi') DELETE a");

        Assert.assertFalse(deleteResult.hasNext());
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.PROPERTIES_SET));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.RELATIONSHIPS_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.RELATIONSHIPS_DELETED));
        Assert.assertEquals(1, deleteResult.getStatistics().nodesDeleted());
        Assert.assertNotNull(deleteResult.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));

        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(a)"));
        deleteResult = api.query("social", "MATCH (a:person) WHERE (a.name = 'roi') DELETE a");

        Assert.assertFalse(deleteResult.hasNext());
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.PROPERTIES_SET));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.RELATIONSHIPS_CREATED));
        Assert.assertEquals(1, deleteResult.getStatistics().relationshipsDeleted());
        Assert.assertEquals(1, deleteResult.getStatistics().nodesDeleted());

        Assert.assertNotNull(deleteResult.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));

    }

    @Test
    public void testDeleteRelationship(){

        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'amit',age:30})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(a)"));
        ResultSet deleteResult = api.query("social", "MATCH (a:person)-[e]->() WHERE (a.name = 'roi') DELETE e");

        Assert.assertFalse(deleteResult.hasNext());
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.PROPERTIES_SET));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.RELATIONSHIPS_CREATED));
        Assert.assertNull(deleteResult.getStatistics().getStringValue(Label.NODES_DELETED));
        Assert.assertEquals(1, deleteResult.getStatistics().relationshipsDeleted());

        Assert.assertNotNull(deleteResult.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));

    }


    @Test
    public void testIndex() {
        // Create both source and destination nodes
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));

        ResultSet createIndexResult = api.query("social", "CREATE INDEX ON :person(age)");
        Assert.assertFalse(createIndexResult.hasNext());
        Assert.assertEquals(1, createIndexResult.getStatistics().indicesAdded());

        // since RediSearch as index, those action are allowed
        ResultSet createNonExistingIndexResult = api.query("social", "CREATE INDEX ON :person(age1)");
        Assert.assertFalse(createNonExistingIndexResult.hasNext());
        Assert.assertNotNull(createNonExistingIndexResult.getStatistics().getStringValue(Label.INDICES_ADDED));
        Assert.assertEquals(1, createNonExistingIndexResult.getStatistics().indicesAdded());

        ResultSet createExistingIndexResult = api.query("social", "CREATE INDEX ON :person(age)");
        Assert.assertFalse(createExistingIndexResult.hasNext());
        Assert.assertNotNull(createExistingIndexResult.getStatistics().getStringValue(Label.INDICES_ADDED));
        Assert.assertEquals(0, createExistingIndexResult.getStatistics().indicesAdded());

        ResultSet deleteExistingIndexResult = api.query("social", "DROP INDEX ON :person(age)");
        Assert.assertFalse(deleteExistingIndexResult.hasNext());
        Assert.assertNotNull(deleteExistingIndexResult.getStatistics().getStringValue(Label.INDICES_DELETED));
        Assert.assertEquals(1, deleteExistingIndexResult.getStatistics().indicesDeleted());

    }

    @Test
    public void testHeader(){

        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'amit',age:30})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(a)"));

        ResultSet queryResult = api.query("social", "MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, a.age");

        Header header = queryResult.getHeader();
        Assert.assertNotNull(header);
        Assert.assertEquals("HeaderImpl{"
            + "schemaTypes=[COLUMN_SCALAR, COLUMN_SCALAR, COLUMN_SCALAR], "
            + "schemaNames=[a, r, a.age]}", header.toString());
//        Assert.assertEquals(-1901778507, header.hashCode());
        
        List<String> schemaNames = header.getSchemaNames();

        Assert.assertNotNull(schemaNames);
        Assert.assertEquals(3, schemaNames.size());
        Assert.assertEquals("a", schemaNames.get(0));
        Assert.assertEquals("r", schemaNames.get(1));
        Assert.assertEquals("a.age", schemaNames.get(2));

    }

    @Test
    public void testRecord(){
        String name = "roi";
        int age = 32;
        double doubleValue = 3.14;
        boolean boolValue  = true;

        String place = "TLV";
        int since = 2000;

        Property<String> nameProperty = new Property<>("name", name);
        Property<Integer> ageProperty = new Property<>("age", age);
        Property<Double> doubleProperty = new Property<>("doubleValue", doubleValue);
        Property<Boolean> trueBooleanProperty = new Property<>("boolValue", true);
        Property<Boolean> falseBooleanProperty = new Property<>("boolValue", false);

        Property<String> placeProperty = new Property<>("place", place);
        Property<Integer> sinceProperty = new Property<>("since", since);

        Node expectedNode = new Node();
        expectedNode.setId(0);
        expectedNode.addLabel("person");
        expectedNode.addProperty(nameProperty);
        expectedNode.addProperty(ageProperty);
        expectedNode.addProperty(doubleProperty);
        expectedNode.addProperty(trueBooleanProperty);
        Assert.assertEquals(
            "Node{labels=[person], id=0, "
            + "propertyMap={name=Property{name='name', value=roi}, "
            + "boolValue=Property{name='boolValue', value=true}, "
            + "doubleValue=Property{name='doubleValue', value=3.14}, "
            + "age=Property{name='age', value=32}}}", expectedNode.toString());

        Edge expectedEdge = new Edge();
        expectedEdge.setId(0);
        expectedEdge.setSource(0);
        expectedEdge.setDestination(1);
        expectedEdge.setRelationshipType("knows");
        expectedEdge.addProperty(placeProperty);
        expectedEdge.addProperty(sinceProperty);
        expectedEdge.addProperty(doubleProperty);
        expectedEdge.addProperty(falseBooleanProperty);
        Assert.assertEquals("Edge{relationshipType='knows', source=0, destination=1, id=0, "
            + "propertyMap={boolValue=Property{name='boolValue', value=false}, "
            + "place=Property{name='place', value=TLV}, "
            + "doubleValue=Property{name='doubleValue', value=3.14}, "
            + "since=Property{name='since', value=2000}}}", expectedEdge.toString());

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("age", age);
        params.put("boolValue", boolValue);
        params.put("doubleValue", doubleValue);

        Assert.assertNotNull(api.query("social", "CREATE (:person{name:$name,age:$age, doubleValue:$doubleValue, boolValue:$boolValue})", params));
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'amit',age:30})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  " +
                "CREATE (a)-[:knows{place:'TLV', since:2000,doubleValue:3.14, boolValue:false}]->(b)"));

        ResultSet resultSet = api.query("social", "MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, " +
                "a.name, a.age, a.doubleValue, a.boolValue, " +
                "r.place, r.since, r.doubleValue, r.boolValue");
        Assert.assertNotNull(resultSet);


        Assert.assertEquals(0, resultSet.getStatistics().nodesCreated());
        Assert.assertEquals(0, resultSet.getStatistics().nodesDeleted());
        Assert.assertEquals(0, resultSet.getStatistics().labelsAdded());
        Assert.assertEquals(0, resultSet.getStatistics().propertiesSet());
        Assert.assertEquals(0, resultSet.getStatistics().relationshipsCreated());
        Assert.assertEquals(0, resultSet.getStatistics().relationshipsDeleted());
        Assert.assertNotNull(resultSet.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));


        Assert.assertEquals(1, resultSet.size());
        Assert.assertTrue(resultSet.hasNext());
        Record record = resultSet.next();
        Assert.assertFalse(resultSet.hasNext());

        Node node = record.getValue(0);
        Assert.assertNotNull(node);

        Assert.assertEquals(expectedNode, node);

        node = record.getValue("a");
        Assert.assertEquals(expectedNode, node);

        Edge edge = record.getValue(1);
        Assert.assertNotNull(edge);
        Assert.assertEquals(expectedEdge, edge);

        edge = record.getValue("r");
        Assert.assertEquals(expectedEdge, edge);

        Assert.assertEquals(Arrays.asList("a", "r", "a.name", "a.age", "a.doubleValue", "a.boolValue",
                "r.place", "r.since", "r.doubleValue", "r.boolValue"), record.keys());

        Assert.assertEquals(Arrays.asList(expectedNode, expectedEdge,
                name, (long)age, doubleValue, true,
                place, (long)since, doubleValue, false),
                record.values());

        Node a = record.getValue("a");
        for (String propertyName : expectedNode.getEntityPropertyNames()){
            Assert.assertEquals(expectedNode.getProperty(propertyName) ,a.getProperty(propertyName));
        }

        Assert.assertEquals( "roi", record.getString(2));
        Assert.assertEquals( "32", record.getString(3));
        Assert.assertEquals( 32L, ((Long)record.getValue(3)).longValue());
        Assert.assertEquals( 32L, ((Long)record.getValue("a.age")).longValue());
        Assert.assertEquals( "roi", record.getString("a.name"));
        Assert.assertEquals( "32", record.getString("a.age"));

    }

    @Ignore
    @Test
    public void tinyTestMultiThread(){
        ResultSet resultSet = api.query("social", "CREATE ({name:'roi',age:32})");
        api.query("social", "MATCH (a:person) RETURN a");
        for (int i =0; i < 10000; i++){
            List<ResultSet> resultSets = IntStream.range(0,16).parallel().
                    mapToObj(
                            j-> api.query("social", "MATCH (a:person) RETURN a")).
                    collect(Collectors.toList());
        }
    }

    @Test
    public void testMultiThread(){

        Assert.assertNotNull(api.query("social", "CREATE (:person {name:'roi', age:32})-[:knows]->(:person {name:'amit',age:30}) "));

        List<ResultSet> resultSets = IntStream.range(0,16).parallel().
                mapToObj(i-> api.query("social", "MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, a.age")).
                collect(Collectors.toList());

        Property<String> nameProperty = new Property<>("name", "roi");
        Property<Integer> ageProperty = new Property<>("age", 32);
        Property<String> lastNameProperty = new Property<>("lastName", "a");

        Node expectedNode = new Node();
        expectedNode.setId(0);
        expectedNode.addLabel("person");
        expectedNode.addProperty(nameProperty);
        expectedNode.addProperty(ageProperty);


        Edge expectedEdge = new Edge();
        expectedEdge.setId(0);
        expectedEdge.setSource(0);
        expectedEdge.setDestination(1);
        expectedEdge.setRelationshipType("knows");


        for (ResultSet resultSet : resultSets){
            Assert.assertNotNull(resultSet.getHeader());
            Header header = resultSet.getHeader();
            List<String> schemaNames = header.getSchemaNames();
            Assert.assertNotNull(schemaNames);
            Assert.assertEquals(3, schemaNames.size());
            Assert.assertEquals("a", schemaNames.get(0));
            Assert.assertEquals("r", schemaNames.get(1));
            Assert.assertEquals("a.age", schemaNames.get(2));
            Assert.assertEquals(1, resultSet.size());
            Assert.assertTrue(resultSet.hasNext());
            Record record = resultSet.next();
            Assert.assertFalse(resultSet.hasNext());
            Assert.assertEquals(Arrays.asList("a", "r", "a.age"), record.keys());
            Assert.assertEquals(Arrays.asList(expectedNode, expectedEdge, 32L), record.values());
        }

        //test for update in local cache
        expectedNode.removeProperty("name");
        expectedNode.removeProperty("age");
        expectedNode.addProperty(lastNameProperty);
        expectedNode.removeLabel("person");
        expectedNode.addLabel("worker");
        expectedNode.setId(2);


        expectedEdge.setRelationshipType("worksWith");
        expectedEdge.setSource(2);
        expectedEdge.setDestination(3);
        expectedEdge.setId(1);

        Assert.assertNotNull(api.query("social", "CREATE (:worker{lastName:'a'})"));
        Assert.assertNotNull(api.query("social", "CREATE (:worker{lastName:'b'})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:worker), (b:worker) WHERE (a.lastName = 'a' AND b.lastName='b')  CREATE (a)-[:worksWith]->(b)"));

        resultSets = IntStream.range(0,16).parallel().
                mapToObj(i-> api.query("social", "MATCH (a:worker)-[r:worksWith]->(b:worker) RETURN a,r")).
                collect(Collectors.toList());

        for (ResultSet resultSet : resultSets){
            Assert.assertNotNull(resultSet.getHeader());
            Header header = resultSet.getHeader();
            List<String> schemaNames = header.getSchemaNames();
            Assert.assertNotNull(schemaNames);
            Assert.assertEquals(2, schemaNames.size());
            Assert.assertEquals("a", schemaNames.get(0));
            Assert.assertEquals("r", schemaNames.get(1));
            Assert.assertEquals(1, resultSet.size());
            Assert.assertTrue(resultSet.hasNext());
            Record record = resultSet.next();
            Assert.assertFalse(resultSet.hasNext());
            Assert.assertEquals(Arrays.asList("a", "r"), record.keys());
            Assert.assertEquals(Arrays.asList(expectedNode, expectedEdge), record.values());
        }
    }


    @Test
    public void testAdditionToProcedures(){

        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'roi',age:32})"));
        Assert.assertNotNull(api.query("social", "CREATE (:person{name:'amit',age:30})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(b)"));

        //expected objects init
        Property<String> nameProperty = new Property<>("name", "roi");
        Property<Integer> ageProperty = new Property<>("age", 32);
        Property<String> lastNameProperty =new Property<>("lastName", "a");

        Node expectedNode = new Node();
        expectedNode.setId(0);
        expectedNode.addLabel("person");
        expectedNode.addProperty(nameProperty);
        expectedNode.addProperty(ageProperty);


        Edge expectedEdge = new Edge();
        expectedEdge.setId(0);
        expectedEdge.setSource(0);
        expectedEdge.setDestination(1);
        expectedEdge.setRelationshipType("knows");


        ResultSet resultSet = api.query("social", "MATCH (a:person)-[r:knows]->(b:person) RETURN a,r");
        Assert.assertNotNull(resultSet.getHeader());
        Header header = resultSet.getHeader();
        List<String> schemaNames = header.getSchemaNames();
        Assert.assertNotNull(schemaNames);
        Assert.assertEquals(2, schemaNames.size());
        Assert.assertEquals("a", schemaNames.get(0));
        Assert.assertEquals("r", schemaNames.get(1));
        Assert.assertEquals(1, resultSet.size());
        Assert.assertTrue(resultSet.hasNext());
        Record record = resultSet.next();
        Assert.assertFalse(resultSet.hasNext());
        Assert.assertEquals(Arrays.asList("a", "r"), record.keys());
        Assert.assertEquals(Arrays.asList(expectedNode, expectedEdge), record.values());

        //test for local cache updates

        expectedNode.removeProperty("name");
        expectedNode.removeProperty("age");
        expectedNode.addProperty(lastNameProperty);
        expectedNode.removeLabel("person");
        expectedNode.addLabel("worker");
        expectedNode.setId(2);
        expectedEdge.setRelationshipType("worksWith");
        expectedEdge.setSource(2);
        expectedEdge.setDestination(3);
        expectedEdge.setId(1);
        Assert.assertNotNull(api.query("social", "CREATE (:worker{lastName:'a'})"));
        Assert.assertNotNull(api.query("social", "CREATE (:worker{lastName:'b'})"));
        Assert.assertNotNull(api.query("social", "MATCH (a:worker), (b:worker) WHERE (a.lastName = 'a' AND b.lastName='b')  CREATE (a)-[:worksWith]->(b)"));
        resultSet = api.query("social", "MATCH (a:worker)-[r:worksWith]->(b:worker) RETURN a,r");
        Assert.assertNotNull(resultSet.getHeader());
        header = resultSet.getHeader();
        schemaNames = header.getSchemaNames();
        Assert.assertNotNull(schemaNames);
        Assert.assertEquals(2, schemaNames.size());
        Assert.assertEquals("a", schemaNames.get(0));
        Assert.assertEquals("r", schemaNames.get(1));
        Assert.assertEquals(1, resultSet.size());
        Assert.assertTrue(resultSet.hasNext());
        record = resultSet.next();
        Assert.assertFalse(resultSet.hasNext());
        Assert.assertEquals(Arrays.asList("a", "r"), record.keys());
        Assert.assertEquals(Arrays.asList(expectedNode, expectedEdge), record.values());

    }

    @Test
    public void testEscapedQuery() {
        Assert.assertNotNull(api.query("social", "CREATE (:escaped{s1:%s,s2:%s})", "S\"'", "S'\""));
        Assert.assertNotNull(api.query("social", "MATCH (n) where n.s1=%s and n.s2=%s RETURN n", "S\"'", "S'\""));
        Assert.assertNotNull(api.query("social", "MATCH (n) where n.s1='S\"' RETURN n"));

    }

    @Test
    public void testPipelineExec(){
        try (RedisGraphContext c = api.getContext()) {
            RedisGraphPipeline pipeline = api.getContext().pipelined();

            pipeline.set("x", "1");
            pipeline.query("social", "CREATE (:Person {name:'a'})");
            pipeline.query("g", "CREATE (:Person {name:'a'})");
            pipeline.incr("x");
            pipeline.get("x");
            pipeline.query("social", "MATCH (n:Person) RETURN n");
            pipeline.deleteGraph("g");
            pipeline.callProcedure("social", "db.labels");
            List<Object> results = pipeline.syncAndReturnAll();

            // Redis set command
            Assert.assertEquals(String.class, results.get(0).getClass());
            Assert.assertEquals("OK", results.get(0));

            // Redis graph command
            Assert.assertEquals(ResultSetImpl.class, results.get(1).getClass());
            ResultSet resultSet = (ResultSet) results.get(1);
            Assert.assertEquals(1, resultSet.getStatistics().nodesCreated());
            Assert.assertEquals(1, resultSet.getStatistics().propertiesSet());


            Assert.assertEquals(ResultSetImpl.class, results.get(2).getClass());
            resultSet = (ResultSet) results.get(2);
            Assert.assertEquals(1, resultSet.getStatistics().nodesCreated());
            Assert.assertEquals(1, resultSet.getStatistics().propertiesSet());

            // Redis incr command
            Assert.assertEquals(Long.class, results.get(3).getClass());
            Assert.assertEquals(2L, results.get(3));

            // Redis get command
            Assert.assertEquals(String.class, results.get(4).getClass());
            Assert.assertEquals("2", results.get(4));

            // Graph query result
            Assert.assertEquals(ResultSetImpl.class, results.get(5).getClass());
            resultSet = (ResultSet) results.get(5);

            Assert.assertNotNull(resultSet.getHeader());
            Header header = resultSet.getHeader();


            List<String> schemaNames = header.getSchemaNames();
            Assert.assertNotNull(schemaNames);
            Assert.assertEquals(1, schemaNames.size());
            Assert.assertEquals("n", schemaNames.get(0));

            Property<String> nameProperty = new Property<>("name", "a");

            Node expectedNode = new Node();
            expectedNode.setId(0);
            expectedNode.addLabel("Person");
            expectedNode.addProperty(nameProperty);
            // see that the result were pulled from the right graph
            Assert.assertEquals(1, resultSet.size());
            Assert.assertTrue(resultSet.hasNext());
            Record record = resultSet.next();
            Assert.assertFalse(resultSet.hasNext());
            Assert.assertEquals(Arrays.asList("n"), record.keys());
            Assert.assertEquals(expectedNode, record.getValue("n"));

            Assert.assertEquals(ResultSetImpl.class, results.get(7).getClass());
            resultSet = (ResultSet) results.get(7);

            Assert.assertNotNull(resultSet.getHeader());
            header = resultSet.getHeader();


            schemaNames = header.getSchemaNames();
            Assert.assertNotNull(schemaNames);
            Assert.assertEquals(1, schemaNames.size());
            Assert.assertEquals("label", schemaNames.get(0));

            Assert.assertEquals(1, resultSet.size());
            Assert.assertTrue(resultSet.hasNext());
            record = resultSet.next();
            Assert.assertFalse(resultSet.hasNext());
            Assert.assertEquals(Arrays.asList("label"), record.keys());
            Assert.assertEquals("Person", record.getValue("label"));
        }
    }

    @Test
    public void testMultiExecWithReadOnlyQueries(){
        try (RedisGraphContext c = api.getContext()) {
            RedisGraphPipeline pipeline = api.getContext().pipelined();
                
            pipeline.set("x", "1");
            pipeline.query("social", "CREATE (:Person {name:'a'})");
            pipeline.query("g", "CREATE (:Person {name:'a'})");
            pipeline.readOnlyQuery("social", "MATCH (n:Person) RETURN n");
            pipeline.deleteGraph("g");
            pipeline.callProcedure("social", "db.labels");
            List<Object> results = pipeline.syncAndReturnAll();

            // Redis set command
            Assert.assertEquals(String.class, results.get(0).getClass());
            Assert.assertEquals("OK", results.get(0));

            // Redis graph command
            Assert.assertEquals(ResultSetImpl.class, results.get(1).getClass());
            ResultSet resultSet = (ResultSet) results.get(1);
            Assert.assertEquals(1, resultSet.getStatistics().nodesCreated());
            Assert.assertEquals(1, resultSet.getStatistics().propertiesSet());


            Assert.assertEquals(ResultSetImpl.class, results.get(2).getClass());
            resultSet = (ResultSet) results.get(2);
            Assert.assertEquals(1, resultSet.getStatistics().nodesCreated());
            Assert.assertEquals(1, resultSet.getStatistics().propertiesSet());

            // Graph read-only query result
            Assert.assertEquals(ResultSetImpl.class, results.get(5).getClass());
            resultSet = (ResultSet) results.get(3);

            Assert.assertNotNull(resultSet.getHeader());
            Header header = resultSet.getHeader();

            List<String> schemaNames = header.getSchemaNames();
            Assert.assertNotNull(schemaNames);
            Assert.assertEquals(1, schemaNames.size());
            Assert.assertEquals("n", schemaNames.get(0));

            Property<String> nameProperty = new Property<>("name", "a");

            Node expectedNode = new Node();
            expectedNode.setId(0);
            expectedNode.addLabel("Person");
            expectedNode.addProperty(nameProperty);
            // see that the result were pulled from the right graph
            Assert.assertEquals(1, resultSet.size());
            Assert.assertTrue(resultSet.hasNext());
            Record record = resultSet.next();
            Assert.assertFalse(resultSet.hasNext());
            Assert.assertEquals(Arrays.asList("n"), record.keys());
            Assert.assertEquals(expectedNode, record.getValue("n"));

            Assert.assertEquals(ResultSetImpl.class, results.get(5).getClass());
            resultSet = (ResultSet) results.get(5);

            Assert.assertNotNull(resultSet.getHeader());
            header = resultSet.getHeader();

            schemaNames = header.getSchemaNames();
            Assert.assertNotNull(schemaNames);
            Assert.assertEquals(1, schemaNames.size());
            Assert.assertEquals("label", schemaNames.get(0));

            Assert.assertEquals(1, resultSet.size());
            Assert.assertTrue(resultSet.hasNext());
            record = resultSet.next();
            Assert.assertFalse(resultSet.hasNext());
            Assert.assertEquals(Arrays.asList("label"), record.keys());
            Assert.assertEquals("Person", record.getValue("label"));
        }
    }
}
