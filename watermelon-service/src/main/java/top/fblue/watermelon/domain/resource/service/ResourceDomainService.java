package top.fblue.watermelon.domain.resource.service;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import java.util.List;

/**
 * 资源领域服务接口
 */
public interface ResourceDomainService {
    
    /**
     * 创建资源节点
     */
    ResourceNode createResourceNode(ResourceNode resourceNode);
    
    /**
     * 根据ID获取资源
     */
    ResourceNode getResourceById(Long id);
    
    /**
     * 根据ID查找资源节点并填充关联信息
     * 包含创建人、更新人、父节点等信息
     */
    ResourceNode findByIdWithAssociations(Long id);
    
    /**
     * 根据条件查询资源列表
     */
    List<ResourceNode> getResourceList(String name, Integer state);
    
    /**
     * 更新资源
     */
    boolean updateResource(ResourceNode resource);
    
    /**
     * 删除资源
     */
    boolean deleteResource(Long id);
    
    /**
     * 根据code获取资源ID
     */
    Long getResourceIdByCode(String code);
    
    /**
     * 导入资源
     */
    void importResource(ResourceNode resourceNode);
    
    /**
     * 检查资源ID是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查资源code是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查资源名称在同级下是否存在
     */
    boolean existsByNameAndParentId(String name, Long parentId);
} 