package top.fblue.watermelon.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.domain.role.entity.Role;
import top.fblue.watermelon.domain.role.repository.RoleRepository;
import top.fblue.watermelon.infrastructure.converter.RolePOConverter;
import top.fblue.watermelon.infrastructure.mapper.RoleMapper;
import top.fblue.watermelon.infrastructure.po.RolePO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色仓储实现
 * 注意：需要先创建对应的RoleRepository接口在domain层
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RolePOConverter rolePOConverter;
    
    @Override
    public Role save(Role role) {
        RolePO po = rolePOConverter.toPO(role);
        roleMapper.insert(po);
        RolePO savedPO = roleMapper.selectById(po.getId());
        return rolePOConverter.toDomain(savedPO);
    }

    @Override
    public Role findById(Long id) {
        RolePO po = roleMapper.selectById(id);
        return rolePOConverter.toDomain(po);
    }

    @Override
    public List<Role> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<RolePO> poList = roleMapper.selectList(queryWrapper);
        return poList.stream()
                .map(rolePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean update(Role role) {
        RolePO po = rolePOConverter.toPO(role);
        return roleMapper.updateById(po) > 0;
    }

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

    @Override
    public List<Role> findByCondition(String keyword, Integer state, int pageNum, int pageSize) {
        QueryWrapper<RolePO> queryWrapper = buildQueryWrapper(keyword, state);
        queryWrapper.orderByDesc("order_num").orderByDesc("updated_time");
        
        Page<RolePO> page = new Page<>(pageNum, pageSize);
        IPage<RolePO> pageResult = roleMapper.selectPage(page, queryWrapper);
        
        return pageResult.getRecords().stream()
                .map(rolePOConverter::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * 根据条件统计角色总数
     */
    public Long countByCondition(String keyword, Integer state) {
        QueryWrapper<RolePO> queryWrapper = buildQueryWrapper(keyword, state);
        return roleMapper.selectCount(queryWrapper);
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