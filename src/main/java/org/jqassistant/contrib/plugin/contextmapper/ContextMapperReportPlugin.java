package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import guru.nidi.graphviz.engine.Format;
import org.contextmapper.contextmap.generator.model.ContextMap;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapGenerator;
import org.jqassistant.contrib.plugin.contextmapper.report.ContextMapImageRenderer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static guru.nidi.graphviz.engine.Format.PNG;
import static guru.nidi.graphviz.engine.Format.SVG;

/**
 * Report plug-in to generate Context Maps from the graph.
 *
 * @author Stephan Pirnbaum
 */
public class ContextMapperReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILE_FORMAT = "context-mapper.report.format";

    private static final Format DEFAULT_FILE_FORMAT = SVG;

    private ReportContext reportContext;

    private File directory;

    private Format fileFormat;


    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
        this.reportContext = reportContext;
        directory = reportContext.getReportDirectory("context-mapper");
        String format = (String) properties.getOrDefault(PROPERTY_FILE_FORMAT, DEFAULT_FILE_FORMAT.fileExtension);
        switch (format) {
            case "svg": fileFormat = SVG;
                break;
            case "png": fileFormat = PNG;
                break;
            default:
                throw new ReportException("Unsupported report type: " + format);
        }
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        ContextMap diagram = new ContextMapGenerator().generateContextMap(result);
        try {
            File file = new ContextMapImageRenderer().renderDiagram(diagram, result.getRule(), directory, fileFormat);
            URL url = file.toURI().toURL();
            reportContext.addReport("Context Map", result.getRule(), ReportContext.ReportType.IMAGE, url);
        } catch (MalformedURLException e) {
            throw new ReportException("Cannot convert file to URL");
        } catch (IOException e) {
            throw new ReportException("Cannot render context map as diagram", e);
        }
    }

}
