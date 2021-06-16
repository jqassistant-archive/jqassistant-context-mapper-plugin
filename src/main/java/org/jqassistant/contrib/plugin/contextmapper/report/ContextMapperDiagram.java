package org.jqassistant.contrib.plugin.contextmapper.report;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextBaseDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyDescriptor;

import java.util.List;

/**
 * Model class representing the content of a context map.
 *
 * @author Stephan Pirnbaum
 */
@Getter
@Builder
public class ContextMapperDiagram {

    @Singular
    private List<BoundedContextBaseDescriptor> boundedContexts;

    @Singular
    private List<BoundedContextDependencyDescriptor> relationships;

}
