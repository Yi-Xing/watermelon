package top.fblue.watermelon.domain.resource.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 资源节点领域实体
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResourceNode {
    
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源类型：1 页面，2 按钮，3 接口，4 目录
     */
    private Integer type;
    
    /**
     * 资源code
     */
    private String code;
    
    /**
     * 状态：1 启用 2 禁用
     */
    private Integer state;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建人
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    private Long updatedBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ResourceNode that = (ResourceNode) o;
        
        return Objects.equals(name, that.name) &&
               Objects.equals(type, that.type) &&
               Objects.equals(code, that.code) &&
               Objects.equals(state, that.state) &&
               Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, code, state, remark);
    }
} 