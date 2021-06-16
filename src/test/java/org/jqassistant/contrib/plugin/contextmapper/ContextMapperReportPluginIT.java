package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.CypherExecutable;
import com.buschmais.jqassistant.core.rule.api.model.Report;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;

public class ContextMapperReportPluginIT extends AbstractPluginIT {

    //@TestStore(type = TestStore.Type.REMOTE)
    @Test
    public void scan() throws RuleException {
        store.beginTransaction();
        File testFile = new File(getClassesDirectory(ContextMapperScannerPluginIT.class), "Insurance-Example-Stage-1.cml");

        getScanner().scan(testFile, "Insurance-Example-Stage-1.cml", DefaultScope.NONE);
        store.commitTransaction();

        ReportContext reportContext = new ReportContextImpl(new File("target/"), new File("target/"));


        Concept concept = Concept.builder()
                .id("context-mapper:BoundedContexts")
                .severity(Severity.MINOR)
                .report(Report.builder().selectedTypes(Report.selectTypes("")).build())
                .executable(new CypherExecutable("MATCH (bC1:BoundedContext) OPTIONAL MATCH (bC1)-[d:DEFINES_DEPENDENCY]->(bC2:BoundedContext) RETURN bC1, d, bC2"))
                .build();

        super.ruleSet.getConceptBucket().add(RuleSetBuilder.newInstance().addConcept(concept).getRuleSet().getConceptBucket());
        Result<Concept> conceptResult = super.applyConcept("context-mapper:BoundedContexts");

        store.beginTransaction();


        ReportPlugin reportPlugin = new ContextMapperReportPlugin();
        reportPlugin.configure(reportContext, new HashMap<>());
        reportPlugin.initialize();
        reportPlugin.begin();
        reportPlugin.beginConcept(concept);
        reportPlugin.setResult(conceptResult);
        reportPlugin.endConcept();
        reportPlugin.end();
        reportPlugin.destroy();

        store.commitTransaction();
    }

}
