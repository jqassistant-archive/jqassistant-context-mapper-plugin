package org.jqassistant.contrib.plugin.contextmapper.report;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import lombok.extern.slf4j.Slf4j;
import org.contextmapper.contextmap.generator.model.BoundedContext;
import org.contextmapper.contextmap.generator.model.ContextMap;
import org.contextmapper.contextmap.generator.model.DownstreamPatterns;
import org.contextmapper.contextmap.generator.model.Partnership;
import org.contextmapper.contextmap.generator.model.Relationship;
import org.contextmapper.contextmap.generator.model.SharedKernel;
import org.contextmapper.contextmap.generator.model.UpstreamDownstreamRelationship;
import org.contextmapper.contextmap.generator.model.UpstreamPatterns;
import org.contextmapper.dsl.contextMappingDSL.DownstreamRole;
import org.contextmapper.dsl.contextMappingDSL.UpstreamRole;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextBaseDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperDiagram.ContextMapperDiagramBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.contextmapper.dsl.contextMappingDSL.DownstreamRole.ANTICORRUPTION_LAYER;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.CUSTOMER_SUPPLIER;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.PARTNERSHIP;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.SHARED_KERNEL;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.UPSTREAM_DOWNSTREAM;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.getByType;

/**
 * Generator to create a Context Map based on a Cypher Query Result.
 *
 * @author stephan.pirnbaum
 */
@Slf4j
public class ContextMapGenerator {

    /**
     * Generate a context map for the given {@link Result}.
     *
     * @param result The result to render.
     * @return The {@link ContextMap}.
     */
    public ContextMap generateContextMap(Result<? extends ExecutableRule> result) {
        ContextMapperDiagram diagram = convert(result);
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
            DownstreamPatterns[] downstreamPatterns = Arrays.stream(dependency.getSourceRoles() != null ? dependency.getSourceRoles() : new String[0])
                    .map(DownstreamRole::get)
                    .filter(Objects::nonNull)
                    .map(r -> r == ANTICORRUPTION_LAYER ? DownstreamPatterns.ANTICORRUPTION_LAYER : r == DownstreamRole.CONFORMIST ? DownstreamPatterns.CONFORMIST : null)
                    .filter(Objects::nonNull)
                    .toArray(DownstreamPatterns[]::new);
            UpstreamPatterns[] upstreamPatterns = Arrays.stream(dependency.getTargetRoles() != null ? dependency.getTargetRoles() : new String[0])
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

    private ContextMapperDiagram convert(Result<? extends ExecutableRule> result) {
        ContextMapperDiagramBuilder builder = ContextMapperDiagram.builder();

        Set<BoundedContextBaseDescriptor> bCs = new TreeSet<>(Comparator.comparing(BoundedContextBaseDescriptor::getName));

        for (Map<String, Object> row : result.getRows()) {
            for (Object value : row.values()) {
                if (value instanceof BoundedContextDependencyDescriptor) {
                    builder.relationship((BoundedContextDependencyDescriptor) value);
                } else if (value instanceof BoundedContextBaseDescriptor) {
                    bCs.add(((BoundedContextBaseDescriptor) value));
                }
            }
        }

        builder.boundedContexts(bCs);

        return builder.build();
    }


}
