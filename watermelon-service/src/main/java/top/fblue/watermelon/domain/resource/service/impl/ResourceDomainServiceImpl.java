package top.fblue.watermelon.domain.resource.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.repository.ResourceRepository;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 资源领域服务实现
 */
@Service
public class ResourceDomainServiceImpl implements ResourceDomainService {

    @Resource
    private ResourceRepository resourceRepository;

    @Override
    public ResourceNode createResourceNode(ResourceNode resourceNode) {
        // 校验业务规则
        validateResourceNode(resourceNode);

        // 保存资源节点
        return resourceRepository.save(resourceNode);
    }

    @Override
    public ResourceNode getResourceById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }

        ResourceNode resource = resourceRepository.findById(id);
        if (resource == null) {
            throw new IllegalArgumentException("资源不存在");
        }

        return resource;
    }

    @Override
    public List<ResourceNode> getResourceList(String name, String code, Integer state) {
        return resourceRepository.findByCondition(name, code, state);
    }

    @Override
    public List<ResourceNode> getResourceListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return resourceRepository.findByIds(ids);
    }

    @Override
    public List<ResourceNode> buildFullPathNodes(List<ResourceNode> resources) {
        Set<Long> resourceIds = new HashSet<>();
        // 收集所有资源的ID
        for (ResourceNode resource : resources) {
            resourceIds.add(resource.getId());
        }

        // 收集所有资源父ID
        for (ResourceNode resource : resources) {
            // 递归收集所有父节点ID
            collectParentIds(resource.getParentId(), resourceIds);
        }

        // 查询所有需要的资源
        return this.getResourceListByIds(new ArrayList<>(resourceIds));
    }

    /**
     * 递归收集父节点ID
     */
    private void collectParentIds(Long parentId, Set<Long> parentIds) {
        if (parentId == null || parentId == 0 || parentIds.contains(parentId)) {
            return;
        }

        parentIds.add(parentId);

        // 获取父节点信息
        ResourceNode parent = this.getResourceById(parentId);
        if (parent != null && parent.getParentId() != null && parent.getParentId() != 0) {
            collectParentIds(parent.getParentId(), parentIds);
        }
    }

    @Override
    public boolean updateResource(ResourceNode resource) {
        // 1. 检查资源是否存在
        ResourceNode existingResource = getResourceById(resource.getId());

        // 2. 校验业务规则（更新时的特殊校验）
        validateResourceNodeForUpdate(resource, existingResource);

        // 3. 更新资源
        return resourceRepository.update(resource);
    }

    @Override
    public boolean deleteResource(Long id) {
        // 1. 检查资源是否存在
        getResourceById(id);

        // 2. 检查是否有子资源
        List<ResourceNode> children = resourceRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("该资源下有子资源，无法删除");
        }

        // 3. 删除资源
        return resourceRepository.delete(id);
    }
    
    @Override
    public int batchDeleteResources(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 1. 检查所有资源是否存在
        List<ResourceNode> resources = getResourceListByIds(ids);
        if (resources.size() != ids.size()) {
            throw new IllegalArgumentException("部分资源不存在");
        }
        
        // 2. 检查是否有子资源
        for (ResourceNode resource : resources) {
            List<ResourceNode> children = resourceRepository.findByParentId(resource.getId());
            if (!children.isEmpty()) {
                throw new IllegalArgumentException("资源 [" + resource.getName() + "] 下有子资源，无法删除");
            }
        }
        
        // 3. 批量删除资源
        return resourceRepository.batchDelete(ids);
    }

    @Override
    public boolean existsById(Long id) {
        return resourceRepository.existsById(id);
    }

    /**
     * 校验资源节点的业务规则
     */
    private void validateResourceNode(ResourceNode resourceNode) {
        // 1. 校验父级资源
        validateParentResource(resourceNode.getParentId());

        // 2. 校验环形引用
        validateCircularReference(resourceNode.getParentId());

        // 3. 校验同级资源名称是否已存在
        validateSiblingResourceNameExists(resourceNode.getName(), resourceNode.getParentId());

        // 4. 校验资源code唯一性
        validateResourceCodeUnique(resourceNode.getCode());
    }

    /**
     * 更新时校验资源节点的业务规则
     */
    private void validateResourceNodeForUpdate(ResourceNode resourceNode, ResourceNode existingResource) {
        // 1. 校验自己不能为自己的父节点
        validateSelfAsParent(resourceNode.getId(), resourceNode.getParentId());

        // 2. 校验父级资源
        validateParentResource(resourceNode.getParentId());

        // 3. 校验环形引用
        validateCircularReference(resourceNode.getParentId());

        // 4. 校验同级资源名称是否已存在（过滤掉自己）

        if (!existingResource.getName().equals(resourceNode.getName())) {
            validateSiblingResourceNameExists(resourceNode.getName(), resourceNode.getParentId());
        }

        // 5. 校验资源code唯一性（过滤掉自己）
        if (!existingResource.getCode().equals(resourceNode.getCode())) {
            validateResourceCodeUnique(resourceNode.getCode());
        }
    }

    /**
     * 校验环形引用
     */
    private void validateCircularReference(Long parentId) {
        if (parentId == null) {
            return;
        }

        // 使用Set记录已访问的节点，检测环形引用
        Set<Long> visitedNodes = new HashSet<>();
        Long currentParentId = parentId;

        while (currentParentId != null) {
            // 如果当前节点已经访问过，说明存在环形引用
            if (visitedNodes.contains(currentParentId)) {
                throw new IllegalArgumentException("检测到环形引用，无法创建资源");
            }

            // 将当前节点加入已访问集合
            visitedNodes.add(currentParentId);

            // 查询当前节点的父节点
            ResourceNode currentResource = resourceRepository.findById(currentParentId);
            if (currentResource == null) {
                break; // 父节点不存在，结束检查
            }

            currentParentId = currentResource.getParentId();

            // 防止无限循环，设置最大深度限制
            if (visitedNodes.size() > 100) {
                throw new IllegalArgumentException("资源层级过深，可能存在环形引用");
            }
        }
    }

    /**
     * 校验同级资源名称是否已存在
     */
    private void validateSiblingResourceNameExists(String name, Long parentId) {
        boolean exists = resourceRepository.existsByNameAndParentId(name, parentId);
        if (exists) {
            throw new IllegalArgumentException("同级资源名称不能重复");
        }
    }

    /**
     * 校验资源code唯一性
     */
    private void validateResourceCodeUnique(String code) {
        boolean exists = resourceRepository.existsByCode(code);
        if (exists) {
            throw new IllegalArgumentException("资源code已存在");
        }
    }

    /**
     * 校验父级资源
     */
    private void validateParentResource(Long parentId) {
        if (parentId != null && parentId != 0) {
            ResourceNode parentResource = resourceRepository.findById(parentId);
            if (parentResource == null) {
                throw new IllegalArgumentException("上级资源不存在");
            }
            // 检查上级资源是否启用
            if (!StateEnum.ENABLE.equals(StateEnum.fromCode(parentResource.getState()))) {
                throw new IllegalArgumentException("上级资源已禁用，无法创建子资源");
            }
        }
    }

    /**
     * 校验自己不能为自己的父节点
     */
    private void validateSelfAsParent(Long resourceId, Long parentId) {
        if (resourceId != null && resourceId.equals(parentId)) {
            throw new IllegalArgumentException("自己不能为自己的父节点");
        }
    }
}