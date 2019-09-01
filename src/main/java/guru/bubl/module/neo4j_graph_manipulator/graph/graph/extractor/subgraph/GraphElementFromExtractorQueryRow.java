/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph;

import guru.bubl.module.model.graph.GraphElementPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.FriendlyResourceFromExtractorQueryRow;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.IdentificationQueryBuilder;
import org.neo4j.driver.v1.Record;

public class GraphElementFromExtractorQueryRow {

    private Record record;
    private String key;
    private String identificationKey = IdentificationQueryBuilder.IDENTIFIER_QUERY_KEY;

    public static GraphElementFromExtractorQueryRow usingRowAndKey(
            Record record,
            String key
    ) {
        return new GraphElementFromExtractorQueryRow(
                record,
                key
        );
    }

    public static GraphElementFromExtractorQueryRow usingRowKeyAndIdentificationKey(
            Record record,
            String key,
            String identificationKey
    ) {
        return new GraphElementFromExtractorQueryRow(
                record,
                key,
                identificationKey
        );
    }

    protected GraphElementFromExtractorQueryRow(Record record, String key) {
        this.record = record;
        this.key = key;
    }

    protected GraphElementFromExtractorQueryRow(Record record, String key, String identificationKey) {
        this.record = record;
        this.key = key;
        this.identificationKey = identificationKey;
    }

    public GraphElementPojo build() {
        return new GraphElementPojo(
                FriendlyResourceFromExtractorQueryRow.usingRowAndNodeKey(
                        record,
                        key
                ).build(),
                IdentifiersFromExtractorQueryRowAsArray.usingRowAndKey(
                        record,
                        identificationKey
                ).build()
        );
    }
}
