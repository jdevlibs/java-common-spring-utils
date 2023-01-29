/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package io.github.jdevlibs.spring.jdbc.criteria;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
public interface Parameter {

    void clearParameters();

    Map<String, Object> toMapParameter();

    SqlParameterSource toSqlParameter();

    Object[] toArrayParameter();
}