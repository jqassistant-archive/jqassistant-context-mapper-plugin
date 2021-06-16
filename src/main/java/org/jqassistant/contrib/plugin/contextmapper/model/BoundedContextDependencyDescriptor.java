package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

public interface BoundedContextDependencyDescriptor extends Descriptor {

    String getType();

    void setType(String type);

    String[] getSourceRoles();

    void setSourceRoles(String[] roles);

    String[] getTargetRoles();

    void setTargetRoles(String[] roles);

    @Relation.Outgoing
    BoundedContextDescriptor getSource();

    @Relation.Incoming
    BoundedContextDescriptor getTarget();

}
