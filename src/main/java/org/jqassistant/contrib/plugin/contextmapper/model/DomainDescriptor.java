package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage;

import java.util.List;

import static org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperLanguage.ContextMapperLanguageElement.Domain;

/**
 * Descriptor for domains.
 *
 * @author Stephan Pirnbaum
 */
@ContextMapperLanguage(Domain)
@Label("Domain")
public interface DomainDescriptor extends ContextMapperDescriptor {

    String getName();

    void setName(String name);

    @Relation("HAS")
    List<SubdomainDescriptor> getSubdomains();

}
