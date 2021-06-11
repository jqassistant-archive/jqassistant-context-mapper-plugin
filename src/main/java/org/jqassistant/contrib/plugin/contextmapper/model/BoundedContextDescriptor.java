package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage;

import java.util.List;
import java.util.Set;

import static org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage.ContextMapperLanguageElement.BoundedContext;

@ContextMapperLanguage(BoundedContext)
@Label("BoundedContext")
public interface BoundedContextDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);

    @Relation("IMPLEMENTS")
    List<DomainDescriptor> getDomains();

    @Relation("IMPLEMENTS")
    List<SubdomainDescriptor> getSubdomains();

    @Relation.Outgoing
    Set<BoundedContextDefinesDependency> getSourceBoundedContexts();

    @Relation.Incoming
    Set<BoundedContextDefinesDependency> getTargetBoundedContexts();

}
