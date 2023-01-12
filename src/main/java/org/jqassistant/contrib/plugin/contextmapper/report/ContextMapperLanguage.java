package org.jqassistant.contrib.plugin.contextmapper.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.Language;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;

import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextBaseDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.DomainDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.SubdomainDescriptor;

/**
 * Context Mapper language defining supported language elements.
 *
 * @author Stephan Pirnbaum
 */
@Language
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextMapperLanguage {

    ContextMapperLanguageElement value();

    enum ContextMapperLanguageElement implements LanguageElement {
        BoundedContext {
            @Override
            public SourceProvider<BoundedContextBaseDescriptor> getSourceProvider() {
                return boundedContextDescriptor -> boundedContextDescriptor.getName();
            }
        },
        Domain {
            public SourceProvider<DomainDescriptor> getSourceProvider() {
                return domainDescriptor -> domainDescriptor.getName();
            }
        },
        Subdomain {
            public SourceProvider<SubdomainDescriptor> getSourceProvider() {
                return subdomainDescriptor -> subdomainDescriptor.getName();
            }
        };

        @Override
        public String getLanguage() {
            return "ContextMapper";
        }
    }
}
