package org.jqassistant.contrib.plugin.contextmapper.report;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.Language;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.DomainDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.SubdomainDescriptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
            public SourceProvider<BoundedContextDescriptor> getSourceProvider() {
                return new SourceProvider<BoundedContextDescriptor>() {
                    @Override
                    public String getName(BoundedContextDescriptor boundedContextDescriptor) {
                        return boundedContextDescriptor.getName();
                    }

                    @Override
                    public String getSourceFile(BoundedContextDescriptor boundedContextDescriptor) {
                        return null;
                    }

                    @Override
                    public Integer getLineNumber(BoundedContextDescriptor boundedContextDescriptor) {
                        return null;
                    }
                };
            }
        },
        Domain {
            public SourceProvider<DomainDescriptor> getSourceProvider() {
                return new SourceProvider<DomainDescriptor>() {
                    @Override
                    public String getName(DomainDescriptor domainDescriptor) {
                        return domainDescriptor.getName();
                    }

                    @Override
                    public String getSourceFile(DomainDescriptor domainDescriptor) {
                        return null;
                    }

                    @Override
                    public Integer getLineNumber(DomainDescriptor domainDescriptor) {
                        return null;
                    }
                };
            }
        },
        Subdomain {
            public SourceProvider<SubdomainDescriptor> getSourceProvider() {
                return new SourceProvider<SubdomainDescriptor>() {
                    @Override
                    public String getName(SubdomainDescriptor subdomainDescriptor) {
                        return subdomainDescriptor.getName();
                    }

                    @Override
                    public String getSourceFile(SubdomainDescriptor subdomainDescriptor) {
                        return null;
                    }

                    @Override
                    public Integer getLineNumber(SubdomainDescriptor subdomainDescriptor) {
                        return null;
                    }
                };
            }
        };

        @Override
        public String getLanguage() {
            return "ContextMapper";
        }
    }
}
