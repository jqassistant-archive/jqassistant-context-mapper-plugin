package org.jqassistant.contrib.plugin.contextmapper.report;

import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import guru.nidi.graphviz.engine.Format;
import org.contextmapper.contextmap.generator.ContextMapGenerator;
import org.contextmapper.contextmap.generator.model.ContextMap;

import java.io.File;
import java.io.IOException;

/**
 * Renderer to create an image from a {@link ContextMap}.
 *
 * @author Stephan Pirnbaum
 */
public class ContextMapImageRenderer {

    /**
     * Renders the diagram for the given {@link ContextMap}.
     *
     * @param map The map to render.
     * @param rule The rule, the map resulted from.
     * @param directory The directory used for storing the image.
     * @param fileFormat The format of the image, supported are PNG and SVG.
     *
     * @return The {@link File} representing the rendered image.
     *
     * @throws IOException In case the image could not be generated.
     */
    public File renderDiagram(ContextMap map, ExecutableRule rule, File directory, Format fileFormat) throws IOException {
        String fileName = rule.getId().replace(":", "_") + "." + fileFormat.fileExtension;
        new ContextMapGenerator()
                .setBaseDir(directory)
                .generateContextMapGraphic(map, fileFormat, fileName);
        return new File(directory, fileName);
    }

}
