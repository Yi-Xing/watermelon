package top.fblue.watermelon.domain.resource.repository;

import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import java.util.List;

/**
 * 资源关系仓储接口
 */
public interface ResourceRelationRepository {
    
    /**
     * 保存资源关系
     */
    ResourceRelation save(ResourceRelation resourceRelation);
    
    /**
     * 根据ID查找资源关系
     */
    ResourceRelation findById(Long id);
    
    /**
     * 根据父级ID查找子关系
     */
    List<ResourceRelation> findByParentId(Long parentId);
    
    /**
     * 根据子级ID查找父关系
     */
    List<ResourceRelation> findByChildId(Long childId);
    
    /**
     * 检查是否存在关系
     */
    boolean existsByParentIdAndChildId(Long parentId, Long childId);
    
    /**
     * 检查资源是否有任何关联关系（作为父级或子级）
     * @param resourceId 资源ID
     * @return true表示有关联关系，false表示没有关联关系
     */
    boolean hasAnyRelation(Long resourceId);
    
    /**
     * 更新资源关系
     */
    boolean update(ResourceRelation resourceRelation);
    
    /**
     * 删除资源关系
     */
    boolean delete(Long id);
    
    /**
     * 根据父级ID删除所有子关系
     */
    int deleteByParentId(Long parentId);
    
    /**
     * 根据子级ID删除所有父关系
     */
    int deleteByChildId(Long childId);
    
    /**
     * 获取所有资源关系
     */
    List<ResourceRelation> findAll();

    /**
     * 批量删除资源关系
     */
    int deleteByIds(List<Long> ids);

    /**
     * 删除所有资源关系
     */
    boolean deleteAll();
}
