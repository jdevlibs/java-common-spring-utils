/*
 * ---------------------------------------------------------------------------
 * Copyright (c) 2023. Cana Enterprise Co., Ltd. All rights reserved
 * ---------------------------------------------------------------------------
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
