/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import th.co.cana.framework.utils.ClassUtils;
import th.co.cana.framework.utils.Convertors;
import th.co.cana.framework.utils.DateFormats;
import th.co.cana.framework.utils.JdbcUtils;
import th.co.cana.framework.utils.bean.NestedSetter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                    value = convertByType(propertyType, value);
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

    private static Object convertByType(Class<?> clazzType, Object value) {
        if (clazzType.equals(BigDecimal.class)) {
            return Convertors.toBigDecimal(value);
        } else if (clazzType.equals(BigInteger.class)) {
            return Convertors.toBigInteger(value);
        } else if (clazzType.equals(Long.class)) {
            return Convertors.toLong(value);
        } else if (clazzType.equals(Integer.class)) {
            return Convertors.toInteger(value);
        } else if (clazzType.equals(Double.class)) {
            return Convertors.toDouble(value);
        } else if (clazzType.equals(Float.class)) {
            return Convertors.toFloat(value);
        } else if (clazzType.equals(Short.class)) {
            return Convertors.toShort(value);
        } else {
            return convertWithOther(clazzType, value);
        }
    }

    private static Object convertWithOther(Class<?> clazzType, Object value) {
        if (clazzType.equals(LocalDateTime.class)) {
            return DateFormats.localDateTime(value);
        } else if (clazzType.equals(LocalDate.class)) {
            return DateFormats.localDate(value);
        } else if (clazzType.equals(LocalTime.class)) {
            return DateFormats.time(value);
        } else {
            if (value instanceof Clob) {
                return JdbcUtils.readClob((Clob) value);
            } else if (value instanceof Blob) {
                return JdbcUtils.toByte((Blob) value);
            }

            return value;
        }
    }
}
