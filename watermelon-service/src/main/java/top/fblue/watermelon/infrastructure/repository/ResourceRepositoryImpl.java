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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

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
        resourceNodeMapper.insert(po);
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
    public List<ResourceNode> findByCondition(String name, String code, Integer state) {
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }

        if (StringUtils.hasText(code)) {
            queryWrapper.like("code", code);
        }

        if (state != null) {
            queryWrapper.eq("state", state);
        }

        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .map(resourceNodePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceNode> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        queryWrapper.orderByAsc("order_num").orderByDesc("updated_time");
        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .map(resourceNodePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> findIdMapByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashMap<>();
        }
        
        QueryWrapper<ResourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("code", codes);
        queryWrapper.select("code", "id");
        
        List<ResourceNodePO> poList = resourceNodeMapper.selectList(queryWrapper);
        return poList.stream()
                .collect(Collectors.toMap(ResourceNodePO::getCode, ResourceNodePO::getId));
    }
}