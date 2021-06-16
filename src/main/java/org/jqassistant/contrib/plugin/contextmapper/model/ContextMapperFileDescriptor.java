package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Descriptor representing a single .cml-file.
 *
 * @author Stephan Pirnbaum
 */
public interface ContextMapperFileDescriptor extends ContextMapperDescriptor, FileDescriptor {

    @Relation("CONTAINS")
    List<ContextMapDescriptor> getContextMaps();

}
