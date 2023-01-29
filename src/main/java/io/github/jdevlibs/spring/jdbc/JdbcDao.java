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
package io.github.jdevlibs.spring.jdbc;

import io.github.jdevlibs.spring.Transformers;
import io.github.jdevlibs.spring.jdbc.criteria.*;
import io.github.jdevlibs.utils.JdbcUtils;
import io.github.jdevlibs.utils.Validators;
import io.github.jdevlibs.utils.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
public abstract class JdbcDao implements InitializingBean {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /* ++++++++++++++++++++++++++ Initial and Validate +++++++++++++++++++++++ */
    @Override
    public final void afterPropertiesSet() throws IllegalArgumentException {
        validateJdbcTemplate();
    }

    protected abstract void autowiredJdbcTemplate(JdbcTemplate jdbcTemplate);

    protected abstract void setPagingOption(StringBuilder sql, Parameter params, Criteria paging);

    public final void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        if (jdbcTemplate != null) {
            jdbcTemplate.setResultsMapCaseInsensitive(true);
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        }
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    private void validateJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            throw new IllegalArgumentException("JdbcTemplate is required");
        }
    }

    /*++++++++++++++++++ SQL -> List Java Bean ++++++++++++++++++ */

    /**
     * Query and auto-convert to the collection of the target class.
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param clazz The result target class
     * @return Collection of result target class
     * @param <T> Generic result class
     */
    protected <T> List<T> queryToList(String sql, Parameter params, Class<T> clazz) {
        logStatement(sql, params, clazz);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), Transformers.toBean(clazz));
        } else {
            return getJdbcTemplate().query(sql, Transformers.toBean(clazz), toArrays(params));
        }
    }

    /*++++++++++++++++++ SQL -> Java Bean ++++++++++++++++++ */

    /**
     * Query and auto-convert to target class (Bean model)
     * @param sql The sql statement
     * @param clazz The result target class
     * @return result target class
     * @param <T> Generic result class
     */
    protected <T> T queryToBean(String sql, Class<T> clazz) {
        return queryToBean(sql, new IndexParameter(0), clazz);
    }

    /**
     * Query and auto-convert to target class (Bean model)
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param clazz The result target class
     * @return result target class
     * @param <T> Generic result class
     */
    protected <T> T queryToBean(String sql, Parameter params, Class<T> clazz) {
        try {
            logStatement(sql, params, clazz);

            if (params instanceof NameParameter) {
                return getNamedParameterJdbcTemplate().queryForObject(sql, params.toSqlParameter(),
                        Transformers.toBean(clazz));
            } else {
                return getJdbcTemplate().queryForObject(sql, Transformers.toBean(clazz), toArrays(params));
            }
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /*++++++++++++++++++ SQL -> Object ++++++++++++++++++ */

    /**
     * Query and auto-convert to object (Int, Number, String, etc.)
     * @param sql The sql statement
     * @param type The result class type
     * @return result target class
     * @param <T> Generic result class
     */
    protected <T> T queryToObject(String sql, Class<T> type) {
        return queryToObject(sql, new IndexParameter(0), type);
    }

    /**
     * Query and auto-convert to object (Int, Number, String, etc.)
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param type The result class type
     * @return result target class
     * @param <T> Generic result class
     */
    protected <T> T queryToObject(String sql, Parameter params, Class<T> type) {
        try {
            logStatement(sql, params, type);

            if (params instanceof NameParameter) {
                return getNamedParameterJdbcTemplate().queryForObject(sql, params.toSqlParameter(), type);
            } else {
                return getJdbcTemplate().queryForObject(sql, type, toArrays(params));
            }
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /*++++++++++++++++++ SQL -> Custom Extractor ++++++++++++++++++ */
    /**
     * Query and auto-convert to ResultSetExtractor operation
     * @param sql The sql statement
     * @param rse The ResultSetExtractor operation
     * @return result target class by ResultSetExtractor
     * @param <T> Generic result class
     */
    protected <T> T query(String sql, ResultSetExtractor<T> rse) {
        return query(sql, new IndexParameter(0), rse);
    }

    /**
     * Query and auto-convert to ResultSetExtractor operation
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param rse The ResultSetExtractor operation
     * @return result target class by ResultSetExtractor
     * @param <T> Generic result class
     */
    protected <T> T query(String sql, Parameter params, ResultSetExtractor<T> rse) {

        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), rse);
        } else {
            return getJdbcTemplate().query(sql, rse, toArrays(params));
        }
    }

    /*++++++++++++++++++ Paging ++++++++++++++++++ */
    /**
     * Query and auto-convert to Paging result
     * @param sql The sql statement
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging data
     * @param <T> Generic result class
     */
    protected <T> Paging<T> queryWithPaging(String sql, Criteria criteria, Class<T> clazz) {
        return queryWithPaging(sql, new IndexParameter(), criteria, clazz);
    }

    /**
     * Query and auto-convert to Paging result
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging data
     * @param <T> Generic result class
     */
    protected <T> Paging<T> queryWithPaging(String sql, Parameter params,
                                            Criteria criteria, Class<T> clazz) {

        Paging<T> paging = new Paging<>();
        Long count = countForPaging(sql, params);
        paging.setTotalRow(count.intValue());

        List<T> items = queryToPaging(sql, params, criteria, clazz);
        paging.setItems(items);
        if (Validators.isNotNull(criteria.getPage())) {
            paging.setPageNo(criteria.getPage());
        }
        if (Validators.isNotNull(criteria.getSize())) {
            paging.setPageSize(criteria.getSize());
        }
        paging.caculateTotalPage();

        return paging;
    }

    /**
     * Query and auto-convert to paging collection result
     * @param sql The sql statement
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging collection data
     * @param <T> Generic result class
     */
    protected <T> List<T> queryToPaging(String sql, Criteria criteria, Class<T> clazz) {
        return queryToPaging(sql, new IndexParameter(), criteria, clazz);
    }

    /**
     * Query and auto-convert to paging collection result
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param criteria Sql criteria
     * @param clazz The result class type
     * @return result paging collection data
     * @param <T> Generic result class
     */
    protected <T> List<T> queryToPaging(String sql, Parameter params,
                                        Criteria criteria, Class<T> clazz) {

        StringBuilder pageSql = new StringBuilder();
        pageSql.append("SELECT * FROM (").append(sql);
        pageSql.append(" ) TB");
        if (criteria.isEmptySort()) {
            if (Validators.isEmpty(criteria.getOrderByColumn())) {
                pageSql.append(" ORDER BY 1");
            } else {
                pageSql.append(" ORDER BY ").append(criteria.getOrderByColumn());
            }
        } else {
            this.setOrderByOption(pageSql, criteria);
        }
        setPagingOption(pageSql, params, criteria);

        return queryToList(pageSql.toString(), params, clazz);
    }

    /**
     * Query count paging collection result
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @return result total record data
     */
    protected Long countForPaging(String sql, Parameter params) {
        String countSql = "SELECT COUNT(*) AS TOTAL FROM (" + sql + ") TB";
        Number value = queryToNumber(countSql, params);
        if (Validators.isNull(value)) {
            return 0L;
        }

        return value.longValue();
    }

    /**
     * Setting for MS SQL Server paging style
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param paging Sql paging criteria
     */
    public void setMSSqlPaging(StringBuilder sql, Parameter params, Criteria paging) {
        if (params instanceof NameParameter) {
            NameParameter name = (NameParameter) params;
            sql.append(" OFFSET :P_ROW_START ROWS FETCH NEXT :P_ROW_TOTAL ROWS ONLY");
            name.add("P_ROW_START", paging.getMsSqlOffset());
            name.add("P_ROW_TOTAL", paging.getSize());
        } else {
            IndexParameter inx = (IndexParameter) params;
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            inx.add(paging.getMsSqlOffset());
            inx.add(paging.getSize());
        }
    }

    /**
     * Setting for Oracle Server paging style
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @param paging Sql paging criteria
     */
    public void setOraclePaging(StringBuilder sql, Parameter params, Criteria paging) {
        sql.append("SELECT T.* FROM (");
        sql.append("SELECT ROWNUM AS ROW_NUM, T.* FROM (");
        sql.append(sql);
        sql.append(") T");
        sql.append(") T");
        if (params instanceof NameParameter) {
            NameParameter name = (NameParameter) params;
            sql.append(" WHERE T.ROW_NUM <= :P_ROW_NUM");
            name.add("P_ROW_NUM", paging.getOracleRowEnd());
        } else {
            IndexParameter inx = (IndexParameter) params;
            sql.append(" WHERE T.ROW_NUM <= ?");
            inx.add(paging.getOracleRowEnd());
        }
    }

    private void setOrderByOption(StringBuilder sql, Criteria paging) {
        if (paging.isEmptySort()) {
            return;
        }

        boolean first = true;
        for (Map.Entry<String, String> map : paging.getSorts().entrySet()) {
            if (first) {
                sql.append(" ORDER BY ").append(map.getKey())
                        .append(" ").append(map.getValue());
                first = false;
            } else {
                sql.append(", ").append(map.getKey())
                        .append(" ").append(map.getValue());
            }
        }
    }

    /*++++++++++++++++++ SQL -> Number ++++++++++++++++++ */
    /**
     * Query and auto-convert to Number
     * @param sql The sql statement
     * @return result number class
     */
    public Number queryToNumber(String sql) {
        return queryToObject(sql, new IndexParameter(0), Number.class);
    }

    /**
     * Query and auto-convert to Number
     * @param sql The sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @return result number class
     */
    public Number queryToNumber(String sql, Parameter params) {
        return queryToObject(sql, params, Number.class);
    }

    /**
     * Close resource
     * @param conn The database connection
     * @param stmt java.sql.Statement
     * @param rs java.sql.ResultSet
     */
    protected void closeResource(Connection conn, Statement stmt, ResultSet rs) {
        closeResource(rs);
        closeResource(stmt);
        closeResource(conn);
    }

    /**
     * Close resource
     * @param stmt java.sql.Statement
     * @param rs java.sql.ResultSet
     */
    protected void closeResource(Statement stmt, ResultSet rs) {
        closeResource(rs);
        closeResource(stmt);
    }

    /**
     * Close auto closeable resource
     * @param obj The Closeable resource
     */
    protected void closeResource(AutoCloseable obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Get active database connection
     * @return The database connection (null when cannot get)
     */
    public Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException ex) {
            logger.error("getConnection", ex);
        }

        return null;
    }

    /**
     * Get active DataSource
     * @return The active DataSource (null when cannot get)
     */
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    /*++++++++++++++++++ Execute update/insert/delete ++++++++++++++++++ */

    /**
     * Execute DML sql statement (insert, update delete)
     * @param sql The DML sql statement
     * @return Total row of execute.
     */
    public int execute(String sql) {
        return execute(sql, new IndexParameter(0));
    }

    /**
     * Execute DML sql statement (insert, update delete)
     * @param sql The DML sql statement
     * @param params The sql statement parameter
     * @see IndexParameter
     * @see NameParameter
     * @return Total row of execute.
     */
    public int execute(String sql, Parameter params) {
        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().update(sql, params.toSqlParameter());
        } else {
            return getJdbcTemplate().update(sql, toArrays(params));
        }
    }

    /**
     * Execute DML sql statement (insert, update delete)
     * @param sql The DML sql statement
     * @param params SQL Statement parameter
     * @return Total row of execute.
     */
    public int execute(String sql, Object ... params) {
        if (Validators.isEmpty(params)) {
            return getJdbcTemplate().update(sql);
        } else {
            return getJdbcTemplate().update(sql, params);
        }
    }

    /**
     * Concat SQL like contain value (computer to '%computer%')
     * @param value The where value
     * @return Value after concat
     */
    public String sqlLikeContain(String value) {
        return JdbcUtils.sqlFullLike(value);
    }

    /**
     * Concat SQL like contain value (computer to '%computer')
     * @param value The where value
     * @return Value after concat
     */
    public String sqlLikeStart(String value) {
        return JdbcUtils.sqlStartLike(value);
    }

    /**
     * Concat SQL like contain value (computer to 'computer%')
     * @param value The where value
     * @return Value after concat
     */
    public String sqlLikeEnd(String value) {
        return JdbcUtils.sqlEndLike(value);
    }

    /**
     * Create dynamic sql WHERE IN statement
     * @param items The items of value
     * @return Result sql WHERE IN
     */
    public String createNumberWhereIn(Number ... items) {
        if (Validators.isEmpty(items)) {
            return Values.EMPTY;
        }

        StringBuilder sql = new StringBuilder(256);
        boolean first = true;
        for (Number obj : items) {
            if (Validators.isNull(obj)) {
                continue;
            }

            if (first) {
                sql.append(obj);
                first = false;
            } else {
                sql.append(",");
                sql.append(obj);

            }
        }

        return sql.toString();
    }

    /**
     * Create dynamic sql WHERE IN statement
     * @param items The items of value
     * @param params The sql statement parameter
     * @see IndexParameter
     * @return Result sql WHERE IN
     */
    public String createWhereIn(Collection<?> items, IndexParameter params) {
        if (Validators.isEmpty(items)) {
            return Values.EMPTY;
        }

        StringBuilder sql = new StringBuilder(256);
        boolean first = true;
        for (Object obj : items) {
            if (first) {
                sql.append("?");
                first = false;
            } else {
                sql.append(", ?");
            }
            params.add(obj);
        }

        return sql.toString();
    }

    /**
     * Create dynamic sql WHERE IN statement
     * @param items The items of value
     * @param params The sql statement parameter
     * @see NameParameter
     * @return Result sql WHERE IN
     */
    public String createWhereIn(Collection<?> items, NameParameter params) {
        return createWhereIn(items, params, "IN");
    }

    /**
     * Create dynamic sql WHERE IN statement
     * @param items The items of value
     * @param params The sql statement parameter
     * @see NameParameter
     * @param prefix Name parameter prefix
     * @return Result sql WHERE IN
     */
    public String createWhereIn(Collection<?> items, NameParameter params, String prefix) {
        if (Validators.isEmpty(items)) {
            return Values.EMPTY;
        }
        prefix = (Validators.isEmpty(prefix) ? "X" : prefix);

        StringBuilder sql = new StringBuilder(256);
        int inx = 1;
        for (Object obj : items) {
            String paramName = "P_" + prefix + "_PARAM_" + inx;
            if (inx == 1) {
                sql.append(":").append(paramName);
            } else {
                sql.append(", ").append(":").append(paramName);
            }
            params.add(paramName, obj);
            inx++;
        }

        return sql.toString();
    }

    /**
     * Create dynamic sql WHERE IN statement
     * @param items The items of value
     * @param params The sql statement parameter
     * @see NameParameter
     * @return Result sql WHERE IN
     */
    public String createWhereIn(Object[] items,NameParameter params) {
        if (Validators.isEmpty(items)) {
            return Values.EMPTY;
        }

        StringBuilder sql = new StringBuilder(256);
        int inx = 1;
        for (Object obj : items) {
            String paramName = "P_IN_PARAM_" + inx;
            if (inx == 1) {
                sql.append(":").append(paramName);
            } else {
                sql.append(", ");
                sql.append(":").append(paramName);
            }
            params.add(paramName, obj);
            inx++;
        }

        return sql.toString();
    }

    private void setDateParameter(NameParameter params, String name, Date value) {
        params.add(name, value, ParamTypes.DATE);
    }

    private Object[] toArrays(Parameter params) {
        if (Validators.isNull(params)) {
            return new Object[] {};
        }
        return params.toArrayParameter();
    }

    public boolean isOracle() {
        try (Connection conn = getConnection()) {
            return JdbcUtils.isOracle(conn);
        } catch (SQLException ex) {
            logger.error("isOracle : {}", ex.getMessage());
        }
        return false;
    }

    public boolean isMySql() {
        try (Connection conn = getConnection()) {
            return JdbcUtils.isMySql(conn);
        } catch (SQLException ex) {
            logger.error("isMySql : {}", ex.getMessage());
        }

        return false;
    }

    public boolean isMSSql() {
        try (Connection conn = getConnection()) {
            return JdbcUtils.isMsSql(conn);
        } catch (SQLException ex) {
            logger.error("isMySql : {}", ex.getMessage());
        }
        return false;
    }

    private void logStatement(String sql, Parameter params) {
        logStatement(sql, params, null);
    }

    private void logStatement(String sql, Parameter params, Class<?> clazz) {
        logger.info("SQL Statement : {}", sql);
        if (params != null) {
            if (params instanceof NameParameter) {
                logger.info("Parameter : {}", params.toMapParameter());
            } else {
                logger.info("Parameter : {}", params.toArrayParameter());
            }
        }

        if (clazz != null) {
            logger.info("Result Target class : {}", clazz.getName());
        }
    }
}
