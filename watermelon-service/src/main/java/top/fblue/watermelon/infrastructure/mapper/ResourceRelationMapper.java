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
    
    /**
     * 检查环形依赖 - 使用递归CTE查询
     */
    @Select("""
        WITH RECURSIVE resource_path AS (
            SELECT parent_id, child_id, 1 as level
            FROM resource_relation 
            WHERE parent_id = #{childId} AND is_deleted = 0
            
            UNION ALL
            
            SELECT rr.parent_id, rr.child_id, rp.level + 1
            FROM resource_relation rr
            INNER JOIN resource_path rp ON rr.parent_id = rp.child_id
            WHERE rp.level < 10 AND rr.is_deleted = 0
        )
        SELECT COUNT(*) > 0 as has_cycle
        FROM resource_path 
        WHERE child_id = #{parentId}
        """)
    boolean hasCyclicDependency(@Param("parentId") Long parentId, @Param("childId") Long childId);
}
