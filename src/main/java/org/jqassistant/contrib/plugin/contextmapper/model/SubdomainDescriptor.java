package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage;

import static org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage.ContextMapperLanguageElement.Subdomain;

@ContextMapperLanguage(Subdomain)
@Label("Subdomain")
public interface SubdomainDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);
}
