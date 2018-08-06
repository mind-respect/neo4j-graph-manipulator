/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.meta;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import guru.bubl.module.common_utils.NoEx;
import guru.bubl.module.model.User;
import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.graph.GraphElementType;
import guru.bubl.module.model.graph.identification.IdentifierPojo;
import guru.bubl.module.model.meta.UserMetasOperator;
import guru.bubl.module.neo4j_graph_manipulator.graph.FriendlyResourceNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.identification.IdentificationNeo4j;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

public class UserMetasOperatorNeo4j implements UserMetasOperator {

    private Connection connection;
    private User user;

    @AssistedInject
    protected UserMetasOperatorNeo4j(
            Connection connection,
            @Assisted User user
    ) {
        this.connection = connection;
        this.user = user;
    }

    @Override
    public Set<IdentifierPojo> get() {
        String query = String.format(
                "START n=node:node_auto_index('" +
                        "owner:%s AND " +
                        FriendlyResourceNeo4j.props.type + ":%s " +
                        "') " +
                        "return n.%s as label, n.%s as nbReferences, n.%s as uri;",
                user.username(),
                GraphElementType.meta,
                FriendlyResourceNeo4j.props.label,
                IdentificationNeo4j.props.nb_references,
                FriendlyResourceNeo4j.props.uri
        );
        Set<IdentifierPojo> userMetas = new HashSet<>();
        return NoEx.wrap(() -> {
            ResultSet rs = connection.createStatement().executeQuery(
                    query
            );
            while (rs.next()) {
                userMetas.add(
                        new IdentifierPojo(
                                new Integer(rs.getString("nbReferences")),
                                new FriendlyResourcePojo(
                                        URI.create(rs.getString("uri")),
                                        rs.getString("label")
                                )
                        )
                );
            }
            return userMetas;
        }).get();
    }
}