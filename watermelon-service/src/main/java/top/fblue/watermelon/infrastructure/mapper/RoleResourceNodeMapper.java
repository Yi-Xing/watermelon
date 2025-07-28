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
    @Select("SELECT resource_id FROM role_resource WHERE role_id = #{roleId} AND is_deleted = 0")
    List<Long> selectResourceIdsByRoleId(Long roleId);
    
    /**
     * 根据资源ID查询角色ID列表
     */
    @Select("SELECT role_id FROM role_resource WHERE resource_id = #{resourceId} AND is_deleted = 0")
    List<Long> selectRoleIdsByResourceId(Long resourceId);
    
    /**
     * 根据角色ID列表查询资源ID列表
     */
    @Select("<script>" +
            "SELECT DISTINCT resource_id FROM role_resource " +
            "WHERE role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            " AND is_deleted = 0" +
            "</script>")
    List<Long> selectResourceIdsByRoleIds(List<Long> roleIds);
} 