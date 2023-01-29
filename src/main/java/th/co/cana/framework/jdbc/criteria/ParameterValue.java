/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package th.co.cana.framework.jdbc.criteria;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class ParameterValue implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Object value;
    private final ParamTypes type;

    public ParameterValue(Object value) {
        super();
        this.value = value;
        this.type = null;
    }

    public ParameterValue(Object value, ParamTypes type) {
        super();
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Integer getType() {
        if (type == null) {
            return null;
        }
        return type.getValue();
    }

    public Collection<?> getCollection() {
        if (isCollection()) {
            return (Collection<?>) value;
        }
        return Collections.emptyList();
    }

    public Object[] getArray() {
        if (isCollection()) {
            return (Object[]) value;
        }
        return new Object[0];
    }

    public boolean isCollection() {
        return (value != null && value instanceof Collection<?>);
    }

    public boolean isArray() {
        return (value != null && value.getClass().isArray());
    }

    public boolean isTypeNotNull() {
        return (type != null);
    }

    public boolean isNull() {
        return (value == null);
    }

    @Override
    public String toString() {
        return "[value=" + value + ", type=" + type + "]";
    }
}
