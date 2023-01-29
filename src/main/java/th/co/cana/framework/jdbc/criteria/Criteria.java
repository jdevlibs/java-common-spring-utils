/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
 */
package th.co.cana.framework.jdbc.criteria;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class Criteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer page;
    private Integer size;
    private String orderByColumn;
    private Map<String, String> sorts;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getOrderByColumn() {
        return orderByColumn;
    }

    public void setOrderByColumn(String orderByColumn) {
        this.orderByColumn = orderByColumn;
    }

    public int getMySqlOffset() {
        return getRowStart();
    }

    public int getMsSqlOffset() {
        return getMySqlOffset();
    }

    public Integer getOracleRowStart() {
        return getRowStart() + 1;
    }

    public Integer getOracleRowEnd() {
        return getRowStart() + size;
    }

    public boolean isNullPaging() {
        return (size == null) || (page == null);
    }

    public boolean isNotNullPaging() {
        return !isNullPaging();
    }

    public void addSorts(String key, String value) {
        if (sorts == null) {
            sorts = new LinkedHashMap<>();
        }
        sorts.put(key, value);
    }

    private int getRowStart() {
        if (page <= 0) {
            page = 1;
        }
        return (page - 1) * size;
    }

    public boolean isCountQuery() {
        return page == null || page <= 1;
    }

    public Map<String, String> getSorts() {
        return sorts;
    }

    public void setSorts(Map<String, String> sorts) {
        this.sorts = sorts;
    }

    public boolean isEmptySort() {
        return (sorts == null || sorts.isEmpty());
    }

    public void setPagingAndSorting(Criteria criteria) {
        if (criteria != null) {
            this.sorts = criteria.getSorts();
            this.size = criteria.getSize();
            this.page = criteria.getPage();
        }
    }
}
