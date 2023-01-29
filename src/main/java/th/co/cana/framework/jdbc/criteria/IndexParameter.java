/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package th.co.cana.framework.jdbc.criteria;

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
