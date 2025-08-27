package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.fblue.watermelon.infrastructure.po.RoleResourceNodePO;

import java.util.List;

/**
 * 角色资源关系Mapper接口
 */
@Mapper
public interface RoleResourceNodeMapper extends BaseMapper<RoleResourceNodePO> {
    
    /**
     * 根据角色ID查询资源ID列表
     */
    @Select("SELECT resource_node_id FROM role_resource_node WHERE role_id = #{roleId} AND is_deleted = 0")
    List<Long> selectResourceIdsByRoleId(Long roleId);
}