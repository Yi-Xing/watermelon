package top.fblue.watermelon.domain.resource.repository;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import java.util.List;

/**
 * 资源节点仓储接口
 */
public interface ResourceNodeRepository {
    
    /**
     * 保存资源节点
     */
    ResourceNode save(ResourceNode resourceNode);
    
    /**
     * 根据ID查找资源节点
     */
    ResourceNode findById(Long id);
    
    /**
     * 根据code查找资源节点
     */
    ResourceNode findByCode(String code);
    
    /**
     * 检查资源code是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查同级资源名称是否存在
     */
    boolean existsByNameAndParentId(String name, Long parentId);
    
    /**
     * 根据父级ID查找子资源
     */
    List<ResourceNode> findByParentId(Long parentId);
    
    /**
     * 根据类型查找资源
     */
    List<ResourceNode> findByType(Integer type);
    
    /**
     * 查询所有启用的资源
     */
    List<ResourceNode> findAllEnabled();
    
    /**
     * 删除资源
     */
    boolean delete(Long id);
} 