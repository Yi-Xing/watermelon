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
     * 分页查询资源列表
     */
    List<ResourceNode> getResourceList(String name, String code, Integer state, int pageNum, int pageSize);

    /**
     * 统计资源总数
     */
    Long countResources(String name, String code, Integer state);

    /**
     * 根据ID列表查询资源列表
     */
    List<ResourceNode> getResourceListByIds(List<Long> ids);

    /**
     * 根据ID列表查询资源并返回Map映射
     *
     * @param ids 资源ID列表
     * @return 资源ID到ResourceNode的映射
     */
    Map<Long, ResourceNode> getResourceMapByIds(List<Long> ids);

    /**
     * 更新资源
     */
    boolean updateResource(ResourceNode resource);

    /**
     * 删除资源
     */
    boolean deleteResource(Long id);

    /**
     * 校验资源IDs是否合法
     */
    void validateResourceIds(List<Long> resourceIds);

    /**
     * 检查指定资源代码是否存在于指定的资源ID列表中
     * 只查询接口类型和启用状态的资源
     */
    boolean existsAPIResourceByCodeAndIds(String resourceCode, List<Long> resourceIds);

    /**
     * 获取全部资源的 code 到 ResourceNodeID 的映射
     */
    Map<String, Long> getResourceMapByCodes();

    /**
     * 获取所有资源
     */
    List<ResourceNode> findAll();

    /**
     * 根据code前缀、type和resourceIds查询指定状态的资源列表
     * @param codePrefix code前缀
     * @param types 资源类型列表
     * @param resourceIds 资源ID列表
     * @param state 资源状态
     * @return 资源列表
     */
    List<ResourceNode> getResourcesByCodePrefixAndTypesAndIds(String codePrefix, List<Integer> types, List<Long> resourceIds, Integer state);
} 