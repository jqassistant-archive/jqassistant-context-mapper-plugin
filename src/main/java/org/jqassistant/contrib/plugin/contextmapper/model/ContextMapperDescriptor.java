package org.jqassistant.contrib.plugin.contextmapper.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Base descriptor for all context mapper related types.
 *
 * @author Stephan Pirnbaum
 */
@Label("ContextMapper")
public interface ContextMapperDescriptor extends Descriptor {

}
