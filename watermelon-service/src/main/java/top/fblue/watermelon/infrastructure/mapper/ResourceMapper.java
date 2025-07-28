package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.fblue.watermelon.infrastructure.po.ResourcePO;

/**
 * 资源Mapper接口
 */
@Mapper
public interface ResourceMapper extends BaseMapper<ResourcePO> {
    // 使用MyBatis Plus的BaseMapper提供的方法
} 