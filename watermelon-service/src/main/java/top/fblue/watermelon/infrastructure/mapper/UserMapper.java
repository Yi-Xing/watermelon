package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
     * 查询 SSO 令牌交换所需的最小用户投影。
     *
     * @param userId 用户 ID
     * @return 仅包含 ID 和名称的用户数据；用户不存在或已删除时返回 {@code null}
     */
    @Select("SELECT id, name FROM `user` WHERE id = #{userId} AND is_deleted = 0")
    UserPO selectSsoUserById(@Param("userId") Long userId);
}
