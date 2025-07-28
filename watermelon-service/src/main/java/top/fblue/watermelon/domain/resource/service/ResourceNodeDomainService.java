package top.fblue.watermelon.domain.resource.service;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;

/**
 * 资源节点领域服务接口
 */
public interface ResourceNodeDomainService {
    
    /**
     * 创建资源节点
     */
    ResourceNode createResourceNode(ResourceNode resourceNode);
    
    /**
     * 根据ID查找资源节点
     */
    ResourceNode findById(Long id);
    
    /**
     * 根据ID查找资源节点并填充关联信息
     * 包含创建人、更新人、父节点等信息
     */
    ResourceNode findByIdWithAssociations(Long id);
}