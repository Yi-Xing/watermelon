package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.application.dto.ResourceTreeExcelDTO;
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

        // 2. 构建父子关系映射（按显示顺序排序）
        Map<Long, List<ResourceRelation>> parentChildMap = relations.stream()
                .collect(Collectors.groupingBy(
                    ResourceRelation::getParentId,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> list.stream()
                                .sorted(Comparator.comparing(ResourceRelation::getOrderNum).reversed())
                                .collect(Collectors.toList())
                    )
                ));

        // 3. 构建树形结构并设置显示顺序
        // 获取所有根节点的资源 ID，以及其对应的 资源关联关系
        Map<Long,ResourceRelation> rootNodeMap = relations.stream()
                .filter(relation -> relation.getParentId() == 0)
                .collect(Collectors.toMap(
                        ResourceRelation::getChildId,resourceRelation -> resourceRelation
                ));

        List<ResourceNodeTreeVO> rootNodes = new ArrayList<>();
        
        for (ResourceNodeTreeVO vo : resourceMap.values()) {
            // 存储根节点
            if (rootNodeMap.containsKey(vo.getId())) {
                ResourceRelation relation = rootNodeMap.get(vo.getId());
                // 使用关联关系中的显示顺序
                vo.setOrderNum(relation.getOrderNum());
                vo.setResourceRelationId(relation.getId());
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
                                child.setResourceRelationId(relation.getId());
                            }
                            return child;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                vo.setChildren(children);
            }
        }

        // 4. 根节点排序：按orderNum降序
        rootNodes.sort(Comparator.comparing(ResourceNodeTreeVO::getOrderNum).reversed());

        return rootNodes;
    }

    /**
     * 构建基于ResourceRelation的资源树形结构
     */
    public List<ResourceTreeExcelDTO> buildResourceTreeExcelData(List<ResourceNode> resources,
                                                                 List<ResourceRelation> relations) {
        // 将资源和资源关系，转为树结构
        List<ResourceNodeTreeVO> resourceTree = buildResourceTree(resources, relations);

        // 将资源树转换为Excel数据
        List<ResourceTreeExcelDTO> excelData = new ArrayList<>();
        for (ResourceNodeTreeVO rootNode : resourceTree) {
            // 处理根节点
            processNodeForExcel(rootNode, excelData, 0);
        }

        return excelData;
    }


    /**
     * 递归处理节点，生成Excel数据
     */
    private void processNodeForExcel(ResourceNodeTreeVO node, List<ResourceTreeExcelDTO> excelData, int level) {
        if (node == null) {
            return;
        }

        String resourceInfo = node.getName() + "/" + node.getCode();

        // 创建当前节点的Excel行
        ResourceTreeExcelDTO currentRow = ResourceTreeExcelDTO.builder()
                .orderNum(node.getOrderNum())
                .state(node.getStateDesc()) // 添加状态信息
                .column(level)
                .resourceInfo(resourceInfo)
                .build();

        // 添加到Excel数据中
        excelData.add(currentRow);

        // 处理子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (ResourceNodeTreeVO child : node.getChildren()) {
                processNodeForExcel(child, excelData, level + 1);
            }
        }
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