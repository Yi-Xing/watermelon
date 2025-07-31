package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.fblue.watermelon.infrastructure.po.UserRolePO;

import java.util.List;

/**
 * 用户角色关系Mapper接口
 * 只返回部分字段，建议用 @Select。MyBatis-plus提供的方法不好用
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRolePO> {
    
    /**
     * 根据用户ID查询角色ID列表
     */
    @Select("SELECT role_id FROM user_role WHERE user_id = #{userId} AND is_deleted = 0")
    List<Long> selectRoleIdsByUserId(Long userId);
}