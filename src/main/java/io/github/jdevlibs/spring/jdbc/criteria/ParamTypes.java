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

import java.sql.Types;

/**
 * @author supot.jdev
 * @version 1.0
 */
public enum ParamTypes {
    VARCHAR(Types.VARCHAR),
    CHAR(Types.CHAR),
    NUMERIC(Types.NUMERIC),
    DECIMAL(Types.DECIMAL),
    INTEGER(Types.INTEGER),
    BIGINT(Types.BIGINT),
    FLOAT(Types.FLOAT),
    BOOLEAN(Types.BOOLEAN),
    DATE(Types.DATE),
    TIMESTAMP(Types.TIMESTAMP),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    NULL(Types.NULL),
    OTHER(Types.OTHER),
    REF_CURSOR(Types.REF_CURSOR),
    CURSOR(-10);

    private final int value;

    ParamTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ParamTypes getSqlTypes(int value) {
        for (ParamTypes type : ParamTypes.values()) {
            if (value == type.getValue()) {
                return type;
            }
        }

        return ParamTypes.OTHER;
    }
}