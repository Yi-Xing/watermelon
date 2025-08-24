package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.CreateResourceDTO;
import top.fblue.watermelon.application.vo.*;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.DateTimeUtil;

import java.util.*;

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
                .state(resourceNode.getState())
                .stateDesc(StateEnum.fromCode(resourceNode.getState()).getDesc())
                .remark(resourceNode.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(resourceNode.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(resourceNode.getUpdatedTime()))
                .build();
    }

    public ResourceNodeVO toVO(ResourceNode resourceNode, Map<Long, User> userMap) {
        if (resourceNode == null) {
            return null;
        }

        return ResourceNodeVO.builder()
                .id(resourceNode.getId())
                .name(resourceNode.getName())
                .type(resourceNode.getType())
                .typeDesc(ResourceTypeEnum.getDescByCode(resourceNode.getType()))
                .code(resourceNode.getCode())
                .state(resourceNode.getState())
                .stateDesc(StateEnum.fromCode(resourceNode.getState()).getDesc())
                .remark(resourceNode.getRemark())
                .createdBy(toUserInfoVO(userMap.get(resourceNode.getCreatedBy())))
                .createdTime(DateTimeUtil.formatDateTime(resourceNode.getCreatedTime()))
                .updatedBy(toUserInfoVO(userMap.get(resourceNode.getUpdatedBy())))
                .updatedTime(DateTimeUtil.formatDateTime(resourceNode.getUpdatedTime()))
                .build();
    }

    /**
     * CreateResourceNodeDTO转换为ResourceNode
     */
    public ResourceNode toResourceNode(CreateResourceDTO dto) {
        if (dto == null) {
            return null;
        }

        return ResourceNode.builder()
                .name(dto.getName())
                .type(dto.getType())
                .code(dto.getCode())
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }

    /**
     * 转换为ResourceNode
     */
    public ResourceNode toResourceNode(ResourceImportDTO dto) {
        return ResourceNode.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .type(dto.getType())
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
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }

    /**
     * User转换为UserBaseVO
     */
    private UserBaseVO toUserInfoVO(User user) {
        if (user == null) {
            return null;
        }

        return UserBaseVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }

    /**
     * 转换资源为Excel VO
     */
    public ResourceExcelVO convertToExcelVO(ResourceNode resource) {
        return ResourceExcelVO.builder()
                .name(resource.getName())
                .code(resource.getCode())
                .type(ResourceTypeEnum.getDescByCode(resource.getType()))
                .state(StateEnum.getDescByCode(resource.getState()))
                .remark(resource.getRemark())
                .build();
    }

    /**
     * 从ResourceExcelVO转换为ResourceImportDTO
     */
    public ResourceImportDTO toImportDTO(ResourceExcelVO excelVO) {
        return ResourceImportDTO.builder()
                .name(excelVO.getName())
                .code(excelVO.getCode())
                .type(ResourceTypeEnum.fromDesc(excelVO.getType()).getCode())
                .state(StateEnum.fromDesc(excelVO.getState()).getCode())
                .remark(excelVO.getRemark())
                .build();
    }
}