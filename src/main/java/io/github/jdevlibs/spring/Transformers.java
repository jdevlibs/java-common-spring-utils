/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class Transformers {
    private Transformers() {
    }

    /**
     * Transformer query result to POJO (Java Bean)
     * @param clazz The target class for transformer
     * @return The result of class after mapping
     */
    public static <T> RowMapper<T> toBean(Class<T> clazz) {
        return new NestedBeanMapper<>(clazz);
    }
}
