package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for the DEFINES_DEPENDENCY relationship between {@link BoundedContextDescriptor}s.
 *
 * @author Stephan Pirnbaum
 */
@Relation("DEFINES_DEPENDENCY")
public interface BoundedContextDefinesDependency extends BoundedContextDependencyDescriptor {

}
