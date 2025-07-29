package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.repository.ResourceRepository;
import top.fblue.watermelon.infrastructure.converter.ResourceNodePOConverter;
import top.fblue.watermelon.infrastructure.mapper.ResourceNodeMapper;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 资源仓储实现
 */
@Repository
public class ResourceRepositoryImpl implements ResourceRepository {

    @Resource
    private ResourceNodeMapper resourceNodeMapper;
    @Resource
    private ResourceNodePOConverter resourceNodePOConverter;

    @Override
    public ResourceNode save(ResourceNode resourceNode) {
        ResourceNodePO po = resourceNodePOConverter.toPO(resourceNode);
        if (po.getId() == null) {
            resourceNodeMapper.insert(po);
        } else {
            resourceNodeMapper.updateById(po);
        }
        ResourceNodePO savedPO = resourceNodeMapper.selectById(po.getId());
        return resourceNodePOConverter.toDomain(savedPO);
    }

    @Override
    public ResourceNode findById(Long id) {
        ResourceNodePO po = resourceNodeMapper.selectById(id);
        return resourceNodePOConverter.toDomain(po);
    }

    @Override
    public ResourceNode findByCode(String code) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        ResourceNodePO po = resourceNodeMapper.selectOne(queryWrapper);
        return resourceNodePOConverter.toDomain(po);
    }

    @Override
    public boolean existsByCode(String code) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        return resourceNodeMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean existsByNameAndParentId(String name, Long parentId) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("parent_id", Objects.requireNonNullElse(parentId, 0));
        return resourceNodeMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<ResourceNode> findByParentId(Long parentId) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.eq("state", 1); // 只查询启用的资源
        queryWrapper.orderByAsc("order_num");
        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .map(resourceNodePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceNode> findByType(Integer type) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type);
        queryWrapper.eq("state", 1); // 只查询启用的资源
        queryWrapper.orderByAsc("order_num");
        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .map(resourceNodePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceNode> findAllEnabled() {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", 1);
        queryWrapper.orderByAsc("parent_id").orderByAsc("order_num");
        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .map(resourceNodePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(Long id) {
        return resourceNodeMapper.deleteById(id) > 0;
    }

    @Override
    public boolean update(ResourceNode resource) {
        ResourceNodePO po = resourceNodePOConverter.toPO(resource);
        return resourceNodeMapper.updateById(po) > 0;
    }
    
    @Override
    public boolean existsById(Long id) {
        return resourceNodeMapper.selectById(id) != null;
    }
    
    @Override
    public List<ResourceNode> findByCondition(String name, Integer state) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        
        if (state != null) {
            queryWrapper.eq("state", state);
        }
        
        queryWrapper.orderByAsc("order_num").orderByDesc("updated_time");
        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .map(resourceNodePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 根据条件分页查询资源列表
     */
    public List<ResourceNodePO> findByCondition(String keyword, Integer type, Integer state, Long parentId, int pageNum, int pageSize) {
        QueryWrapper<ResourceNodePO> queryWrapper = buildQueryWrapper(keyword, type, state, parentId);
        queryWrapper.orderByAsc("order_num").orderByDesc("updated_time");
        
        Page<ResourceNodePO> page = new Page<>(pageNum, pageSize);
        IPage<ResourceNodePO> pageResult = resourceNodeMapper.selectPage(page, queryWrapper);
        
        return pageResult.getRecords();
    }

    /**
     * 根据条件统计资源总数
     */
    public Long countByCondition(String keyword, Integer type, Integer state, Long parentId) {
        QueryWrapper<ResourceNodePO> queryWrapper = buildQueryWrapper(keyword, type, state, parentId);
        return resourceNodeMapper.selectCount(queryWrapper);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<ResourceNodePO> buildQueryWrapper(String keyword, Integer type, Integer state, Long parentId) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        
        // 关键词模糊查询（资源名、资源代码）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("code", keyword)
            );
        }
        
        // 类型筛选
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        
        // 状态筛选
        if (state != null) {
            queryWrapper.eq("state", state);
        }
        
        // 父级ID筛选
        if (parentId != null) {
            queryWrapper.eq("parent_id", parentId);
        }
        
        return queryWrapper;
    }
} 