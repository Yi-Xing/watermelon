package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.infrastructure.converter.RolePOConverter;
import top.fblue.watermelon.infrastructure.mapper.RoleMapper;
import top.fblue.watermelon.infrastructure.po.RolePO;

import java.util.List;

/**
 * 角色仓储实现
 * 注意：需要先创建对应的RoleRepository接口在domain层
 */
@Repository
public class RoleRepositoryImpl {

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RolePOConverter rolePOConverter;

    /**
     * 保存角色
     * 
     * 注意：需要先创建Role domain实体和RoleRepository接口
     * public Role save(Role role) {
     *     RolePO po = rolePOConverter.toPO(role);
     *     if (po.getId() == null) {
     *         roleMapper.insert(po);
     *     } else {
     *         roleMapper.updateById(po);
     *     }
     *     RolePO savedPO = roleMapper.selectById(po.getId());
     *     return rolePOConverter.toDomain(savedPO);
     * }
     */

    /**
     * 根据ID查找角色
     * 
     * 注意：需要先创建Role domain实体和RoleRepository接口
     * public Role findById(Long id) {
     *     RolePO po = roleMapper.selectById(id);
     *     return rolePOConverter.toDomain(po);
     * }
     */

    /**
     * 检查角色名是否存在
     */
    public boolean existsByName(String name) {
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return roleMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 删除角色
     */
    public boolean delete(Long id) {
        return roleMapper.deleteById(id) > 0;
    }

    /**
     * 根据条件分页查询角色列表
     */
    public List<RolePO> findByCondition(String keyword, Integer state, int pageNum, int pageSize) {
        QueryWrapper<RolePO> queryWrapper = buildQueryWrapper(keyword, state);
        queryWrapper.orderByAsc("order_num").orderByDesc("updated_time");
        
        Page<RolePO> page = new Page<>(pageNum, pageSize);
        IPage<RolePO> pageResult = roleMapper.selectPage(page, queryWrapper);
        
        return pageResult.getRecords();
    }

    /**
     * 根据条件统计角色总数
     */
    public Long countByCondition(String keyword, Integer state) {
        QueryWrapper<RolePO> queryWrapper = buildQueryWrapper(keyword, state);
        return roleMapper.selectCount(queryWrapper);
    }

    /**
     * 查询所有启用的角色
     */
    public List<RolePO> findAllEnabled() {
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", 1);
        queryWrapper.orderByAsc("order_num");
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<RolePO> buildQueryWrapper(String keyword, Integer state) {
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        
        // 关键词模糊查询（角色名）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("name", keyword);
        }
        
        // 状态筛选
        if (state != null) {
            queryWrapper.eq("state", state);
        }
        
        return queryWrapper;
    }
} 