package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation("DEFINES_DEPENDENCY")
public interface BoundedContextDefinesDependency extends Descriptor {

    String getType();

    void setType(String type);

    @Relation.Outgoing
    BoundedContextDescriptor getSource();

    @Relation.Incoming
    BoundedContextDescriptor getTarget();

}
