package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;
import java.util.Set;

/**
 * Descriptor for bounded contexts.
 *
 * @author Stephan Pirnbaum
 */
public interface BoundedContextDescriptor extends ContextMapperDescriptor, BoundedContextBaseDescriptor {

    @Relation("IMPLEMENTS")
    List<DomainDescriptor> getDomains();

    @Relation("IMPLEMENTS")
    List<SubdomainDescriptor> getSubdomains();

    @Relation.Outgoing
    Set<BoundedContextDefinesDependency> getSourceBoundedContextsDefines();

    @Relation.Incoming
    Set<BoundedContextDefinesDependency> getTargetBoundedContextsDefines();

    @Relation.Outgoing
    Set<BoundedContextDependsOn> getSourceBoundedContextsDepends();

    @Relation.Incoming
    Set<BoundedContextDependsOn> getTargetBoundedContextsDepends();

}
