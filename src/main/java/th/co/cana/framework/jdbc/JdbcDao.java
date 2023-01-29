/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package th.co.cana.framework.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import th.co.cana.framework.jdbc.criteria.*;
import th.co.cana.framework.spring.Transformers;
import th.co.cana.framework.utils.JdbcUtils;
import th.co.cana.framework.utils.Validators;
import th.co.cana.framework.utils.Values;

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
    protected <T> List<T> queryToList(String sql, Parameter params, Class<T> clazz) {
        logStatement(sql, params, clazz);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), Transformers.toBean(clazz));
        } else {
            return getJdbcTemplate().query(sql, Transformers.toBean(clazz), toArrays(params));
        }
    }

    /*++++++++++++++++++ SQL -> Java Bean ++++++++++++++++++ */
    protected <T> T queryToBean(String sql, Class<T> clazz) {
        return queryToBean(sql, new IndexParameter(0), clazz);
    }

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
    protected <T> T queryToObject(String sql, Class<T> type) {
        return queryToObject(sql, new IndexParameter(0), type);
    }

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
    protected <T> T query(String sql, ResultSetExtractor<T> rse) {
        return query(sql, new IndexParameter(0), rse);
    }

    protected <T> T query(String sql, Parameter params, ResultSetExtractor<T> rse) {

        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().query(sql, params.toSqlParameter(), rse);
        } else {
            return getJdbcTemplate().query(sql, rse, toArrays(params));
        }
    }

    /*++++++++++++++++++ Paging ++++++++++++++++++ */
    protected <T> Paging<T> queryWithPaging(String sql, Criteria criteria, Class<T> clazz) {
        return queryWithPaging(sql, new IndexParameter(), criteria, clazz);
    }

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

    protected <T> List<T> queryToPaging(String sql, Criteria criteria, Class<T> clazz) {
        return queryToPaging(sql, new IndexParameter(), criteria, clazz);
    }

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

    protected Long countForPaging(String sql, Parameter params) {
        String countSql = "SELECT COUNT(*) AS TOTAL FROM (" + sql + ") TB";
        Number value = queryToNumber(countSql, params);
        if (Validators.isNull(value)) {
            return 0L;
        }

        return value.longValue();
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

    private void setPagingOption(StringBuilder sql, Parameter params, Criteria paging) {
        if (Validators.isNull(paging) || paging.isNullPaging()) {
            return;
        }
        this.setMSSqlPaging(sql, params, paging);
    }

    private void setMSSqlPaging(StringBuilder sql, Parameter params, Criteria paging) {
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

    /*++++++++++++++++++ SQL -> Number ++++++++++++++++++ */
    public Number queryToNumber(String sql) {
        return queryToObject(sql, new IndexParameter(0), Number.class);
    }

    public Number queryToNumber(String sql, Parameter params) {
        return queryToObject(sql, params, Number.class);
    }

    protected void closeResource(Connection conn, Statement stmt, ResultSet rs) {
        closeResource(rs);
        closeResource(stmt);
        closeResource(conn);
    }

    protected void closeResource(Statement stmt, ResultSet rs) {
        closeResource(rs);
        closeResource(stmt);
    }

    protected void closeResource(AutoCloseable obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException ex) {
            logger.error("getConnection", ex);
        }

        return null;
    }

    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    /*++++++++++++++++++ Execute update/insert/delete ++++++++++++++++++ */
    public int execute(String sql) {
        return execute(sql, new IndexParameter(0));
    }

    public int execute(String sql, Parameter params) {
        logStatement(sql, params);

        if (params instanceof NameParameter) {
            return getNamedParameterJdbcTemplate().update(sql, params.toSqlParameter());
        } else {
            return getJdbcTemplate().update(sql, toArrays(params));
        }
    }

    public int execute(String sql, Object ... params) {
        if (Validators.isEmpty(params)) {
            return getJdbcTemplate().update(sql);
        } else {
            return getJdbcTemplate().update(sql, params);
        }
    }

    public int deleteAll(final String table) {
        if (Validators.isNull(table)) {
            return 0;
        }

        String sql = "DELETE FROM " + table;
        return execute(sql);
    }

    public String sqlFullLike(String value) {
        return JdbcUtils.sqlFullLike(value);
    }

    public String sqlStartLike(String value) {
        return JdbcUtils.sqlStartLike(value);
    }

    public String sqlEndLike(String value) {
        return JdbcUtils.sqlEndLike(value);
    }

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

    public String createWhereIn(String ... items) {
        if (Validators.isEmpty(items)) {
            return Values.EMPTY;
        }

        StringBuilder sql = new StringBuilder(256);
        boolean first = true;
        for (String obj : items) {
            if (Validators.isNull(obj)) {
                continue;
            }

            if (first) {
                sql.append("'");
                sql.append(obj);
                sql.append("'");
                first = false;
            } else {
                sql.append(",");
                sql.append("'");
                sql.append(obj);
                sql.append("'");
            }
        }

        return sql.toString();
    }

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

    public String createWhereIn(Collection<?> items, NameParameter params) {
        return createWhereIn(items, params, "IN");
    }

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

    public void setDateParameter(NameParameter params, String name, Date value) {
        params.add(name, value, ParamTypes.DATE);
    }

    private Object[] toArrays(Parameter params) {
        if (Validators.isNull(params)) {
            return new Object[] {};
        }
        return params.toArrayParameter();
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
