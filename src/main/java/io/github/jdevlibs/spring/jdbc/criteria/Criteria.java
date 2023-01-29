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
