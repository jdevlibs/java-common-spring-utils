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
package io.github.jdevlibs.spring;

import io.github.jdevlibs.utils.ClassUtils;
import io.github.jdevlibs.utils.Convertors;
import io.github.jdevlibs.utils.JdbcUtils;
import io.github.jdevlibs.utils.bean.NestedSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class NestedBeanMapper<T> implements RowMapper<T> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> clazz;
    private Map<String, NestedSetter> setters;
    private List<JdbcUtils.ColumnInfo> columns;

    public NestedBeanMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Assert.state(this.clazz != null, "Mapped class was not specified");
        this.createCachedSetters(rs);

        T result = BeanUtils.instantiateClass(clazz);
        for (JdbcUtils.ColumnInfo col : columns) {
            NestedSetter setter = setters.get(col.getColumn());
            if (setter == null || setter.getPropertyType() == null) {
                continue;
            }
            Object value = JdbcUtils.getResultSetValue(rs, col.getColumn(), col.getType());
            if (value == null) {
                continue;
            }
            
            Class<?> propertyType = setter.getPropertyType();
            Class<?> resultType = value.getClass();
            if (logger.isDebugEnabled() && rowNumber == 0) {
                logger.debug("Mapping column '{}' to property '{}' of type {} from type {}", col.getColumn()
                        , setter.getName(), propertyType, resultType);
            }

            if (propertyType.equals(resultType)) {
                setter.setValue(result, value);
            } else {
                if (ClassUtils.isString(propertyType)) {
                    setter.setValue(result, value.toString());
                } else {
                    value = Convertors.convertWithType(propertyType, value);
                    setter.setValue(result, value);
                }
            }
        }

        return result;
    }

    private void createCachedSetters(ResultSet rs) throws SQLException {
        if (columns == null || setters == null) {
            ResultSetMetaData metaData = rs.getMetaData();
            columns = JdbcUtils.getColumInfo(metaData);
            if (setters == null) {
                setters = createSetters(clazz);
            }
        }
    }

    private Map<String, NestedSetter> createSetters(Class<?> resultClass) {
        Map<String, NestedSetter> result = new HashMap<>(columns.size());
        for (JdbcUtils.ColumnInfo col : columns) {
            NestedSetter setter = NestedSetter.create(resultClass, col.getColumn());
            if (setter != null) {
                result.put(col.getColumn(), setter);
            }
        }

        return result;
    }
}
