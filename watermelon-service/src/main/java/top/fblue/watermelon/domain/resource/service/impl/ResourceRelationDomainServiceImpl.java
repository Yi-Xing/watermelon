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
        if (resourceRelation.getParentId() != null && 
            resourceRelationRepository.hasCyclicDependency(
                resourceRelation.getParentId(), resourceRelation.getChildId())) {
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
            if (resourceRelation.getParentId() != null && 
                resourceRelationRepository.hasCyclicDependency(
                    resourceRelation.getParentId(), resourceRelation.getChildId())) {
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
     * 递归添加父级资源ID
     */
    private void addParentResourceIds(Long resourceId, Map<Long, List<Long>> childToParentsMap, Set<Long> resultIds) {
        List<Long> parentIds = childToParentsMap.get(resourceId);
        if (parentIds != null) {
            for (Long parentId : parentIds) {
                if (resultIds.add(parentId)) { // 如果是新添加的父级ID，继续递归
                    addParentResourceIds(parentId, childToParentsMap, resultIds);
                }
            }
        }
    }
}
