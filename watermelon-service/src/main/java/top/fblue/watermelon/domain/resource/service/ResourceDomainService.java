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
     * 根据ID查找资源节点并填充关联信息
     * 包含创建人、更新人、父节点等信息
     */
    ResourceNode findByIdWithAssociations(Long id);
    
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
     * 根据code获取资源ID
     */
    Long getResourceIdByCode(String code);
    
    /**
     * 根据code查找资源
     */
    ResourceNode findByCode(String code);
    
    /**
     * 根据code列表批量获取资源ID映射
     */
    Map<String, Long> getResourceIdMapByCodes(List<String> codes);
    
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