package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import org.contextmapper.contextmap.generator.model.ContextMap;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.util.Arrays.asList;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.SHARED_KERNEL;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.UPSTREAM_DOWNSTREAM;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class ContextMapperReportPluginIT extends AbstractPluginIT {

    //@TestStore(type = TestStore.Type.REMOTE)
    @Test
    public void scan() {
        store.beginTransaction();
        ContextMap contextMap = new ContextMapGenerator().renderDiagram(getSubGraph());
        store.commitTransaction();
    }

    private SubGraph getSubGraph() {
        Node a1 = getNode(1, "bC1", "CustomerManagementContext", "ContextMapper", "BoundedContext");
        Node a2 = getNode(2, "bC2", "CustomerSelfServiceContext", "BoundedContext");
        Node a3 = getNode(3, "bC3", "PrintingContext", "BoundedContext");
        Node a4 = getNode(4, "bC4", "PolicyManagementContext", "BoundedContext");
        Node a5 = getNode(5, "bC5", "RiskManagementContext", "BoundedContext");
        Node a6 = getNode(6, "bC6", "DebtCollection", "BoundedContext");

        // 	CustomerSelfServiceContext <- CustomerManagementContext
        Relationship r1 = getRelationship(1, a1, "DEPENDS_ON", UPSTREAM_DOWNSTREAM, a2);
        //	CustomerManagementContext <- PrintingContext
        Relationship r2 = getRelationship(2, a3, "DEPENDS_ON", UPSTREAM_DOWNSTREAM, a1);
        //	PrintingContext -> PolicyManagementContext
        Relationship r3 = getRelationship(3, a3, "DEPENDS_ON", UPSTREAM_DOWNSTREAM, a4);
        //	RiskManagementContext <-> PolicyManagementContext
        Relationship r4 = getRelationship(4, a5, "DEPENDS_ON", SHARED_KERNEL, a4);
        //	PolicyManagementContext <- CustomerManagementContext
        Relationship r5 = getRelationship(5, a1, "DEPENDS_ON", UPSTREAM_DOWNSTREAM, a4);
        //	DebtCollection <- PrintingContext
        Relationship r6 = getRelationship(6, a3, "DEPENDS_ON", UPSTREAM_DOWNSTREAM, a6);
        //	PolicyManagementContext <-> DebtCollection
        Relationship r7 = getRelationship(7, a4, "DEPENDS_ON", SHARED_KERNEL, a6);

        SubGraph subGraph = new SubGraph();
        subGraph.getNodes().put(a1.getId(), a1);
        subGraph.getNodes().put(a2.getId(), a2);
        subGraph.getNodes().put(a3.getId(), a3);
        subGraph.getNodes().put(a4.getId(), a4);
        subGraph.getNodes().put(a5.getId(), a5);
        subGraph.getNodes().put(a6.getId(), a6);
        subGraph.getRelationships().put(r1.getId(), r1);
        subGraph.getRelationships().put(r2.getId(), r2);
        subGraph.getRelationships().put(r3.getId(), r3);
        subGraph.getRelationships().put(r4.getId(), r4);
        subGraph.getRelationships().put(r5.getId(), r5);
        subGraph.getRelationships().put(r6.getId(), r6);
        subGraph.getRelationships().put(r7.getId(), r7);
        return subGraph;
    }

    private Relationship getRelationship(long id, Node start, String type, BoundedContextDependencyType depType, Node end) {
        Relationship relationship = new Relationship();
        relationship.setId(id);
        relationship.setStartNode(start);
        relationship.setEndNode(end);
        relationship.setType(type);
        relationship.getProperties().put("type", depType.getType());
        return relationship;
    }

    private BoundedContextDescriptor createBoundedContext(long id, String name) {
        BoundedContextDescriptor mock = mock(BoundedContextDescriptor.class);
        doReturn(name).when(mock).getName();
        return mock;
    }

    private Node getNode(long id, String label, String name, String... labels) {

        Node node = new Node();
        node.setId(id);
        node.setLabel(label);
        node.getProperties().put("name", name);
        node.getLabels().addAll(asList(labels));
        return node;
    }
}
