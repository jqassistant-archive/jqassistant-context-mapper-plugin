package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.CypherExecutable;
import com.buschmais.jqassistant.core.rule.api.model.Report;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import org.contextmapper.contextmap.generator.model.BoundedContext;
import org.contextmapper.contextmap.generator.model.ContextMap;
import org.contextmapper.contextmap.generator.model.Partnership;
import org.contextmapper.contextmap.generator.model.Relationship;
import org.contextmapper.contextmap.generator.model.SharedKernel;
import org.contextmapper.contextmap.generator.model.UpstreamDownstreamRelationship;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.contextmapper.contextmap.generator.model.DownstreamPatterns.ANTICORRUPTION_LAYER;
import static org.contextmapper.contextmap.generator.model.DownstreamPatterns.CONFORMIST;
import static org.contextmapper.contextmap.generator.model.UpstreamPatterns.OPEN_HOST_SERVICE;
import static org.contextmapper.contextmap.generator.model.UpstreamPatterns.PUBLISHED_LANGUAGE;

public class ContextMapGeneratorIT extends AbstractPluginIT {

    @Test
    public void testContextMapperNodes() throws RuleException {
        File testFile = new File(getClassesDirectory(ContextMapperScannerPluginIT.class), "Insurance-Example-Stage-2.cml");

        getScanner().scan(testFile, "Insurance-Example-Stage-2.cml", DefaultScope.NONE);

        Concept concept = Concept.builder()
                .id("context-mapper:BoundedContexts")
                .severity(Severity.MINOR)
                .report(Report.builder().selectedTypes(Report.selectTypes("context-mapper-diagram")).build())
                .executable(new CypherExecutable("MATCH (bC1:BoundedContext) OPTIONAL MATCH (bC1)-[d:DEFINES_DEPENDENCY]->(bC2:BoundedContext) RETURN bC1, d, bC2"))
                .build();

        super.ruleSet.getConceptBucket().add(RuleSetBuilder.newInstance().addConcept(concept).getRuleSet().getConceptBucket());
        Result<Concept> conceptResult = super.applyConcept("context-mapper:BoundedContexts");

        store.beginTransaction();

        ContextMap contextMap = new ContextMapGenerator().generateContextMap(conceptResult);

        Map<String, BoundedContext> bCs = Stream.of("CustomerManagementContext", "CustomerSelfServiceContext", "PrintingContext", "PolicyManagementContext", "RiskManagementContext", "DebtCollection")
                .map(BoundedContext::new)
                .collect(Collectors.toMap(BoundedContext::getName, b -> b));
        assertThat(contextMap.getBoundedContexts()).containsExactlyInAnyOrderElementsOf(bCs.values());

        List<Relationship> relationships = new ArrayList<>();
        relationships.add(new UpstreamDownstreamRelationship(bCs.get("CustomerManagementContext"), bCs.get("CustomerSelfServiceContext")).setCustomerSupplier(true));
        relationships.add(new UpstreamDownstreamRelationship(bCs.get("PrintingContext"), bCs.get("CustomerManagementContext")).setUpstreamPatterns(OPEN_HOST_SERVICE, PUBLISHED_LANGUAGE).setDownstreamPatterns(ANTICORRUPTION_LAYER));
        relationships.add(new UpstreamDownstreamRelationship(bCs.get("PrintingContext"), bCs.get("PolicyManagementContext")).setUpstreamPatterns(OPEN_HOST_SERVICE, PUBLISHED_LANGUAGE).setDownstreamPatterns(ANTICORRUPTION_LAYER));
        relationships.add(new Partnership(bCs.get("RiskManagementContext"), bCs.get("PolicyManagementContext")));
        relationships.add(new UpstreamDownstreamRelationship(bCs.get("CustomerManagementContext"), bCs.get("PolicyManagementContext")).setUpstreamPatterns(OPEN_HOST_SERVICE, PUBLISHED_LANGUAGE).setDownstreamPatterns(CONFORMIST));
        relationships.add(new UpstreamDownstreamRelationship(bCs.get("PrintingContext"), bCs.get("DebtCollection")).setUpstreamPatterns(OPEN_HOST_SERVICE, PUBLISHED_LANGUAGE).setDownstreamPatterns(ANTICORRUPTION_LAYER));
        relationships.add(new SharedKernel(bCs.get("PolicyManagementContext"), bCs.get("DebtCollection")));

        assertRelationships(contextMap, relationships);

        store.commitTransaction();
    }

    @Test
    public void testJMoleculesNodes() throws RuleException {
        store.beginTransaction();

        super.query("MERGE (bC1:JMolecules:BoundedContext{name: 'bC1'})-[:DEPENDS_ON]->(bC2:JMolecules:BoundedContext{name: 'bC2'})");

        store.commitTransaction();
        Concept concept = Concept.builder()
                .id("jMolecules:BoundedContexts")
                .severity(Severity.MINOR)
                .report(Report.builder().selectedTypes(Report.selectTypes("context-mapper-diagram")).build())
                .executable(new CypherExecutable("MATCH (bC1:BoundedContext)-[d:DEPENDS_ON]->(bC2:BoundedContext) RETURN bC1, d, bC2"))
                .build();

        super.ruleSet.getConceptBucket().add(RuleSetBuilder.newInstance().addConcept(concept).getRuleSet().getConceptBucket());
        Result<Concept> conceptResult = super.applyConcept("jMolecules:BoundedContexts");

        store.beginTransaction();

        ContextMap contextMap = new ContextMapGenerator().generateContextMap(conceptResult);

        Map<String, BoundedContext> bCs = Stream.of("bC1", "bC2").map(BoundedContext::new).collect(Collectors.toMap(BoundedContext::getName, b -> b));
        assertThat(contextMap.getBoundedContexts()).containsExactlyInAnyOrderElementsOf(bCs.values());

        List<Relationship> relationships = new ArrayList<>();
        relationships.add(new UpstreamDownstreamRelationship(bCs.get("bC2"), bCs.get("bC1")));
        assertRelationships(contextMap, relationships);

        store.commitTransaction();
    }

    private void assertRelationships(ContextMap contextMap, List<Relationship> relationships) {
        assertThat(contextMap.getRelationships())
                .usingElementComparator((o1, o2) -> {
                    boolean equals = o1.getFirstParticipant().equals(o2.getFirstParticipant());
                    equals &= o1.getSecondParticipant().equals(o2.getSecondParticipant());
                    equals &= o1.getClass().equals(o2.getClass());
                    if (o1 instanceof UpstreamDownstreamRelationship && o2 instanceof UpstreamDownstreamRelationship) {
                        equals &= ((UpstreamDownstreamRelationship) o1).getUpstreamPatterns().size() == ((UpstreamDownstreamRelationship) o2).getUpstreamPatterns().size();
                        equals &= ((UpstreamDownstreamRelationship) o1).getDownstreamPatterns().size() == ((UpstreamDownstreamRelationship) o2).getDownstreamPatterns().size();
                    }
                    return equals ? 0 : -1;
                })
                .containsExactlyInAnyOrderElementsOf(relationships);
    }

}
