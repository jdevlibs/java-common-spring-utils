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
import java.util.ArrayList;
import java.util.List;

/**
 * @author supot.jdev
 * @version 1.0
 */
public class Paging<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer totalRow;
    private int totalPage;
    private int pageNo;
    private int pageSize;
    private List<T> items;

    public Integer getTotalRow() {
        return totalRow;
    }

    public void setTotalRow(Integer totalRow) {
        this.totalRow = totalRow;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getItemSize() {
        return (items != null ? items.size() : 0);
    }

    public void caculateTotalPage() {
        if (totalRow == null || totalRow == 0 || pageSize == 0) {
            return;
        }

        totalPage = totalRow / pageSize;
        if (totalRow % pageSize != 0) {
            totalPage += 1;
        }
    }

    public void addItem(T item) {
        if (item == null) {
            return;
        }
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }
}
