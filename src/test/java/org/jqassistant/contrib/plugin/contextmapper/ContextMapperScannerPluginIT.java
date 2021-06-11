package org.jqassistant.contrib.plugin.contextmapper;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import org.jqassistant.contrib.plugin.contextmapper.model.ContextMapperDescriptor;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


public class ContextMapperScannerPluginIT extends AbstractPluginIT {

    //@TestStore(type = TestStore.Type.REMOTE)
    @Test
    public void scan() {
        store.beginTransaction();
        File testFile = new File(getClassesDirectory(ContextMapperScannerPluginIT.class), "Insurance-Example-Stage-1.cml");

        Descriptor descriptor = getScanner().scan(testFile, "Insurance-Example-Stage-1.cml", DefaultScope.NONE);
        assertThat(descriptor).isInstanceOf(ContextMapperDescriptor.class);

        store.commitTransaction();
    }
}
