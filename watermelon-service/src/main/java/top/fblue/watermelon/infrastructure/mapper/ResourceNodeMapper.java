package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;


/**
 * 资源Mapper接口
 */
@Mapper
public interface ResourceNodeMapper extends BaseMapper<ResourceNodePO> {
}