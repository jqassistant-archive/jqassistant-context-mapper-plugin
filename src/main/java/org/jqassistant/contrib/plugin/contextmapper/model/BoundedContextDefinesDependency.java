package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("DEFINES_DEPENDENCY")
public interface BoundedContextDefinesDependency extends BoundedContextDependencyDescriptor {

}
