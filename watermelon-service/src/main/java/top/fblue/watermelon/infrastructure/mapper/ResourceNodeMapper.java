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
    
    /**
     * 根据角色ID查询资源ID列表
     */
    @Select("SELECT resource_node_id FROM role_resource_node WHERE role_id = #{roleId} AND is_deleted = 0")
    List<Long> selectResourceIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID删除资源关系（逻辑删除）
     */
    @Delete("UPDATE role_resource_node SET is_deleted = 1 WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 插入角色资源关系
     */
    @Insert("INSERT INTO role_resource_node (role_id, resource_node_id, created_by, created_time, updated_by, updated_time) VALUES (#{roleId}, #{resourceId}, #{createdBy}, NOW(), #{updatedBy}, NOW())")
    int insertRoleResource(@Param("roleId") Long roleId, @Param("resourceId") Long resourceId, @Param("createdBy") Long createdBy, @Param("updatedBy") Long updatedBy);
} 