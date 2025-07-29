package top.fblue.watermelon.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/**
 * 更新角色资源DTO
 */
@Data
public class UpdateRoleResourceDTO {
    
    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long id;
    
    /**
     * 资源ID列表
     */
    private List<Long> resourceIds;
} 