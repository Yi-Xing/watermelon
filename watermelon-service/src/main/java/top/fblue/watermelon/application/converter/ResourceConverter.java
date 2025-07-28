package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.common.utils.DateTimeUtil;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 资源转换器
 */
@Component
public class ResourceConverter {

    /**
     * CreateResourceDTO转换为ResourceNode领域实体
     */
    public ResourceNode toResourceNode(CreateResourceNodeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return ResourceNode.builder()
                .name(dto.getName())
                .type(dto.getType())
                .code(dto.getCode())
                .orderNum(dto.getOrderNum())
                .parentId(dto.getParentId())
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }

    /**
     * CreateResourceDTO转换为ResourcePO
     */
    public ResourceNodePO toResourcePO(CreateResourceNodeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return ResourceNodePO.builder()
                .name(dto.getName())
                .type(dto.getType())
                .code(dto.getCode())
                .orderNum(dto.getOrderNum())
                .parentId(dto.getParentId())
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }
    
    /**
     * ResourcePO转换为ResourceVO
     */
    public ResourceNodeVO toResourceVO(ResourceNodePO po, UserPO createdByUser, UserPO updatedByUser, String parentName) {
        if (po == null) {
            return null;
        }
        
        return ResourceNodeVO.builder()
                .id(po.getId())
                .name(po.getName())
                .type(po.getType())
                .typeDesc(getTypeDesc(po.getType()))
                .code(po.getCode())
                .orderNum(po.getOrderNum())
                .parentId(po.getParentId())
                .parentName(parentName)
                .state(po.getState())
                .stateDesc(getStateDesc(po.getState()))
                .remark(po.getRemark())
                .createdBy(convertToUserInfoVO(createdByUser))
                .createdTime(DateTimeUtil.formatDateTime(po.getCreatedTime()))
                .updatedBy(convertToUserInfoVO(updatedByUser))
                .updatedTime(DateTimeUtil.formatDateTime(po.getUpdatedTime()))
                .build();
    }
    
    /**
     * UserPO转换为UserInfoVO
     */
    private UserInfoVO convertToUserInfoVO(UserPO userPO) {
        if (userPO == null) {
            return null;
        }
        
        return UserInfoVO.builder()
                .id(userPO.getId())
                .name(userPO.getName())
                .build();
    }
    
    /**
     * 获取资源类型描述
     */
    private String getTypeDesc(Integer type) {
        if (type == null) {
            return null;
        }
        
        return switch (type) {
            case 1 -> "页面";
            case 2 -> "按钮";
            case 3 -> "接口";
            default -> "未知";
        };
    }
    
    /**
     * 获取状态描述
     */
    private String getStateDesc(Integer state) {
        if (state == null) {
            return null;
        }
        
        return switch (state) {
            case 1 -> "启用";
            case 2 -> "禁用";
            default -> "未知";
        };
    }
} 