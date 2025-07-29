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
        validateResourceNodeCreation(resourceNode);

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
    public ResourceNode findByIdWithAssociations(Long id) {
        // 1. 获取资源节点基本信息
        ResourceNode resourceNode = getResourceById(id);
        if (resourceNode == null) {
            return null;
        }
        return resourceNode;
    }

    @Override
    public List<ResourceNode> getResourceList(String name, Integer state) {
        return resourceRepository.findByCondition(name, state);
    }

    @Override
    public boolean updateResource(ResourceNode resource) {
        // 1. 检查资源是否存在
        ResourceNode existingResource = getResourceById(resource.getId());
        
        // 2. 如果修改了父级资源，检查父级资源是否存在
        if (resource.getParentId() != null && resource.getParentId() > 0) {
            if (!existsById(resource.getParentId())) {
                throw new IllegalArgumentException("父级资源不存在");
            }
            
            // 检查是否形成环形引用
            checkCircularReference(resource.getId(), resource.getParentId());
        }
        
        // 3. 如果修改了名称，检查新名称在同级下是否已存在
        if (!existingResource.getName().equals(resource.getName())) {
            if (existsByNameAndParentId(resource.getName(), resource.getParentId())) {
                throw new IllegalArgumentException("同级资源名称不能重复");
            }
        }
        
        // 4. 如果修改了code，检查新code是否已存在
        if (!existingResource.getCode().equals(resource.getCode())) {
            if (existsByCode(resource.getCode())) {
                throw new IllegalArgumentException("资源code已存在");
            }
        }
        
        // 5. 更新资源
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
    public boolean existsById(Long id) {
        return resourceRepository.existsById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return resourceRepository.existsByCode(code);
    }

    @Override
    public boolean existsByNameAndParentId(String name, Long parentId) {
        return resourceRepository.existsByNameAndParentId(name, parentId);
    }
    
    /**
     * 校验资源节点创建的业务规则
     */
    private void validateResourceNodeCreation(ResourceNode resourceNode) {
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
        if (parentId != null) {
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
     * 检查是否形成环形引用
     */
    private void checkCircularReference(Long resourceId, Long parentId) {
        // 使用Set记录已访问的节点，检测环形引用
        Set<Long> visitedNodes = new HashSet<>();
        Long currentParentId = parentId;
        
        while (currentParentId != null && currentParentId > 0) {
            // 如果当前节点已经访问过，说明存在环形引用
            if (visitedNodes.contains(currentParentId)) {
                throw new IllegalArgumentException("检测到环形引用，无法更新资源");
            }
            
            // 如果当前父节点是要修改的资源ID，说明形成了环
            if (currentParentId.equals(resourceId)) {
                throw new IllegalArgumentException("检测到环形引用，无法更新资源");
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
} 