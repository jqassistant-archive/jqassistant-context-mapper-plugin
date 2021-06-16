package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage;

import java.util.Set;

import static org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage.ContextMapperLanguageElement.BoundedContext;

/**
 * Descriptor for bounded contexts independent of the Context Mapper, e.g. for compatibility with jMolecules.
 *
 * @author Stephan Pirnbaum
 */
@ContextMapperLanguage(BoundedContext)
@Label("BoundedContext")
public interface BoundedContextBaseDescriptor extends Descriptor {

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);

    @Relation.Outgoing
    Set<BoundedContextDefinesDependency> getSourceBoundedContextsDefines();

    @Relation.Incoming
    Set<BoundedContextDefinesDependency> getTargetBoundedContextsDefines();

    @Relation.Outgoing
    Set<BoundedContextDependsOn> getSourceBoundedContextsDepends();

    @Relation.Incoming
    Set<BoundedContextDependsOn> getTargetBoundedContextsDepends();

}
