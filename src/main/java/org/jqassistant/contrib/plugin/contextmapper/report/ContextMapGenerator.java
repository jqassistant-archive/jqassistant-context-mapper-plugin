package org.jqassistant.contrib.plugin.contextmapper.report;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.Identifiable;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import com.buschmais.jqassistant.core.report.api.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.contextmapper.contextmap.generator.model.*;
import org.contextmapper.dsl.contextMappingDSL.DownstreamRole;
import org.contextmapper.dsl.contextMappingDSL.UpstreamRole;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependsOn;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperDiagram.ContextMapperDiagramBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.contextmapper.dsl.contextMappingDSL.DownstreamRole.ANTICORRUPTION_LAYER;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.*;

/**
 * Generator to create a Context Map based on a Cypher Query Result.
 *
 * @author stephan.pirnbaum
 */
@Slf4j
public class ContextMapGenerator {

    /**
     * Render a context map for the given {@link Result}.
     *
     * @param subGraph The subgraph to render.
     * @return The {@link ContextMap}.
     */
    public ContextMap renderDiagram(SubGraph subGraph) {
        ContextMapperDiagram diagram = convert(subGraph);
        ContextMap contextMap = new ContextMap();

        Map<String, BoundedContext> boundedContextMap = diagram.getBoundedContexts().stream()
                .map(b -> new BoundedContext(b.getName()))
                .peek(contextMap::addBoundedContext)
                .collect(Collectors.toMap(BoundedContext::getName, b -> b));

        diagram.getRelationships().stream()
                .map(r -> createRelationship(r, boundedContextMap))
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .forEach(contextMap::addRelationship);

        return contextMap;
    }

    private Optional<Relationship> createRelationship(BoundedContextDependencyDescriptor dependency, Map<String, BoundedContext> boundedContextMap) {
        BoundedContext source = boundedContextMap.get(dependency.getSource().getName());
        BoundedContext target = boundedContextMap.get(dependency.getTarget().getName());
        BoundedContextDependencyType type = getByType(dependency.getType());

        Relationship result = null;
        if (type == null) {
            log.warn("Unknown bounded context dependency type: {}. Skipping relation.", dependency.getType());
        } else if (type == PARTNERSHIP) {
            result = new Partnership(source, target);
        } else if (type == SHARED_KERNEL) {
            result = new SharedKernel(source, target);
        } else if (type == CUSTOMER_SUPPLIER || type == UPSTREAM_DOWNSTREAM) {
            DownstreamPatterns[] downstreamPatterns = Arrays.stream(dependency.getSourceRoles())
                    .map(DownstreamRole::get)
                    .filter(Objects::nonNull)
                    .map(r -> r == ANTICORRUPTION_LAYER ? DownstreamPatterns.ANTICORRUPTION_LAYER : r == DownstreamRole.CONFORMIST ? DownstreamPatterns.CONFORMIST : null)
                    .filter(Objects::nonNull)
                    .toArray(DownstreamPatterns[]::new);
            UpstreamPatterns[] upstreamPatterns = Arrays.stream(dependency.getTargetRoles())
                    .map(UpstreamRole::get)
                    .filter(Objects::nonNull)
                    .map(r -> r == UpstreamRole.OPEN_HOST_SERVICE ? UpstreamPatterns.OPEN_HOST_SERVICE : r == UpstreamRole.PUBLISHED_LANGUAGE ? UpstreamPatterns.PUBLISHED_LANGUAGE : null)
                    .toArray(UpstreamPatterns[]::new);
            // the source is the downstream system
            // the target is the upstream system
            result = new UpstreamDownstreamRelationship(target, source)
                    .setCustomerSupplier(type == CUSTOMER_SUPPLIER)
                    .setDownstreamPatterns(downstreamPatterns)
                    .setUpstreamPatterns(upstreamPatterns);
        }
        return Optional.ofNullable(result);
    }

    private ContextMapperDiagram convert(SubGraph subGraph) {
        ContextMapperDiagramBuilder builder = ContextMapperDiagram.builder();

        for (Node node : subGraph.getNodes().values()) {
            if (node instanceof BoundedContextDescriptor) {
                builder.boundedContext((BoundedContextDescriptor) node);
            }
        }

        for (com.buschmais.jqassistant.core.report.api.graph.model.Relationship relationship : subGraph.getRelationships().values()) {
            if (relationship instanceof BoundedContextDependencyDescriptor) {
                builder.relationship((BoundedContextDependencyDescriptor) relationship);
            }
        }

        return builder.build();
    }

}
