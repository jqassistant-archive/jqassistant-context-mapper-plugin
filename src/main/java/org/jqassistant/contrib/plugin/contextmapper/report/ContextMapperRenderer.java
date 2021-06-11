package org.jqassistant.contrib.plugin.contextmapper.report;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.Identifiable;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import org.contextmapper.contextmap.generator.model.BoundedContext;
import org.contextmapper.contextmap.generator.model.ContextMap;
import org.contextmapper.dsl.contextMappingDSL.impl.DomainImpl;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.DomainDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.SubdomainDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperDiagram;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperDiagram.ContextMapperDiagramBuilder;

import java.util.Map;
import java.util.stream.Collectors;

public class ContextMapperRenderer {

    private final SubGraphFactory subGraphFactory;

    public ContextMapperRenderer() {
        this.subGraphFactory = new SubGraphFactory();
    }

    public String renderDiagram(Result<? extends ExecutableRule> result) throws ReportException {
        SubGraph subGraph = new SubGraphFactory().createSubGraph(result);

        ContextMapperDiagramBuilder builder = ContextMapperDiagram.builder();

        // supported labels: Domain, Subdomain, BoundedContext
        for (Map<String, Object> row : result.getRows()) {
            for (Object value : row.values()) {
                convert(value, builder);
            }
        }

        ContextMapperDiagram diagram = builder.build();

        ContextMap contextMap = new ContextMap();

        Map<String, BoundedContext> boundedContextMap = diagram.getBoundedContexts().stream()
                .map(b -> new BoundedContext(b.getName()))
                .peek(contextMap::addBoundedContext)
                .collect(Collectors.toMap(BoundedContext::getName, b -> b));

        diagram.getRelationships().stream()
                .filter(r -> diagram.getBoundedContexts().contains(r.getStartNode()))
                .forEach(r -> {

                });


        return null;
    }

    private void convert(Object node, ContextMapperDiagramBuilder builder) throws ReportException {
        if (node instanceof Iterable<?>) {
            for (Object n : ((Iterable<?>) node)) {
                convert(n, builder);
            }
        } else {
            Identifiable identifiable = this.subGraphFactory.toIdentifiable(node);
            if (identifiable instanceof Node) {
                if (identifiable instanceof BoundedContextDescriptor) {
                    builder.boundedContext((BoundedContextDescriptor) identifiable);
                }
            } else if (identifiable instanceof Relationship) {
                builder.relationship((Relationship) identifiable);
            }
        }

    }

}
