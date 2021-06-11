package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import org.contextmapper.dsl.cml.CMLResource;
import org.contextmapper.dsl.contextMappingDSL.BoundedContext;
import org.contextmapper.dsl.contextMappingDSL.ContextMap;
import org.contextmapper.dsl.contextMappingDSL.CustomerSupplierRelationship;
import org.contextmapper.dsl.contextMappingDSL.Domain;
import org.contextmapper.dsl.contextMappingDSL.Partnership;
import org.contextmapper.dsl.contextMappingDSL.SharedKernel;
import org.contextmapper.dsl.contextMappingDSL.Subdomain;
import org.contextmapper.dsl.contextMappingDSL.UpstreamDownstreamRelationship;
import org.contextmapper.dsl.standalone.ContextMapperStandaloneSetup;
import org.contextmapper.dsl.standalone.StandaloneContextMapperAPI;
import org.eclipse.xtext.EcoreUtil2;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDefinesDependency;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapperDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapperFileDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.DomainDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.SubdomainDescriptor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Requires(FileDescriptor.class)
public class ContextMapperScannerPlugin extends AbstractScannerPlugin<FileResource, ContextMapperDescriptor> {
    @Override
    public boolean accepts(FileResource fileResource, String path, Scope scope) {
        return path.toLowerCase().endsWith(".cml");
    }

    @Override
    public ContextMapperDescriptor scan(FileResource fileResource, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        final Store store = context.getStore();

        StandaloneContextMapperAPI api = ContextMapperStandaloneSetup.getStandaloneAPI();
        CMLResource cml = api.loadCML(fileResource.getFile());

        ContextMapperFileDescriptor contextMapperDescriptor = store.addDescriptorType(context.getCurrentDescriptor(), ContextMapperFileDescriptor.class);

        List<DomainDescriptor> domains = EcoreUtil2.eAllOfType(cml.getContextMappingModel(), Domain.class).stream()
                .map(d -> processDomain(store, d))
                .collect(Collectors.toList());

        List<BoundedContextDescriptor> boundedContexts = EcoreUtil2.eAllOfType(cml.getContextMappingModel(), BoundedContext.class).stream()
            .map(b -> processBoundedContexts(store, domains, b))
            .collect(Collectors.toList());

        EcoreUtil2.eAllOfType(cml.getContextMappingModel(), ContextMap.class).stream()
                .map(c -> processContextMap(store, c, boundedContexts))
                .forEach(c -> contextMapperDescriptor.getContextMaps().add(c));

        return contextMapperDescriptor;
    }

    private DomainDescriptor processDomain(Store store, Domain domain) {
        DomainDescriptor domainDescriptor = store.create(DomainDescriptor.class);
        domainDescriptor.setName(domain.getName());

        domain.getSubdomains().forEach(s -> {
            SubdomainDescriptor subdomainDescriptor = store.create(SubdomainDescriptor.class);
            subdomainDescriptor.setName(s.getName());
            if (s.getType() != null) {
                subdomainDescriptor.setType(s.getType().getName());
            }
            domainDescriptor.getSubdomains().add(subdomainDescriptor);
        });

        return domainDescriptor;
    }

    private BoundedContextDescriptor processBoundedContexts(Store store, List<DomainDescriptor> domains, BoundedContext boundedContext) {
        BoundedContextDescriptor boundedContextDescriptor = store.create(BoundedContextDescriptor.class);
        boundedContextDescriptor.setName(boundedContext.getName());
        if (boundedContext.getType() != null) {
            boundedContextDescriptor.setType(boundedContext.getType().getName());
        }
        boundedContext.getImplementedDomainParts().forEach(d -> {
            if (d instanceof Subdomain) {
                getSubdomainByName(domains, d.getName()).ifPresent(descriptor -> boundedContextDescriptor.getSubdomains().add(descriptor));
            } else if (d instanceof Domain) {
                getDomainByName(domains, d.getName()).ifPresent(descriptor -> boundedContextDescriptor.getDomains().add(descriptor));
            }
        });

        return boundedContextDescriptor;
    }

    private ContextMapDescriptor processContextMap(Store store, ContextMap contextMap, List<BoundedContextDescriptor> boundedContextDescriptors) {
        ContextMapDescriptor contextMapDescriptor = store.create(ContextMapDescriptor.class);
        contextMapDescriptor.setName(contextMap.getName());
        if (contextMap.getState() != null) {
            contextMapDescriptor.setState(contextMap.getState().getName());
        }
        if (contextMap.getType() != null) {
            contextMapDescriptor.setType(contextMap.getType().getName());
        }

        contextMap.getRelationships().forEach(r -> {
            if (r instanceof UpstreamDownstreamRelationship) {
                getBoundedContextByName(boundedContextDescriptors, ((UpstreamDownstreamRelationship) r).getUpstream().getName()).ifPresent(uS -> {
                    getBoundedContextByName(boundedContextDescriptors, ((UpstreamDownstreamRelationship) r).getDownstream().getName()).ifPresent(dS -> {
                        createBoundedContextRelationship(store, uS, dS, (r instanceof CustomerSupplierRelationship) ? "C/S" : "U/D");
                    });
                });
            } else if (r instanceof SharedKernel) {
                getBoundedContextByName(boundedContextDescriptors, ((SharedKernel) r).getParticipant1().getName()).ifPresent(p1 -> {
                    getBoundedContextByName(boundedContextDescriptors, ((SharedKernel) r).getParticipant2().getName()).ifPresent(p2 -> {
                        createBoundedContextRelationship(store, p1, p2, "SK");
                    });
                });
            } else if (r instanceof Partnership) {
                getBoundedContextByName(boundedContextDescriptors, ((Partnership) r).getParticipant1().getName()).ifPresent(p1 -> {
                    getBoundedContextByName(boundedContextDescriptors, ((Partnership) r).getParticipant2().getName()).ifPresent(p2 -> {
                        createBoundedContextRelationship(store, p1, p2, "P");
                    });
                });
            }
        });

        boundedContextDescriptors.forEach(b -> contextMapDescriptor.getBoundedContexts().add(b));

        return contextMapDescriptor;
    }

    private Optional<DomainDescriptor> getDomainByName(List<DomainDescriptor> domainDescriptors, String name) {
        return domainDescriptors.stream()
                .filter(d -> name.equals(d.getName()))
                .findFirst();
    }

    private Optional<SubdomainDescriptor> getSubdomainByName(List<DomainDescriptor> domainDescriptors, String name) {
        return domainDescriptors.stream()
                .flatMap(d -> d.getSubdomains().stream())
                .filter(s -> name.equals(s.getName()))
                .findFirst();
    }

    private Optional<BoundedContextDescriptor> getBoundedContextByName(List<BoundedContextDescriptor> boundedContextDescriptors, String name) {
        return boundedContextDescriptors.stream()
                .filter(bC -> name.equals(bC.getName()))
                .findFirst();
    }

    private void createBoundedContextRelationship(Store store, BoundedContextDescriptor source, BoundedContextDescriptor target, String type) {
        BoundedContextDefinesDependency boundedContextDefinesDependency = store.create(source, BoundedContextDefinesDependency.class, target);
        boundedContextDefinesDependency.setType(type);
    }

}
