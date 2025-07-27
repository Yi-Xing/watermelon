package top.fblue.watermelon.application.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页响应VO
 */
@Data
public class PageVO<T> {
    
    /**
     * 数据列表
     */
    private List<T> dataList;
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int current;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 总页数
     */
    private long pages;
    
    /**
     * 是否有上一页
     */
    private boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    
    /**
     * 构造函数
     */
    public PageVO(List<T> dataList, long total, int current, int size) {
        this.dataList = dataList;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size; // 向上取整
        this.hasPrevious = current > 1;
        this.hasNext = current < pages;
    }
} 