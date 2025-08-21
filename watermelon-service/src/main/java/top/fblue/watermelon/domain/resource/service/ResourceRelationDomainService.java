package top.fblue.watermelon.domain.resource.service;

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
     * 构建树形结构的资源ID列表
     */
    List<Long> buildTreeResourceIds(List<Long> filteredResourceIds);
}
