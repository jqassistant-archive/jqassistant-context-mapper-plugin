package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import lombok.Builder;
import lombok.Singular;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDefinesDependency;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapperDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapperFileDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.CUSTOMER_SUPPLIER;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.PARTNERSHIP;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.SHARED_KERNEL;
import static org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyType.UPSTREAM_DOWNSTREAM;
import static org.mockito.Mockito.*;

public class ContextMapperScannerPluginIT extends AbstractPluginIT {

    Map<String, BoundedContextDescriptor> bCDescriptors = new HashMap<>();

    @BeforeEach
    public void setup() {
        String[] bCnames = {"CustomerManagementContext", "CustomerSelfServiceContext", "PrintingContext", "PolicyManagementContext", "RiskManagementContext", "DebtCollection"};

        for (String bCname : bCnames) {
            BoundedContextDescriptor bC = mock(BoundedContextDescriptor.class);
            when(bC.getName()).thenReturn(bCname);
            bCDescriptors.put(bCname, bC);
        }
    }

    @Test
    public void testInsuranceExample1() {
        store.beginTransaction();
        ContextMapDescriptor contextMapDescriptor = scanFileAndAssert("Insurance-Example-Stage-1.cml");

        List<BcDependency> deps = new ArrayList<>();
        deps.add(BcDependency.builder().source("CustomerSelfServiceContext").target("CustomerManagementContext").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("CustomerManagementContext").target("PrintingContext").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("PolicyManagementContext").target("PrintingContext").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("RiskManagementContext").target("PolicyManagementContext").type(SHARED_KERNEL.getType()).build());
        deps.add(BcDependency.builder().source("PolicyManagementContext").target("CustomerManagementContext").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("DebtCollection").target("PrintingContext").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("PolicyManagementContext").target("DebtCollection").type(SHARED_KERNEL.getType()).build());

        assertDependencies(deps, contextMapDescriptor);

        store.commitTransaction();
    }

    @Test
    public void testInsuranceExample2() {
        store.beginTransaction();
        ContextMapDescriptor contextMapDescriptor = scanFileAndAssert("Insurance-Example-Stage-2.cml");

        List<BcDependency> deps = new ArrayList<>();
        deps.add(BcDependency.builder().source("CustomerSelfServiceContext").target("CustomerManagementContext").type(CUSTOMER_SUPPLIER.getType()).build());
        deps.add(BcDependency.builder().source("CustomerManagementContext").sourcePattern("ACL").target("PrintingContext").targetPattern("OHS").targetPattern("PL").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("PolicyManagementContext").sourcePattern("ACL").target("PrintingContext").targetPattern("OHS").targetPattern("PL").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("RiskManagementContext").target("PolicyManagementContext").type(PARTNERSHIP.getType()).build());
        deps.add(BcDependency.builder().source("PolicyManagementContext").sourcePattern("CF").target("CustomerManagementContext").targetPattern("OHS").targetPattern("PL").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("DebtCollection").sourcePattern("ACL").target("PrintingContext").targetPattern("OHS").targetPattern("PL").type(UPSTREAM_DOWNSTREAM.getType()).build());
        deps.add(BcDependency.builder().source("PolicyManagementContext").target("DebtCollection").type(SHARED_KERNEL.getType()).build());

        assertDependencies(deps, contextMapDescriptor);

        store.commitTransaction();
    }

    private ContextMapDescriptor scanFileAndAssert(String fileName) {
        File testFile = new File(getClassesDirectory(ContextMapperScannerPluginIT.class), fileName);
        Descriptor descriptor = getScanner().scan(testFile, fileName, DefaultScope.NONE);
        assertThat(descriptor).isInstanceOf(ContextMapperDescriptor.class);
        assertThat(descriptor).isInstanceOf(ContextMapperFileDescriptor.class);
        ContextMapperFileDescriptor contextMapper = (ContextMapperFileDescriptor) descriptor;
        assertThat(contextMapper.getContextMaps()).hasSize(1);
        assertContextMapNode(contextMapper.getContextMaps().get(0), "InsuranceContextMap", "SYSTEM_LANDSCAPE", "TO_BE");
        assertThat(contextMapper.getContextMaps().get(0).getBoundedContexts()).usingElementComparator(Comparator.comparing(BoundedContextDescriptor::getName)).containsExactlyInAnyOrderElementsOf(bCDescriptors.values());
        return contextMapper.getContextMaps().get(0);
    }

    private void assertDependencies(List<BcDependency> expectedDependencies, ContextMapDescriptor contextMapDescriptor) {
        Set<BoundedContextDefinesDependency> existingDependencies = new TreeSet<>(Comparator.comparing(o -> ((Long) o.getId())));
        for (BoundedContextDescriptor boundedContext : contextMapDescriptor.getBoundedContexts()) {
            existingDependencies.addAll(boundedContext.getSourceBoundedContextsDefines());
            existingDependencies.addAll(boundedContext.getTargetBoundedContextsDefines());
        }

        List<BoundedContextDefinesDependency> definedDeps = mockRelationships(expectedDependencies);

        assertThat(existingDependencies).usingElementComparator((o1, o2) -> {
            boolean equal = o1.getSource().getName().equals(o2.getSource().getName());
            equal &= o1.getTarget().getName().equals(o2.getTarget().getName());
            equal &= o1.getType().equals(o2.getType());
            equal &= o1.getSourceRoles().length == o2.getSourceRoles().length;
            equal &= o1.getTargetRoles().length == o2.getTargetRoles().length;
            return  equal ? 0 : -1;
        }).containsExactlyInAnyOrderElementsOf(definedDeps);
    }

    private void assertContextMapNode(ContextMapDescriptor contextMap, String name, String type, String state) {
        assertThat(contextMap.getName()).isEqualTo(name);
        assertThat(contextMap.getType()).isEqualTo(type);
        assertThat(contextMap.getState()).isEqualTo(state);
    }

    private List<BoundedContextDefinesDependency> mockRelationships(List<BcDependency> deps) {
        List<BoundedContextDefinesDependency> definedDeps = new ArrayList<>();
        for (BcDependency dep : deps) {
            BoundedContextDefinesDependency def = mock(BoundedContextDefinesDependency.class);
            when(def.getType()).thenReturn(dep.type);
            when(def.getSource()).thenReturn(bCDescriptors.get(dep.source));
            when(def.getSourceRoles()).thenReturn(dep.sourcePatterns.toArray(new String[0]));
            when(def.getTarget()).thenReturn(bCDescriptors.get(dep.target));
            when(def.getTargetRoles()).thenReturn(dep.targetPatterns.toArray(new String[0]));
            definedDeps.add(def);
        }
        return definedDeps;
    }

    @Builder
    static class BcDependency {
        String source;
        @Singular
        List<String> sourcePatterns;
        String target;
        @Singular
        List<String> targetPatterns;
        String type;
    }

}
