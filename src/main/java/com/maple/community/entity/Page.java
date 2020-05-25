package com.maple.community.entity;


/**
 * 封装分页相关的信息
 */
public class Page {
    /*当前页码*/
    private int current = 1;
    /*每页多少页*/
    private int limit = 10;
    /*数据总数 页数总页数*/
    private int rows;
    /*查询路径 复用分页链接*/
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset(){
        // 计算当前页的起始行
        return (current-1) * limit;
    }

    public int getTotal(){
        // 获取总的页数
        return rows % limit == 0 ? rows / limit : (rows / limit) + 1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        int from = current-2;
        return Math.max(from, 1);
    }

    /**
     * 获取结束页
     * @return
     */
    public int getTo(){
        int to = current + 2;
        return Math.min(to, getTotal());
    }

}
