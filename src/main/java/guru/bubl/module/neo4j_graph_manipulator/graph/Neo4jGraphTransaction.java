/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph;

import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestAPI;
import guru.bubl.module.model.GraphTransaction;

import javax.inject.Inject;

public class Neo4jGraphTransaction implements GraphTransaction {

    @Inject
    RestAPI neo4jRestApi;

    @Override
    public Object before() {
        return neo4jRestApi.beginTx();
    }

    @Override
    public void after(Object transactionAsObject) {
        Transaction transaction = (Transaction) transactionAsObject;
        transaction.success();
        transaction.close();
    }
}