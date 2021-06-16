package org.jqassistant.contrib.plugin.contextmapper.report;

import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependencyDescriptor;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDependsOn;
import org.jqassistant.contrib.plugin.contextmapper.model.BoundedContextDescriptor;

import java.util.List;

@Getter
@Builder
public class ContextMapperDiagram {

    @Singular
    private List<BoundedContextDescriptor> boundedContexts;

    @Singular
    private List<BoundedContextDependencyDescriptor> relationships;

}
