/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.test;

import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.GraphFactory;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.subgraph.SubGraphPojo;
import guru.bubl.module.model.graph.subgraph.UserGraph;
import guru.bubl.module.model.graph.vertex.VertexOperator;
import guru.bubl.module.model.test.GraphComponentTest;
import guru.bubl.module.model.test.scenarios.GraphElementsOfTestScenario;
import guru.bubl.module.model.test.scenarios.TestScenarios;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.UserGraphFactoryNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.SubGraphExtractorFactoryNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.vertex.VertexFactoryNeo4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import javax.inject.Inject;

public class GraphComponentTestNeo4j implements GraphComponentTest {

    @Inject
    protected TestScenarios testScenarios;

    @Inject
    protected SubGraphExtractorFactoryNeo4j neo4jSubGraphExtractorFactory;

    @Inject
    protected VertexFactoryNeo4j vertexFactory;

    @Inject
    protected UserGraphFactoryNeo4j neo4jUserGraphFactory;

    @Inject
    protected GraphFactory graphFactory;

    @Inject
    Driver driver;

    protected VertexOperator vertexA;
    protected VertexOperator vertexB;
    protected VertexOperator vertexC;

    protected static User user;

    protected static User anotherUser;
    protected static UserGraph anotherUserGraph;
    protected static VertexOperator vertexOfAnotherUser;

    protected UserGraph userGraph;

    @Override
    public void beforeClass() {
    }

    @Override
    public void before() {
        user = User.withEmail(
                "roger.lamothe@example.org"
        ).setUsername("roger_lamothe");
        anotherUser = User.withEmail(
                "colette.armande@example.org"
        ).setUsername("colette_armande");

        userGraph = neo4jUserGraphFactory.withUser(user);
        GraphElementsOfTestScenario graphElementsOfTestScenario = testScenarios.buildTestScenario(
                userGraph
        );
        vertexA = graphElementsOfTestScenario.getVertexA();
        vertexB = graphElementsOfTestScenario.getVertexB();
        vertexC = graphElementsOfTestScenario.getVertexC();
        anotherUserGraph = neo4jUserGraphFactory.withUser(anotherUser);
        vertexOfAnotherUser = vertexFactory.withUri(
                anotherUserGraph.createVertex().uri()
        );
        vertexOfAnotherUser.label("vertex of another user");
    }

    @Override
    public void after() {
    }

    @Override
    public void afterClass() {
        removeWholeGraph();
    }

    @Override
    public int numberOfEdgesAndVertices() {
        return numberOfVertices() +
                numberOfEdges();
    }

    @Override
    public SubGraphPojo wholeGraphAroundDefaultCenterVertex() {
        Integer depthThatShouldCoverWholeGraph = 1000;
        return neo4jSubGraphExtractorFactory.withCenterVertexInShareLevelsAndDepth(
                vertexA.uri(),
                depthThatShouldCoverWholeGraph,
                ShareLevel.allShareLevelsInt
        ).load();
    }

    @Override
    public void removeGraphElements() {
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (n:GraphElement) DETACH DELETE n"
            );
        }
    }

    @Override
    public void removeUsers() {
        try (Session session = driver.session()) {
            session.run(
                    "MATCH (n:User) DETACH DELETE n"
            );
        }
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public void user(User user) {
        this.user = user;
    }

    @Override
    public UserGraph userGraph() {
        return userGraph;
    }

    @Override
    public VertexOperator vertexA() {
        return vertexFactory.withUri(
                vertexA.uri()
        );
    }

    @Override
    public void setDefaultVertexAkaVertexA(VertexOperator vertexA) {
        this.vertexA = vertexA;
    }

    @Override
    public VertexOperator vertexB() {
        return vertexFactory.withUri(
                vertexB.uri()
        );
    }

    @Override
    public VertexOperator vertexC() {
        return vertexFactory.withUri(
                vertexC.uri()
        );
    }

    @Override
    public VertexOperator vertexOfAnotherUser() {
        return vertexOfAnotherUser;
    }

    protected int numberOfVertices() {
        try (Session session = driver.session()) {
            Record record = session.run("MATCH (n:Vertex) return count(n) as nbVertices").single();
            return record.get("nbVertices").asInt();
        }
    }

    protected int numberOfEdges() {
        try (Session session = driver.session()) {
            Record record = session.run("MATCH (n:Edge) return count(n) as nbEdges").single();
            return record.get("nbEdges").asInt();
        }
    }
}
