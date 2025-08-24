package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 资源树形结构视图对象
 */
@Data
@Builder
public class ResourceNodeTreeVO {

    /**
     * 资源ID
     */
    private Long id;

    /**
     * 资源树code，每个节点的唯一值
     * 前端需要该值展示选中
     */
    private String treeCode;

    /**
     * 资源关联ID
     */
    private Long resourceRelationId;

    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源类型：1 页面，2 按钮，3 接口，4 目录
     */
    private Integer type;
    
    /**
     * 资源类型描述
     */
    private String typeDesc;
    
    /**
     * 资源code
     */
    private String code;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 状态：1 启用 2 禁用
     */
    private Integer state;
    
    /**
     * 状态描述
     */
    private String stateDesc;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private String createdTime;
    
    /**
     * 更新时间
     */
    private String updatedTime;
    
    /**
     * 子节点列表
     */
    private List<ResourceNodeTreeVO> children;
} 