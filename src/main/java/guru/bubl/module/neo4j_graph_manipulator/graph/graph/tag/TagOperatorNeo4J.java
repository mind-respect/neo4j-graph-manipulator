/*
 * Copyright Vincent Blouin under the GPL License version 3
 */

package guru.bubl.module.neo4j_graph_manipulator.graph.graph.tag;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import guru.bubl.module.model.Image;
import guru.bubl.module.model.graph.FriendlyResourcePojo;
import guru.bubl.module.model.graph.graph_element.GraphElementPojo;
import guru.bubl.module.model.graph.ShareLevel;
import guru.bubl.module.model.graph.fork.ForkOperator;
import guru.bubl.module.model.graph.relation.RelationOperator;
import guru.bubl.module.model.graph.relation.RelationPojo;
import guru.bubl.module.model.graph.tag.TagFactory;
import guru.bubl.module.model.graph.tag.TagOperator;
import guru.bubl.module.model.graph.tag.Tag;
import guru.bubl.module.model.graph.tag.TagPojo;
import guru.bubl.module.model.graph.fork.NbNeighbors;
import guru.bubl.module.model.graph.fork.NbNeighborsPojo;
import guru.bubl.module.model.graph.fork.ForkOperatorFactory;
import guru.bubl.module.neo4j_graph_manipulator.graph.OperatorNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.graph_element.GraphElementFactoryNeo4j;
import guru.bubl.module.neo4j_graph_manipulator.graph.graph.graph_element.GraphElementOperatorNeo4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.neo4j.driver.Values.parameters;

public class TagOperatorNeo4J implements TagOperator, OperatorNeo4j {

    public enum props {
        external_uri,
        identification_type,
        relation_external_uri
    }

    private GraphElementOperatorNeo4j graphElementOperator;
    private Driver driver;
    private GraphElementFactoryNeo4j graphElementOperatorFactory;
    private TagFactory tagFactory;
    protected ForkOperatorFactory forkOperatorFactory;

    @AssistedInject
    protected TagOperatorNeo4J(
            Driver driver,
            GraphElementFactoryNeo4j graphElementOperatorFactory,
            TagFactory tagFactory,
            ForkOperatorFactory forkOperatorFactory,
            @Assisted URI uri
    ) {
        this.graphElementOperator = graphElementOperatorFactory.withUri(
                uri
        );
        this.driver = driver;
        this.graphElementOperatorFactory = graphElementOperatorFactory;
        this.tagFactory = tagFactory;
        this.forkOperatorFactory = forkOperatorFactory;
    }

    @Override
    public URI getRelationExternalResourceUri() {
        String query = String.format(
                "%sRETURN n.%s as relationExternalUri",
                queryPrefix(),
                props.relation_external_uri
        );
        try (Session session = driver.session()) {
            Record record = session.run(
                    query,
                    parameters(
                            "uri",
                            uri().toString()
                    )
            ).single();
            return URI.create(
                    record.get("relationExternalUri").asString()
            );
        }
    }

    @Override
    public URI getExternalResourceUri() {
        try (Session session = driver.session()) {
            Record record = session.run(
                    queryPrefix() + "RETURN n.external_uri as externalUri",
                    parameters(
                            "uri",
                            this.uri().toString()
                    )
            ).single();
            return URI.create(
                    record.get("externalUri").asString()
            );
        }
    }

    @Override
    public void setExternalResourceUri(URI uri) {
        try (Session session = driver.session()) {
            session.run(
                    queryPrefix() + "SET n.external_uri=$external_uri",
                    parameters(
                            "uri", this.uri().toString(),
                            "external_uri", uri.toString()
                    )
            );
        }
    }

    @Override
    public TagPojo buildPojo() {
        try (Session session = driver.session()) {
            Record record = session.run(
                    queryPrefix() + "RETURN n.uri as uri, n.label as label, n.comment as comment, n.external_uri as externalUri, n.nb_private_neighbors as nbPrivateNeighbors, n.nb_friend_neighbors as nbFriendNeighbors, n.nb_public_neighbors as nbPublicNeighbors",
                    parameters(
                            "uri", this.uri().toString()
                    )
            ).single();
            FriendlyResourcePojo friendlyResourcePojo = new FriendlyResourcePojo(
                    URI.create(record.get("uri").asString()),
                    record.get("label").asString()
            );
            friendlyResourcePojo.setComment(
                    record.get("comment").asString()
            );
            return new TagPojo(
                    URI.create(record.get("externalUri").asString()),
                    new GraphElementPojo(
                            friendlyResourcePojo
                    ),
                    new NbNeighborsPojo(
                            record.get("nbPrivateNeighbors").asInt(),
                            record.get("nbFriendNeighbors").asInt(),
                            record.get("nbPublicNeighbors").asInt()
                    )
            );
        }
    }

    @Override
    public void mergeTo(Tag mergeTo) {
        try (Session session = driver.session()) {
            session.run(
                    queryPrefix() + ", (mergeTo:Resource{uri:$mergeToUri}) " +
                            "SET mergeTo.nb_private_neighbors = mergeTo.nb_private_neighbors + n.nb_private_neighbors," +
                            "mergeTo.nb_friend_neighbors = mergeTo.nb_friend_neighbors + n.nb_friend_neighbors," +
                            "mergeTo.nb_public_neighbors = mergeTo.nb_public_neighbors + n.nb_public_neighbors " +
                            "WITH n, mergeTo " +
                            "OPTIONAL MATCH (n)<-[:IDENTIFIED_TO]-(ge) " +
                            "MERGE (mergeTo)<-[:IDENTIFIED_TO]-(ge) " +
                            "DETACH DELETE n ",
                    parameters(
                            "uri", this.uri().toString(),
                            "mergeToUri", mergeTo.uri().toString()
                    )
            );
        }
    }

    @Override
    public void setShareLevel(ShareLevel shareLevel) {
        try (Session session = driver.session()) {
            session.run(
                    queryPrefix()
                            + "SET n.shareLevel=$shareLevel",
                    parameters(
                            "uri", uri().toString(),
                            "shareLevel", shareLevel.getIndex()
                    )
            );
        }
    }

    @Override
    public void setShareLevel(ShareLevel shareLevel, ShareLevel previousShareLevel) {
        this.setShareLevel(shareLevel);
    }

    @Override
    public RelationPojo addVertexAndRelation() {
        return null;
    }

    @Override
    public RelationPojo addVertexAndRelationWithIds(String vertexId, String edgeId) {
        return null;
    }

    @Override
    public RelationOperator addRelationToFork(URI destinationUri, ShareLevel sourceShareLevel, ShareLevel destinationShareLevel) {
        return null;
    }

    @Override
    public String getPrivateContext() {
        return graphElementOperator.getPrivateContext();
    }

    @Override
    public void addUpdateNotifications(String action) {
        graphElementOperator.addUpdateNotifications(action);
    }

    @Override
    public ShareLevel getShareLevel() {
        return graphElementOperator.getShareLevel();
    }

    @Override
    public NbNeighbors getNbNeighbors() {
        return forkOperatorFactory.withUri(uri()).getNbNeighbors();
    }

    @Override
    public URI uri() {
        return graphElementOperator.uri();
    }

    @Override
    public boolean hasLabel() {
        return graphElementOperator.hasLabel();
    }

    @Override
    public String label() {
        return graphElementOperator.label();
    }

    @Override
    public Set<Image> images() {
        return graphElementOperator.images();
    }

    @Override
    public Boolean gotImages() {
        return graphElementOperator.gotImages();
    }

    @Override
    public String comment() {
        return graphElementOperator.comment();
    }

    @Override
    public Boolean gotComments() {
        return graphElementOperator.gotComments();
    }

    @Override
    public Date creationDate() {
        return graphElementOperator.creationDate();
    }

    @Override
    public Date lastModificationDate() {
        return graphElementOperator.lastModificationDate();
    }

    @Override
    public void comment(String comment) {
        graphElementOperator.comment(
                comment
        );
    }

    @Override
    public void label(String label) {
        graphElementOperator.label(
                label
        );
    }

    @Override
    public void addImages(Set<Image> images) {
        graphElementOperator.addImages(
                images
        );
    }

    @Override
    public void create() {
        graphElementOperator.create();
    }

    @Override
    public void createUsingInitialValues(Map<String, Object> values) {
        graphElementOperator.createUsingInitialValues(
                values
        );
    }

    @Override
    public void remove() {
        graphElementOperator.remove();
    }

    @Override
    public void removeTag(Tag tag, ShareLevel sourceShareLevel) {

    }

    @Override
    public Map<URI, TagPojo> addTag(Tag friendlyResource, ShareLevel sourceShareLevel) {
        return new HashMap<>();
    }

    @Override
    public void setColors(String colors) {
        graphElementOperator.setColors(colors);
    }

    @Override
    public void setFont(String font) {
        graphElementOperator.setFont(font);
    }

    @Override
    public void setChildrenIndex(String childrenIndex) {
        graphElementOperator.setChildrenIndex(childrenIndex);
    }

    @Override
    public Boolean isUnderPattern() {
        return graphElementOperator.isUnderPattern();
    }

    @Override
    public Boolean isPatternOrUnderPattern() {
        return this.isUnderPattern();
    }

    @Override
    public boolean equals(Object toCompare) {
        return graphElementOperator.equals(toCompare);
    }

    @Override
    public int hashCode() {
        return graphElementOperator.hashCode();
    }


    @Override
    public Map<URI, TagPojo> getTags() {
        return null;
    }

    @Override
    public String getColors() {
        return graphElementOperator.getColors();
    }

    @Override
    public String getFont() {
        return graphElementOperator.getFont();
    }

    @Override
    public String getChildrenIndex() {
        return graphElementOperator.getChildrenIndex();
    }

    @Override
    public URI getCopiedFromUri() {
        return null;
    }
}
