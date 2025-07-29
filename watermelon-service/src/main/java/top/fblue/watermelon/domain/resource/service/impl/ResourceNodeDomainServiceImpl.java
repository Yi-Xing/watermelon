package top.fblue.watermelon.domain.resource.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.repository.ResourceNodeRepository;
import top.fblue.watermelon.domain.resource.service.ResourceNodeDomainService;

import java.util.HashSet;
import java.util.Set;

/**
 * 资源节点领域服务实现
 */
@Service
public class ResourceNodeDomainServiceImpl implements ResourceNodeDomainService {

    @Resource
    private ResourceNodeRepository resourceNodeRepository;

    @Override
    public ResourceNode createResourceNode(ResourceNode resourceNode) {
        // 校验业务规则
        validateResourceNodeCreation(resourceNode);

        // 保存资源节点
        return resourceNodeRepository.save(resourceNode);
    }

    @Override
    public ResourceNode findById(Long id) {
        return resourceNodeRepository.findById(id);
    }

    @Override
    public ResourceNode findByIdWithAssociations(Long id) {
        // 1. 获取资源节点基本信息
        ResourceNode resourceNode = findById(id);
        if (resourceNode == null) {
            return null;
        }
        return resourceNode;
    }

    private void validateResourceNodeCreation(ResourceNode resourceNode) {
        // 1. 校验父级资源
        validateParentResource(resourceNode.getParentId());

        // 2. 校验环形引用
        validateCircularReference(resourceNode.getParentId());

        // 3. 校验资源名称唯一性
        validateResourceNameUnique(resourceNode.getName(), resourceNode.getParentId());

        // 4. 校验资源code唯一性
        validateResourceCodeUnique(resourceNode.getCode());
    }

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
            ResourceNode currentResource = resourceNodeRepository.findById(currentParentId);
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

    private void validateResourceNameUnique(String name, Long parentId) {
        boolean exists = resourceNodeRepository.existsByNameAndParentId(name, parentId);
        if (exists) {
            throw new IllegalArgumentException("同级资源名称不能重复");
        }
    }

    private void validateResourceCodeUnique(String code) {
        boolean exists = resourceNodeRepository.existsByCode(code);
        if (exists) {
            throw new IllegalArgumentException("资源code已存在");
        }
    }

    private void validateParentResource(Long parentId) {
        if (parentId != null) {
            ResourceNode parentResource = resourceNodeRepository.findById(parentId);
            if (parentResource == null) {
                throw new IllegalArgumentException("上级资源不存在");
            }
            // 检查上级资源是否启用
            if (!StateEnum.ENABLE.getCode().equals(parentResource.getState())) {
                throw new IllegalArgumentException("上级资源已禁用，无法创建子资源");
            }
        }
    }
} 