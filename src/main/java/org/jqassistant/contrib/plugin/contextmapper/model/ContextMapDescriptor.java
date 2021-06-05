package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("ContextMap")
public interface ContextMapDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);

    String getState();

    void setState(String state);

    @Relation("CONTAINS")
    List<BoundedContextDescriptor> getBoundedContexts();

}
