/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.search.result_builder;

import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.model.search.GraphElementSearchResult;
import guru.bubl.module.model.search.GraphElementSearchResultPojo;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.extractor.subgraph.GraphElementFromExtractorQueryRow;
import org.neo4j.driver.v1.Record;

public class PropertySearchResultBuilder implements SearchResultBuilder {

    private Record row;
    private String prefix;

    public PropertySearchResultBuilder(Record row, String prefix) {
        this.row = row;
        this.prefix = prefix;
    }

    @Override
    public GraphElementSearchResult build() {
        return new GraphElementSearchResultPojo(
                GraphElementType.Property,
                GraphElementFromExtractorQueryRow.usingRowAndKey(row, prefix).build(),
                getContext()
        );
    }

    @Override
    public Record getRow() {
        return row;
    }
}
