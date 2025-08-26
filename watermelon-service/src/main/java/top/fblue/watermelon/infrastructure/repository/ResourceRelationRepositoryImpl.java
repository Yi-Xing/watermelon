package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.repository.ResourceRelationRepository;
import top.fblue.watermelon.infrastructure.converter.ResourceRelationPOConverter;
import top.fblue.watermelon.infrastructure.mapper.ResourceRelationMapper;
import top.fblue.watermelon.infrastructure.po.ResourceRelationPO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源关系仓储实现
 */
@Repository
public class ResourceRelationRepositoryImpl implements ResourceRelationRepository {

    @Resource
    private ResourceRelationMapper resourceRelationMapper;

    @Override
    public ResourceRelation save(ResourceRelation resourceRelation) {
        ResourceRelationPO po = ResourceRelationPOConverter.toPO(resourceRelation);
        if (po.getId() == null) {
            resourceRelationMapper.insert(po);
        } else {
            resourceRelationMapper.updateById(po);
        }
        return ResourceRelationPOConverter.toDomain(po);
    }

    @Override
    public ResourceRelation findById(Long id) {
        ResourceRelationPO po = resourceRelationMapper.selectById(id);
        return ResourceRelationPOConverter.toDomain(po);
    }

    @Override
    public List<ResourceRelation> findByParentId(Long parentId) {
        LambdaQueryWrapper<ResourceRelationPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceRelationPO::getParentId, parentId);
        queryWrapper.orderByAsc(ResourceRelationPO::getOrderNum);

        List<ResourceRelationPO> poList = resourceRelationMapper.selectList(queryWrapper);
        return poList.stream()
                .map(ResourceRelationPOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByParentIdAndChildId(Long parentId, Long childId) {
        LambdaQueryWrapper<ResourceRelationPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceRelationPO::getParentId, parentId);
        queryWrapper.eq(ResourceRelationPO::getChildId, childId);

        return resourceRelationMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByChildId(Long childId) {
        LambdaQueryWrapper<ResourceRelationPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ResourceRelationPO::getChildId, childId);
        return resourceRelationMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean hasAnyRelation(Long resourceId) {
        // 检查资源是否作为父级或子级存在关联关系
        LambdaQueryWrapper<ResourceRelationPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(ResourceRelationPO::getParentId, resourceId)
                .or()
                .eq(ResourceRelationPO::getChildId, resourceId)
        );
        return resourceRelationMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean update(ResourceRelation resourceRelation) {
        ResourceRelationPO po = ResourceRelationPOConverter.toPO(resourceRelation);
        return resourceRelationMapper.updateById(po) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return resourceRelationMapper.deleteById(id) > 0;
    }

    @Override
    public List<ResourceRelation> findAll() {
        List<ResourceRelationPO> poList = resourceRelationMapper.selectList(null);
        return poList.stream()
                .map(ResourceRelationPOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return resourceRelationMapper.deleteByIds(ids);
    }

    @Override
    public List<ResourceRelation> findByResourceIds(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        
        // 查询资源作为父级或子级存在的所有关系
        LambdaQueryWrapper<ResourceRelationPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .in(ResourceRelationPO::getParentId, resourceIds)
                .or()
                .in(ResourceRelationPO::getChildId, resourceIds)
        );
        
        List<ResourceRelationPO> poList = resourceRelationMapper.selectList(queryWrapper);
        return poList.stream()
                .map(ResourceRelationPOConverter::toDomain)
                .collect(Collectors.toList());
    }
}
