package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for the DEPENDS_ON relationship between {@link BoundedContextDescriptor}s.
 *
 * @author Stephan Pirnbaum
 */
@Relation("DEPENDS_ON")
public interface BoundedContextDependsOn extends BoundedContextDependencyDescriptor {

}
