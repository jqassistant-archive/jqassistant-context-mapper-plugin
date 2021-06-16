package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Descriptor representing a context map with its {@link BoundedContextDescriptor}s.
 *
 * @author Stephan Pirnbaum
 */
@Label("ContextMap")
public interface ContextMapDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);

    String getState();

    void setState(String state);

    @Relation("SHOWS")
    List<BoundedContextDescriptor> getBoundedContexts();

}
