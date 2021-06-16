package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import guru.nidi.graphviz.engine.Format;
import org.contextmapper.contextmap.generator.model.ContextMap;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapGenerator;
import org.jqassistant.contrib.plugin.contextmapper.report.ConxtMapImageRenderer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Report plug-in to generate Context Maps from the graph.
 *
 * @author stephan.pirnbaum
 */
public class ContextMapperReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILE_FORMAT = "plantuml.report.format";

    private static final String DEFAULT_FILE_FORMAT = Format.SVG.name();

    private final SubGraphFactory subGraphFactory;

    private ReportContext reportContext;

    private File directory;

    private String fileFormat;

    /**
     * Constructor.
     */
    public ContextMapperReportPlugin() {
        this.subGraphFactory = new SubGraphFactory();
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.reportContext = reportContext;
        directory = reportContext.getReportDirectory("context-mapper");
        fileFormat = (String) properties.getOrDefault(PROPERTY_FILE_FORMAT, DEFAULT_FILE_FORMAT);

    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        // result.getRule().getReport().getProperties()
        SubGraph subGraph = this.subGraphFactory.createSubGraph(result);
        ContextMap diagram = new ContextMapGenerator().renderDiagram(subGraph);
        File file = new ConxtMapImageRenderer().renderDiagram(diagram, result.getRule(), directory, fileFormat);
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ReportException("Cannot convert file '" + file.getAbsolutePath() + "' to URL");
        }
        reportContext.addReport("Context Map", result.getRule(), ReportContext.ReportType.IMAGE, url);
    }

}
