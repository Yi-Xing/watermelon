package top.fblue.watermelon.domain.resource.repository;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import java.util.List;
import java.util.Map;

/**
 * 资源仓储接口
 */
public interface ResourceRepository {
    
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
     * 根据条件查询资源列表
     */
    List<ResourceNode> findByCondition(String name, String code, Integer state);
    
    /**
     * 根据ID列表查询资源列表
     */
    List<ResourceNode> findByIds(List<Long> ids);
    
    /**
     * 根据父级ID查找子资源
     */
    List<ResourceNode> findByParentId(Long parentId);

    /**
     * 查询所有启用的资源
     */
    List<ResourceNode> findAllEnabled();
    
    /**
     * 更新资源
     */
    boolean update(ResourceNode resource);
    
    /**
     * 删除资源
     */
    boolean delete(Long id);
    
    /**
     * 检查资源ID是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查资源code是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查同级资源名称是否存在
     */
    boolean existsByNameAndParentId(String name, Long parentId);
    
    
    /**
     * 根据code列表批量获取资源ID映射
     */
    Map<String, Long> findIdMapByCodes(List<String> codes);
} 