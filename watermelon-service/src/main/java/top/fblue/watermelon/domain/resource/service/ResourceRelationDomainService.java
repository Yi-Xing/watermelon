package top.fblue.watermelon.domain.resource.service;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import java.util.List;

/**
 * 资源关联领域服务接口
 */
public interface ResourceRelationDomainService {
    
    /**
     * 创建资源关联
     */
    ResourceRelation createResourceRelation(ResourceRelation resourceRelation);
    
    /**
     * 根据ID获取资源关联
     */
    ResourceRelation getResourceRelationById(Long id);
    
    /**
     * 根据父级ID获取子关联
     */
    List<ResourceRelation> getResourceRelationsByParentId(Long parentId);
    
    /**
     * 根据子级ID获取父关联
     */
    List<ResourceRelation> getResourceRelationsByChildId(Long childId);
    
    /**
     * 更新资源关联
     */
    boolean updateResourceRelation(ResourceRelation resourceRelation);
    
    /**
     * 删除资源关联
     */
    boolean deleteResourceRelation(Long id);
    
    /**
     * 获取所有资源关联
     */
    List<ResourceRelation> getAllResourceRelations();
    
    /**
     * 根据过滤后的资源和关联关系构建完整的资源列表
     * 包含构建树形结构所需的所有父级节点
     */
    List<Long> buildCompleteResourceIds(List<ResourceNode> filteredResources, List<ResourceRelation> relations);

    /**
     * 批量创建资源关联
     */
    List<ResourceRelation> batchCreateResourceRelations(List<ResourceRelation> resourceRelations);

    /**
     * 批量删除资源关联
     */
    int batchDelete(List<Long> ids);

    /**
     * 批量更新资源关联
     */
    boolean batchUpdateResourceRelations(List<ResourceRelation> resourceRelations);

    /**
     * 删除所有资源关联
     */
    boolean deleteAllResourceRelations();
}
