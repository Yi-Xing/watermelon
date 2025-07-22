package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {
    // 可以添加自定义的查询方法
} 