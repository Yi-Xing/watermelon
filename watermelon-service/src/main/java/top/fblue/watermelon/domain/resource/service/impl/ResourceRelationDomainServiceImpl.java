package top.fblue.watermelon.domain.resource.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.repository.ResourceRelationRepository;
import top.fblue.watermelon.domain.resource.repository.ResourceRepository;
import top.fblue.watermelon.domain.resource.service.ResourceRelationDomainService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

/**
 * 资源关联领域服务实现
 */
@Slf4j
@Service
public class ResourceRelationDomainServiceImpl implements ResourceRelationDomainService {

    @Resource
    private ResourceRelationRepository resourceRelationRepository;

    @Resource
    private ResourceRepository resourceRepository;

    @Override
    public ResourceRelation createResourceRelation(ResourceRelation resourceRelation) {
        // 1. 校验子级资源是否存在
        if (resourceRepository.findById(resourceRelation.getChildId()) == null) {
            throw new BusinessException("子级资源不存在");
        }

        // 2. 校验父级资源（如果不为空）
        if (resourceRelation.getParentId() != null) {
            if (resourceRepository.findById(resourceRelation.getParentId()) == null) {
                throw new BusinessException("父级资源不存在");
            }
        }

        // 3. 校验是否已存在关联关系
        if (resourceRelationRepository.existsByParentIdAndChildId(
                resourceRelation.getParentId(), resourceRelation.getChildId())) {
            throw new BusinessException("关联关系已存在");
        }

        // 4. 校验是否会形成环形依赖（仅当父级资源不为空时）
        if (hasCyclicDependency(resourceRelation.getParentId(), resourceRelation.getChildId())) {
            throw new BusinessException("不能创建环形依赖关系");
        }

        // 5. 保存关联关系
        return resourceRelationRepository.save(resourceRelation);
    }

    @Override
    public ResourceRelation getResourceRelationById(Long id) {
        ResourceRelation resourceRelation = resourceRelationRepository.findById(id);
        if (resourceRelation == null) {
            throw new BusinessException("资源关联不存在");
        }
        return resourceRelation;
    }

    @Override
    public List<ResourceRelation> getResourceRelationsByParentId(Long parentId) {
        return resourceRelationRepository.findByParentId(parentId);
    }

    @Override
    public List<ResourceRelation> getResourceRelationsByChildId(Long childId) {
        return resourceRelationRepository.findByChildId(childId);
    }

    @Override
    public boolean updateResourceRelation(ResourceRelation resourceRelation) {
        // 1. 校验关联关系是否存在
        ResourceRelation existingRelation = resourceRelationRepository.findById(resourceRelation.getId());
        if (existingRelation == null) {
            throw new BusinessException("资源关联不存在");
        }

        // 2. 如果父子关系有变化，需要重新校验
        boolean parentChanged = !java.util.Objects.equals(existingRelation.getParentId(), resourceRelation.getParentId());
        boolean childChanged = !existingRelation.getChildId().equals(resourceRelation.getChildId());

        if (parentChanged || childChanged) {

            // 校验子级资源是否存在
            if (resourceRepository.findById(resourceRelation.getChildId()) == null) {
                throw new BusinessException("子级资源不存在");
            }

            // 校验父级资源（如果不为空）
            if (resourceRelation.getParentId() != null) {
                if (resourceRepository.findById(resourceRelation.getParentId()) == null) {
                    throw new BusinessException("父级资源不存在");
                }
            }

            // 校验是否会形成环形依赖（仅当父级资源不为空时）
            if (hasCyclicDependency(resourceRelation.getParentId(), resourceRelation.getChildId())) {
                throw new BusinessException("不能创建环形依赖关系");
            }
        }

        return resourceRelationRepository.update(resourceRelation);
    }

    @Override
    public boolean deleteResourceRelation(Long id) {
        // 1. 校验关联关系是否存在
        ResourceRelation resourceRelation = resourceRelationRepository.findById(id);
        if (resourceRelation == null) {
            throw new BusinessException("资源关联不存在");
        }

        // 2. 校验是否有子级关联（不能删除有子级的关联）
        List<ResourceRelation> childRelations = resourceRelationRepository.findByParentId(resourceRelation.getChildId());
        if (!childRelations.isEmpty()) {
            throw new BusinessException("该资源关联下还有子级关联，无法删除");
        }

        return resourceRelationRepository.delete(id);
    }

    @Override
    public List<ResourceRelation> getAllResourceRelations() {
        return resourceRelationRepository.findAll();
    }

    @Override
    public List<Long> buildTreeResourceIds(List<Long> filteredResourceIds) {
        if (filteredResourceIds == null || filteredResourceIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有资源关联
        List<ResourceRelation> allRelations = resourceRelationRepository.findAll();

        // 构建父子关系映射
        Map<Long, List<Long>> childToParentsMap = allRelations.stream()
                .collect(Collectors.groupingBy(
                        ResourceRelation::getChildId,
                        Collectors.mapping(ResourceRelation::getParentId, Collectors.toList())
                ));

        Set<Long> resultIds = new HashSet<>(filteredResourceIds);

        // 对每个过滤出的资源，递归添加其所有父级资源
        for (Long resourceId : filteredResourceIds) {
            addParentResourceIds(resourceId, childToParentsMap, resultIds);
        }

        return new ArrayList<>(resultIds);
    }

    /**
     * 在已有 DAG 中新增一条边（父->子）时，判断是否会形成环
     * 这种场景不需要每次遍历整个图，只需要检查新增的这条边是否会导致 子节点可以回到父节点。
     * 换句话说，如果从 子节点出发能够到达父节点，再新增父->子这条边就会形成环。
     *
     * @param parentId 新增父节点ID
     * @param childId  新增子节点ID
     * @return true表示会形成环，false表示不会形成环
     */
    private boolean hasCyclicDependency(Long parentId, Long childId) {
        // 如果父级ID为空，表示顶级资源，不存在环形依赖
        if (parentId == null) {
            return false;
        }
        // 如果childId等于parentId，直接形成环
        if (childId.equals(parentId)) {
            return true;
        }

        // 使用visited集合记录已访问的节点，避免重复访问
        Set<Long> visited = new HashSet<>();

        // 从子节点出发，DFS 是否能到达父节点
        return dfsDetectCycle(childId, parentId, visited);
    }

    /**
     * 使用DFS + visited集合检测是否有环
     *
     * @param currentNode 当前节点
     * @param targetNode  目标节点（要检测是否会形成环的节点）
     * @param visited     已访问的节点集合
     * @return true表示会形成环，false表示不会形成环
     */
    private boolean dfsDetectCycle(Long currentNode, Long targetNode, Set<Long> visited) {
        // 找到父节点，形成环
        if (currentNode.equals(targetNode)) {
            return true;
        }

        // 如果当前节点已经访问过，说明这条路径不会形成环
        if (visited.contains(currentNode)) {
            return false;
        }

        // 将当前节点加入已访问集合
        visited.add(currentNode);

        // 查找当前节点的所有子级节点
        List<ResourceRelation> parentRelations = resourceRelationRepository.findByParentId(currentNode);

        for (ResourceRelation relation : parentRelations) {
            // 递归检测子级节点
            if (dfsDetectCycle(relation.getChildId(), targetNode, visited)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 递归添加父级资源ID
     */
    private void addParentResourceIds(Long resourceId,
                                      Map<Long, List<Long>> childToParentsMap,
                                      Set<Long> resultIds) {
        List<Long> parentIds = childToParentsMap.get(resourceId);
        if (parentIds != null) {
            for (Long parentId : parentIds) {
                if (parentId != null && resultIds.add(parentId)) {
                    // 递归添加父级的父级
                    addParentResourceIds(parentId, childToParentsMap, resultIds);
                }
            }
        }
    }
}
