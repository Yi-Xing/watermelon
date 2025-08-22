package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateResourceRelationDTO;
import top.fblue.watermelon.application.dto.UpdateResourceRelationDTO;
import top.fblue.watermelon.application.vo.ResourceRelationVO;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 资源关联转换器
 */
@Component
public class ResourceRelationConverter {

    /**
     * CreateResourceRelationDTO转换为ResourceRelation
     */
    public ResourceRelation toResourceRelation(CreateResourceRelationDTO dto) {
        if (dto == null) {
            return null;
        }

        return ResourceRelation.builder()
                .parentId(dto.getParentId())
                .childId(dto.getChildId())
                .orderNum(dto.getOrderNum())
                .build();
    }

    /**
     * UpdateResourceRelationDTO转换为ResourceRelation
     */
    public ResourceRelation toResourceRelation(UpdateResourceRelationDTO dto) {
        if (dto == null) {
            return null;
        }

        return ResourceRelation.builder()
                .id(dto.getId())
                .parentId(dto.getParentId())
                .childId(dto.getChildId())
                .orderNum(dto.getOrderNum())
                .build();
    }

    /**
     * ResourceRelation转换为ResourceRelationVO（包含资源详细信息）
     */
    public ResourceRelationVO toVO(ResourceRelation resourceRelation, Map<Long, ResourceNode> resourceMap) {
        if (resourceRelation == null) {
            return null;
        }

        ResourceNode parentResource = resourceRelation.getParentId() != null ? 
                resourceMap.get(resourceRelation.getParentId()) : null;
        ResourceNode childResource = resourceMap.get(resourceRelation.getChildId());

        return ResourceRelationVO.builder()
                .id(resourceRelation.getId())
                .parentId(resourceRelation.getParentId())
                .parentName(parentResource != null ? parentResource.getName() : null)
                .parentCode(parentResource != null ? parentResource.getCode() : null)
                .childId(resourceRelation.getChildId())
                .childName(childResource != null ? childResource.getName() : null)
                .childCode(childResource != null ? childResource.getCode() : null)
                .orderNum(resourceRelation.getOrderNum())
                .build();
    }

    /**
     * ResourceRelation转换为ResourceRelationVO
     */
    public ResourceRelationVO toVO(ResourceRelation resourceRelation) {
        if (resourceRelation == null) {
            return null;
        }

        return ResourceRelationVO.builder()
                .id(resourceRelation.getId())
                .parentId(resourceRelation.getParentId())
                .childId(resourceRelation.getChildId())
                .orderNum(resourceRelation.getOrderNum())
                .build();
    }
    
    /**
     * 从单个ResourceRelation中收集相关的资源ID
     * @param resourceRelation 资源关联
     * @return 资源ID列表
     */
    public List<Long> collectResourceIds(ResourceRelation resourceRelation) {
        if (resourceRelation == null) {
            return List.of();
        }
        
        List<Long> ids = new ArrayList<>();
        if (resourceRelation.getParentId() != null) {
            ids.add(resourceRelation.getParentId());
        }
        ids.add(resourceRelation.getChildId());
        return ids;
    }
}
