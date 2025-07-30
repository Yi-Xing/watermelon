package top.fblue.watermelon.domain.resource.service;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import java.util.List;
import java.util.Map;

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
     * 根据条件查询资源列表
     */
    List<ResourceNode> getResourceList(String name, String code, Integer state);
    
    /**
     * 根据ID列表查询资源列表
     */
    List<ResourceNode> getResourceListByIds(List<Long> ids);

    /**
     * 根据 resources 返回其全路径的节点
     */
    List<ResourceNode> buildFullPathNodes(List<ResourceNode> resources);
    
    /**
     * 更新资源
     */
    boolean updateResource(ResourceNode resource);
    
    /**
     * 删除资源
     */
    boolean deleteResource(Long id);

    /**
     * 批量删除资源
     */
    int batchDeleteResources(List<Long> ids);
    
    /**
     * 检查资源IDs是否存在
     */
    boolean existsById(Long id);
} 