package com.contest.common.dto;

import java.util.List;

/**
 * 通用分页响应结果
 *
 * 封装后端分页查询结果，包含总记录数、当前页码、每页大小和当前页数据。
 * 部分接口已切换为此类返回，逐步替代 MyBatis-Plus 的 IPage。
 */
public class PageResult<T> {

    private long total;
    private int page;
    private int size;
    private List<T> data;

    public PageResult() {}

    public PageResult(long total, int page, int size, List<T> data) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.data = data;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
}
