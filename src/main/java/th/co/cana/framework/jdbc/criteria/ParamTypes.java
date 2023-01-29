/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package th.co.cana.framework.jdbc.criteria;

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