package top.fblue.watermelon.application.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 资源关联视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRelationVO {
    
    /**
     * 关联ID
     */
    private Long id;
    
    /**
     * 父级资源ID
     */
    private Long parentId;
    
    /**
     * 父级资源名称
     */
    private String parentName;
    
    /**
     * 父级资源Code
     */
    private String parentCode;
    
    /**
     * 子级资源ID
     */
    private Long childId;
    
    /**
     * 子级资源名称
     */
    private String childName;
    
    /**
     * 子级资源Code
     */
    private String childCode;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
}
