package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.CreateResourceDTO;
import top.fblue.watermelon.application.vo.*;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.utils.DateTimeUtil;

import java.util.*;
import java.util.stream.Collectors;

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
     * ResourceNode转换为ResourceNodeTreeVO
     */
    public ResourceNodeTreeVO toTreeVO(ResourceNode resource) {
        if (resource == null) {
            return null;
        }

        return ResourceNodeTreeVO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .typeDesc(ResourceTypeEnum.getDescByCode(resource.getType()))
                .code(resource.getCode())
                .orderNum(null) // 显示顺序将由资源关联关系设置
                .state(resource.getState())
                .stateDesc(StateEnum.fromCode(resource.getState()).getDesc())
                .remark(resource.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(resource.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(resource.getUpdatedTime()))
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
     * 构建基于ResourceRelation的资源树形结构
     */
    public List<ResourceNodeTreeVO> buildResourceTree(List<ResourceNode> resources, 
                                                     List<ResourceRelation> relations) {
        // 1. 转换为VO并构建映射
        Map<Long, ResourceNodeTreeVO> resourceMap = resources.stream()
                .collect(Collectors.toMap(
                    ResourceNode::getId,
                        this::toTreeVO
                ));

        // 2. 构建关联关系映射（包含父子关系和显示顺序）
        Map<Long, Integer> relationOrderMap = relations.stream()
                .collect(Collectors.toMap(
                    ResourceRelation::getChildId,
                    ResourceRelation::getOrderNum,
                    (existing, replacement) -> existing // 如果有重复key，保留第一个
                ));

        // 3. 构建父子关系映射（按显示顺序排序）
        Map<Long, List<ResourceRelation>> parentChildMap = relations.stream()
                .collect(Collectors.groupingBy(
                    ResourceRelation::getParentId,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> list.stream()
                                .sorted(Comparator.comparing(ResourceRelation::getOrderNum))
                                .collect(Collectors.toList())
                    )
                ));

        // 4. 构建树形结构并设置显示顺序
        Set<Long> childIds = relations.stream()
                .map(ResourceRelation::getChildId)
                .collect(Collectors.toSet());

        List<ResourceNodeTreeVO> rootNodes = new ArrayList<>();
        
        for (ResourceNodeTreeVO vo : resourceMap.values()) {
            // 为子节点设置资源关联关系中的显示顺序
            Integer relationOrder = relationOrderMap.get(vo.getId());
            if (relationOrder != null) {
                vo.setOrderNum(relationOrder);
            }
            
            if (!childIds.contains(vo.getId())) {
                // 这是根节点
                rootNodes.add(vo);
            }
            
            // 构建子节点
            List<ResourceRelation> childRelations = parentChildMap.get(vo.getId());
            if (childRelations != null) {
                List<ResourceNodeTreeVO> children = childRelations.stream()
                        .map(relation -> {
                            ResourceNodeTreeVO child = resourceMap.get(relation.getChildId());
                            if (child != null) {
                                // 使用关联关系中的显示顺序
                                child.setOrderNum(relation.getOrderNum());
                            }
                            return child;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                vo.setChildren(children);
            }
        }

        // 5. 根节点排序：如果有orderNum则按orderNum升序，否则按更新时间倒序
        rootNodes.sort((a, b) -> a.getOrderNum().compareTo(b.getOrderNum()));

        return rootNodes;
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