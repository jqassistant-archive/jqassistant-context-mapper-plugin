package org.jqassistant.contrib.plugin.contextmapper.model;

import lombok.Getter;

/**
 * DDD dependency type between {@link BoundedContextDescriptor}s.
 *
 * @author Stephan Pirnbaum
 */
@Getter
public enum BoundedContextDependencyType {
    CUSTOMER_SUPPLIER("C/S"),
    UPSTREAM_DOWNSTREAM("U/D"),
    SHARED_KERNEL("SK"),
    PARTNERSHIP("P");

    private final String type;

    BoundedContextDependencyType(String type) {
        this.type = type;
    }

    public static BoundedContextDependencyType getByType(String type) {
        if (type == null || type.isEmpty()) {
            return CUSTOMER_SUPPLIER;
        } else {
            switch (type) {
                case "C/S": return CUSTOMER_SUPPLIER;
                case "U/D": return UPSTREAM_DOWNSTREAM;
                case "SK": return SHARED_KERNEL;
                case "P": return PARTNERSHIP;
                default: return null;
            }
        }
    }
}
