/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package th.co.cana.framework.jdbc.criteria;


import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class NameParameter implements Parameter {
    private Map<String, ParameterValue> params;
    private final int size;

    public NameParameter() {
        this.size = 10;
    }

    public NameParameter(int size) {
        super();
        this.size = size;
    }

    @Override
    public Map<String, Object> toMapParameter() {
        if (params == null) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> paramNames = new LinkedHashMap<>();
        for (Map.Entry<String, ParameterValue> map : params.entrySet()) {
            paramNames.put(map.getKey(), map.getValue().getValue());
        }
        return paramNames;
    }

    @Override
    public SqlParameterSource toSqlParameter() {
        if (params == null) {
            return new EmptySqlParameterSource();
        }

        MapSqlParameterSource param = new MapSqlParameterSource();
        for (Map.Entry<String, ParameterValue> map : params.entrySet()) {
            Integer sqlType = map.getValue().getType();
            if (sqlType != null) {
                param.addValue(map.getKey(), map.getValue().getValue(), sqlType);
            } else {
                param.addValue(map.getKey(), map.getValue().getValue());
            }
        }

        return param;
    }

    @Override
    public void clearParameters() {
        params = null;
    }

    @Override
    public Object[] toArrayParameter() {
        throw new UnsupportedOperationException();
    }

    public void add(String name, Object value) {
        getParams().put(name, new ParameterValue(value));
    }

    public void add(String name, Object value, ParamTypes type) {
        getParams().put(name, new ParameterValue(value, type));
    }

    public Map<String, ParameterValue> getParams() {
        if (params == null) {
            params = new LinkedHashMap<>(size);
        }
        return params;
    }

    @Override
    public String toString() {
        return "NameParameter [" + params + "]";
    }
}
