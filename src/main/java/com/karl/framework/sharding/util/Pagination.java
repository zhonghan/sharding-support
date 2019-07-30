package com.karl.framework.sharding.util;

/**
 * @author karl.zhong
 */
public class Pagination {
    private int totalSize;
    private int totalPages;
    private int pageSize;
    private int currentPageNo;
    private Object data;

    public Pagination(int totalSize, int pageSize, int currentPageNo, Object data) {
        this.totalSize = totalSize;
        this.totalPages = totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1;
        this.pageSize = pageSize;
        this.currentPageNo = currentPageNo;
        this.data = data;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public Pagination setTotalSize(int totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Pagination setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Pagination setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getCurrentPageNo() {
        return currentPageNo;
    }

    public Pagination setCurrentPageNo(int currentPageNo) {
        this.currentPageNo = currentPageNo;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Pagination setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "totalSize=" + totalSize +
                ", totalPages=" + totalPages +
                ", pageSize=" + pageSize +
                ", currentPageNo=" + currentPageNo +
                ", data=" + data +
                '}';
    }
}
