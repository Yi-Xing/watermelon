package top.fblue.watermelon.domain.resource.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.repository.ResourceRepository;
import top.fblue.watermelon.domain.resource.repository.ResourceRelationRepository;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源领域服务实现
 */
@Service
public class ResourceDomainServiceImpl implements ResourceDomainService {

    @Resource
    private ResourceRepository resourceRepository;
    
    @Resource
    private ResourceRelationRepository resourceRelationRepository;

    @Override
    public ResourceNode createResourceNode(ResourceNode resourceNode) {
        // 校验业务规则
        validateResourceNode(resourceNode);

        // 保存资源节点
        return resourceRepository.save(resourceNode);
    }

    @Override
    public ResourceNode getResourceById(Long id) {
        ResourceNode resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        return resource;
    }

    @Override
    public List<ResourceNode> getResourceList(String name, String code, Integer state) {
        return resourceRepository.findByCondition(name, code, state);
    }
    
    @Override
    public List<ResourceNode> getResourceList(String name, String code, Integer state, int pageNum, int pageSize) {
        return resourceRepository.findByCondition(name, code, state, pageNum, pageSize);
    }
    
    @Override
    public Long countResources(String name, String code, Integer state) {
        return resourceRepository.countByCondition(name, code, state);
    }

    @Override
    public List<ResourceNode> getResourceListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return resourceRepository.findByIds(ids);
    }
    
    @Override
    public Map<Long, ResourceNode> getResourceMapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        
        List<ResourceNode> resources = resourceRepository.findByIds(ids);
        return resources.stream()
                .collect(Collectors.toMap(ResourceNode::getId, resource -> resource));
    }

    @Override
    public List<ResourceNode> buildFullPathNodes(List<ResourceNode> resources) {
        // 新架构下，这个方法不再需要，因为树形结构通过ResourceRelation管理
        // 直接返回传入的资源列表
        return resources != null ? resources : new ArrayList<>();
    }

    @Override
    public boolean updateResource(ResourceNode resource) {
        // 1. 校验业务规则
        validateResourceNodeUpdate(resource);

        // 2. 更新资源
        return resourceRepository.update(resource);
    }

    @Override
    public boolean deleteResource(Long id) {
        // 校验资源是否存在
        ResourceNode resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }

        // 检查是否有资源关联关系（作为父级或子级）
        if (resourceRelationRepository.hasAnyRelation(id)) {
            throw new BusinessException("该资源存在关联关系，无法删除");
        }

        // 删除资源
        return resourceRepository.delete(id);
    }

    @Override
    public void validateResourceIds(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }

        // 批量查询资源信息
        List<ResourceNode> resources = getResourceListByIds(resourceIds);
        if (resources.size() == resourceIds.size()) {
            return;
        }
        // 找出不存在的资源ID
        Set<Long> existResourceIdSet = resources.stream()
                .map(ResourceNode::getId)
                .collect(Collectors.toSet());

        List<Long> notExistResourceIds = resourceIds.stream()
                .filter(id -> !existResourceIdSet.contains(id))
                .toList();
        if (!notExistResourceIds.isEmpty()) {
            String ids = notExistResourceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            throw new BusinessException("以下资源ID不存在: " + ids);
        }
    }

    @Override
    public boolean existsAPIResourceByCodeAndIds(String resourceCode, List<Long> resourceIds) {
        if (StringUtil.isEmpty(resourceCode) || resourceIds == null || resourceIds.isEmpty()) {
            return false;
        }
        return resourceRepository.existsByIdsAndTypeAndStateCode(
                resourceIds,
                ResourceTypeEnum.API.getCode(),
                StateEnum.ENABLE.getCode(),
                resourceCode);
    }

    /**
     * 校验资源节点（新增时）
     */
    private void validateResourceNode(ResourceNode resourceNode) {
        // 校验Code唯一性
        if (resourceRepository.existsByCode(resourceNode.getCode())) {
            throw new BusinessException("资源Code已存在");
        }
    }

    /**
     * 校验资源节点（更新时）
     */
    private void validateResourceNodeUpdate(ResourceNode resourceNode) {
        // 1. 校验资源是否存在
        ResourceNode existingResource = resourceRepository.findById(resourceNode.getId());
        if (existingResource == null) {
            throw new BusinessException("资源不存在");
        }
        // 2. 如果code有变化，校验新code的唯一性
        if (!existingResource.getCode().equals(resourceNode.getCode())) {
            if (resourceRepository.existsByCodeExcludeId(resourceNode.getCode(), resourceNode.getId())) {
                throw new BusinessException("资源Code已存在");
            }
        }
    }

    @Override
    public Map<String, Long> getResourceMapByCodes() {
        List<ResourceNode> resources = resourceRepository.findAll();
        return resources.stream().collect(Collectors.toMap(ResourceNode::getCode, ResourceNode::getId));
    }

    /**
     * 获取所有资源
     */
    @Override
    public List<ResourceNode> findAll(){
        return resourceRepository.findAll();
    }
}
