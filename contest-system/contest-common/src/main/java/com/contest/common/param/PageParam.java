package com.contest.common.param;

import jakarta.validation.constraints.Min;

/**
 * 通用分页请求参数
 *
 * <p>封装前端传来的分页参数（页码和每页条数），
 * 支持 Controller 层通过 @Valid 注解自动校验。
 *
 * <p>可用性说明：默认 page=1, size=10，前端不传时使用默认值。
 * 校验约束防止恶意大数值请求拖垮数据库。
 */
public class PageParam {

    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    @Min(value = 1, message = "每页条数最小为1")
    private int size = 10;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
