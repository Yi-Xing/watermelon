package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.fblue.watermelon.infrastructure.po.RolePO;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<RolePO> {
    // 使用MyBatis Plus的BaseMapper提供的方法
} 