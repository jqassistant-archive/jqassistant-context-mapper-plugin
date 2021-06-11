package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import guru.nidi.graphviz.engine.Format;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapperRenderer;
import org.jqassistant.contrib.plugin.contextmapper.report.ConxtMapImageRenderer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class ContextMapperReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILE_FORMAT = "plantuml.report.format";
    private static final String PROPERTY_RENDER_MODE = "plantuml.report.rendermode";

    private static final String DEFAULT_FILE_FORMAT = Format.SVG.name();

    private ReportContext reportContext;

    private File directory;

    private String fileFormat;


    @Override
    public void initialize() {
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
        this.reportContext = reportContext;
        directory = reportContext.getReportDirectory("context-mapper");
        fileFormat = (String) properties.getOrDefault(PROPERTY_FILE_FORMAT, DEFAULT_FILE_FORMAT);
        String renderModeValue = (String) properties.getOrDefault(PROPERTY_RENDER_MODE, null);
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        String diagram = new ContextMapperRenderer().renderDiagram(result);
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
