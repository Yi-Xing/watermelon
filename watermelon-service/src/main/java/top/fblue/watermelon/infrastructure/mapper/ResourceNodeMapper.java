package top.fblue.watermelon.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;


/**
 * 资源Mapper接口
 */
@Mapper
public interface ResourceNodeMapper extends BaseMapper<ResourceNodePO> {

    /**
     * 查询用户在指定系统中通过启用角色获得的启用资源。
     *
     * @param userId 用户 ID
     * @param systemCode 目标系统编码
     * @return 用户在目标系统中的有效资源持久化对象
     */
    @Select("""
            SELECT DISTINCT rn.*
            FROM resource_node rn
            JOIN role_resource_node rrn
              ON rrn.resource_node_id = rn.id AND rrn.is_deleted = 0
            JOIN `role` r
              ON r.id = rrn.role_id AND r.is_deleted = 0 AND r.state = 1
            JOIN user_role ur
              ON ur.role_id = r.id AND ur.is_deleted = 0
            JOIN `user` u
              ON u.id = ur.user_id AND u.is_deleted = 0 AND u.state = 1
            WHERE u.id = #{userId}
              AND rn.is_deleted = 0
              AND rn.state = 1
              AND rn.type IN (1, 2, 3)
              AND rn.code LIKE CONCAT(#{systemCode}, ':%')
            ORDER BY rn.id
            """)
    List<ResourceNodePO> selectEffectiveResources(@Param("userId") Long userId,
                                                  @Param("systemCode") String systemCode);
}
