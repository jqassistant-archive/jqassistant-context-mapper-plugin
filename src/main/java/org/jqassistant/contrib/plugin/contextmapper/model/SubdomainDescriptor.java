package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Subdomain")
public interface SubdomainDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);
}
