/*  ---------------------------------------------------------------------------
 *  * Copyright 2020-2021 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring.jdbc.criteria;

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
