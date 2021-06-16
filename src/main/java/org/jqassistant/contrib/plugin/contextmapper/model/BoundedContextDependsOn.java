package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("DEPENDS_ON")
public interface BoundedContextDependsOn extends BoundedContextDependencyDescriptor {

}
