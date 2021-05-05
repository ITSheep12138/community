package com.community.bean;

/**
 * 自定义分页
 */
public class Page {
    //当前页码，默认为1
    private int current = 1;

    //每页显示条数，默认为10
    private int limit = 10;

    //数据总数
    private int rows;

    //分页的路径
    private String path;

    public int getCurrent(){
        return current;
    }

    //设置当前页，最起码为1
    public void setCurrent(int current){
        if (current >= 1){
            this.current = current;
        }
    }
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行,也就是从第几行开始查询
     * @return
     */
    public int getOffset(){
        //计算公式：current * limit - limit
        return (current - 1) * limit;
    }

    /**
     * 总页数，总行数除以每页的条数，就是总页数。
     * 这里如果除不尽应当加一，因为3/2=1,但是因该显示两页
     * @return
     */
    public int getTotal(){
        //rows / limit + 1
        return rows % limit == 0 ? rows/limit : rows/limit+1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        //获取起始页
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
