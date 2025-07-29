package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.DateTimeUtil;

import java.util.Map;

/**
 * 资源转换器
 */
@Component
public class ResourceConverter {

    public ResourceNodeVO toVO(ResourceNode resourceNode) {
        if (resourceNode == null) {
            return null;
        }

        return ResourceNodeVO.builder()
                .id(resourceNode.getId())
                .name(resourceNode.getName())
                .type(resourceNode.getType())
                .typeDesc(ResourceTypeEnum.getDescByCode(resourceNode.getType()))
                .code(resourceNode.getCode())
                .orderNum(resourceNode.getOrderNum())
                .parentId(resourceNode.getParentId())
                .state(resourceNode.getState())
                .stateDesc(StateEnum.fromCode(resourceNode.getState()).getDesc())
                .remark(resourceNode.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(resourceNode.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(resourceNode.getUpdatedTime()))
                .build();
    }

    /**
     * CreateResourceNodeDTO转换为ResourceNode
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
     * UpdateResourceDTO转换为ResourceNode
     */
    public ResourceNode toResourceNode(UpdateResourceDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return ResourceNode.builder()
                .id(dto.getId())
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
     * ResourceNode转换为ResourceNodeTreeVO
     */
    public ResourceNodeTreeVO toTreeVO(ResourceNode resource, Map<Long, User> userMap) {
        if (resource == null) {
            return null;
        }
        
        return ResourceNodeTreeVO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .typeDesc(ResourceTypeEnum.getDescByCode(resource.getType()))
                .code(resource.getCode())
                .orderNum(resource.getOrderNum())
                .parentId(resource.getParentId())
                .state(resource.getState())
                .stateDesc(StateEnum.fromCode(resource.getState()).getDesc())
                .remark(resource.getRemark())
                .createdBy(convertToUserInfoVO(userMap.get(resource.getCreatedBy())))
                .createdTime(DateTimeUtil.formatDateTime(resource.getCreatedTime()))
                .updatedBy(convertToUserInfoVO(userMap.get(resource.getUpdatedBy())))
                .updatedTime(DateTimeUtil.formatDateTime(resource.getUpdatedTime()))
                .build();
    }
    
    /**
     * User转换为UserInfoVO
     */
    private UserInfoVO convertToUserInfoVO(User user) {
        if (user == null) {
            return null;
        }
        
        return UserInfoVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }
}