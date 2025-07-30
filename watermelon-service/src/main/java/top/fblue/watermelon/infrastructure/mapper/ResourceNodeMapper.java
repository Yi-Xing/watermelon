package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;

/**
 * 资源Mapper接口
 */
@Mapper
public interface ResourceNodeMapper extends BaseMapper<ResourceNodePO> {
}