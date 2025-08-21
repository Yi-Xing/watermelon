package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO {
    
    /**
     * 搜索关键词（用户名、邮箱、手机号模糊匹配）
     */
    private String keyword;

    /**
     * 用户状态
     */
    private Integer state;

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    @NotNull(message = "页码不能为空")
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能大于100")
    @NotNull(message = "每页大小不能为空")
    private Integer pageSize = 20;
} 