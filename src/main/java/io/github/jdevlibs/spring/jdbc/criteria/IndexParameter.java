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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class IndexParameter implements Parameter {
    private List<ParameterValue> params;
    private final int size;

    public IndexParameter() {
        this.size = 10;
    }

    public IndexParameter(int size) {
        super();
        this.size = size;
    }

    @Override
    public void clearParameters() {
        params = null;
    }

    @Override
    public Map<String, Object> toMapParameter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SqlParameterSource toSqlParameter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArrayParameter() {
        if (params == null || params.isEmpty()) {
            return new Object[]{};
        }

        Object[] values = new Object[params.size()];
        int inx = 0;
        for (ParameterValue obj : params) {
            values[inx++] = obj.getValue();
        }
        return values;
    }

    public void add(Object value) {
        getParams().add(new ParameterValue(value));
    }

    public void add(Object value, ParamTypes type) {
        getParams().add(new ParameterValue(value, type));
    }

    public List<ParameterValue> getParams() {
        if (params == null) {
            params = new ArrayList<>(size);
        }
        return params;
    }

    @Override
    public String toString() {
        return "IndexParameter [" + params + "]";
    }
}
