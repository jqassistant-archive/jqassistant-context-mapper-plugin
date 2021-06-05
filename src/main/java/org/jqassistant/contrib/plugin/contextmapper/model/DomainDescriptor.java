package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Domain")
public interface DomainDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    @Relation("HAS")
    List<SubdomainDescriptor> getSubdomains();

}
