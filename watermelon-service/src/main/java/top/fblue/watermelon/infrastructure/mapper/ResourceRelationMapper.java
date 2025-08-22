package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.fblue.watermelon.infrastructure.po.ResourceRelationPO;

import java.util.List;

/**
 * 资源关系Mapper接口
 */
@Mapper
public interface ResourceRelationMapper extends BaseMapper<ResourceRelationPO> {
}
