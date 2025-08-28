package top.fblue.watermelon.domain.resource.repository;

import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import java.util.List;

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
     * 根据条件查询资源列表
     */
    List<ResourceNode> findByCondition(String name, String code, Integer state);
    
    /**
     * 分页查询资源列表
     */
    List<ResourceNode> findByCondition(String name, String code, Integer state, int pageNum, int pageSize);
    
    /**
     * 根据条件统计资源总数
     */
    Long countByCondition(String name, String code, Integer state);
    
    /**
     * 根据ID列表查询资源列表
     */
    List<ResourceNode> findByIds(List<Long> ids);
    
    /**
     * 更新资源
     */
    boolean update(ResourceNode resource);
    
    /**
     * 删除资源
     */
    boolean delete(Long id);
    
    /**
     * 批量删除资源
     */
    int deleteByIds(List<Long> ids);
    
    /**
     * 检查资源code是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查资源code是否存在（排除指定ID）
     */
    boolean existsByCodeExcludeId(String code, Long id);
    
    /**
     * 获取所有资源
     */
    List<ResourceNode> getAllResources();

    /**
     * 检查指定资源代码是否存在于指定的资源ID列表中
     */
    boolean existsByIdsAndTypeAndStateCode(List<Long> resourceIds, Integer type, Integer state, String code);

    /**
     * 根据code前缀、type和resourceIds查询指定状态的资源列表
     * @param codePrefix code前缀
     * @param types 资源类型列表
     * @param resourceIds 资源ID列表
     * @param state 资源状态
     * @return 资源列表
     */
    List<ResourceNode> findByCodePrefixAndTypesAndIds(String codePrefix, List<Integer> types, List<Long> resourceIds, Integer state);
} 