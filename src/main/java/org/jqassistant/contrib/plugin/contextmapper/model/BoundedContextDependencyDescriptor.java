package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Base descriptor for relationships between {@link BoundedContextDescriptor}s.
 *
 * @author Stephan Pirnbaum
 */
public interface BoundedContextDependencyDescriptor extends Descriptor {

    String getType();

    void setType(String type);

    String[] getSourceRoles();

    void setSourceRoles(String[] roles);

    String[] getTargetRoles();

    void setTargetRoles(String[] roles);

    @Relation.Outgoing
    BoundedContextBaseDescriptor getSource();

    @Relation.Incoming
    BoundedContextBaseDescriptor getTarget();

}
