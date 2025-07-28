package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.infrastructure.converter.ResourcePOConverter;
import top.fblue.watermelon.infrastructure.mapper.ResourceMapper;
import top.fblue.watermelon.infrastructure.po.ResourcePO;

import java.util.List;

/**
 * 资源仓储实现
 * 注意：需要先创建对应的ResourceRepository接口在domain层
 */
@Repository
public class ResourceRepositoryImpl {

    @Resource
    private ResourceMapper resourceMapper;
    @Resource
    private ResourcePOConverter resourcePOConverter;

    /**
     * 保存资源
     * 
     * 注意：需要先创建Resource domain实体和ResourceRepository接口
     * public Resource save(Resource resource) {
     *     ResourcePO po = resourcePOConverter.toPO(resource);
     *     if (po.getId() == null) {
     *         resourceMapper.insert(po);
     *     } else {
     *         resourceMapper.updateById(po);
     *     }
     *     ResourcePO savedPO = resourceMapper.selectById(po.getId());
     *     return resourcePOConverter.toDomain(savedPO);
     * }
     */

    /**
     * 根据ID查找资源
     * 
     * 注意：需要先创建Resource domain实体和ResourceRepository接口
     * public Resource findById(Long id) {
     *     ResourcePO po = resourceMapper.selectById(id);
     *     return resourcePOConverter.toDomain(po);
     * }
     */

    /**
     * 检查资源代码是否存在
     */
    public boolean existsByCode(String code) {
        QueryWrapper<ResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        return resourceMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 删除资源
     */
    public boolean delete(Long id) {
        return resourceMapper.deleteById(id) > 0;
    }

    /**
     * 根据条件分页查询资源列表
     */
    public List<ResourcePO> findByCondition(String keyword, Integer type, Integer state, Long parentId, int pageNum, int pageSize) {
        QueryWrapper<ResourcePO> queryWrapper = buildQueryWrapper(keyword, type, state, parentId);
        queryWrapper.orderByAsc("order_num").orderByDesc("updated_time");
        
        Page<ResourcePO> page = new Page<>(pageNum, pageSize);
        IPage<ResourcePO> pageResult = resourceMapper.selectPage(page, queryWrapper);
        
        return pageResult.getRecords();
    }

    /**
     * 根据条件统计资源总数
     */
    public Long countByCondition(String keyword, Integer type, Integer state, Long parentId) {
        QueryWrapper<ResourcePO> queryWrapper = buildQueryWrapper(keyword, type, state, parentId);
        return resourceMapper.selectCount(queryWrapper);
    }

    /**
     * 根据父级ID查询子资源
     */
    public List<ResourcePO> findByParentId(Long parentId) {
        QueryWrapper<ResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId);
        queryWrapper.eq("state", 1); // 只查询启用的资源
        queryWrapper.orderByAsc("order_num");
        return resourceMapper.selectList(queryWrapper);
    }

    /**
     * 根据类型查询资源
     */
    public List<ResourcePO> findByType(Integer type) {
        QueryWrapper<ResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type);
        queryWrapper.eq("state", 1); // 只查询启用的资源
        queryWrapper.orderByAsc("order_num");
        return resourceMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有启用的资源（树形结构）
     */
    public List<ResourcePO> findAllEnabled() {
        QueryWrapper<ResourcePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", 1);
        queryWrapper.orderByAsc("parent_id").orderByAsc("order_num");
        return resourceMapper.selectList(queryWrapper);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<ResourcePO> buildQueryWrapper(String keyword, Integer type, Integer state, Long parentId) {
        QueryWrapper<ResourcePO> queryWrapper = new QueryWrapper<>();
        
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