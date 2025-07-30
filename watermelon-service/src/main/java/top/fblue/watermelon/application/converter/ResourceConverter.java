package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.ResourceNodeImportDTO;
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
import top.fblue.watermelon.application.vo.ResourceExcelVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Comparator;

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

    public ResourceNodeVO toVO(ResourceNode resourceNode, ResourceNode parentResourceNode, Map<Long, User> userMap) {
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
                .parentName(parentResourceNode != null ? parentResourceNode.getName() : "")
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
     * 转换为ResourceNode
     */
    public ResourceNode toResourceNode(ResourceNodeImportDTO dto,Long parentId) {
        return ResourceNode.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .type(dto.getType())
                .orderNum(dto.getOrderNum())
                .state(dto.getState())
                .remark(dto.getRemark())
                .parentId(parentId)
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
                .createdBy(toUserInfoVO(userMap.get(resource.getCreatedBy())))
                .createdTime(DateTimeUtil.formatDateTime(resource.getCreatedTime()))
                .updatedBy(toUserInfoVO(userMap.get(resource.getUpdatedBy())))
                .updatedTime(DateTimeUtil.formatDateTime(resource.getUpdatedTime()))
                .build();
    }

    /**
     * User转换为UserInfoVO
     */
    private UserInfoVO toUserInfoVO(User user) {
        if (user == null) {
            return null;
        }

        return UserInfoVO.builder()
                .id(user.getId())
                .name(user.getUsername())
                .build();
    }

    /**
     * 构建资源树形结构
     */
    public List<ResourceNodeTreeVO> buildResourceTree(List<ResourceNode> resources, Map<Long, User> userMap) {
        // 1. 转换为VO
        List<ResourceNodeTreeVO> resourceVOs = resources.stream()
                .map(resource -> toTreeVO(resource, userMap))
                .toList();

        // 2. 构建父子关系
        Map<Long, ResourceNodeTreeVO> resourceMap = resourceVOs.stream()
                .collect(Collectors.toMap(ResourceNodeTreeVO::getId, vo -> vo));

        List<ResourceNodeTreeVO> rootNodes = new ArrayList<>();

        for (ResourceNodeTreeVO vo : resourceVOs) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                rootNodes.add(vo);
            } else {
                ResourceNodeTreeVO parent = resourceMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                }
            }
        }

        // 3. 排序
        sortResourceTree(rootNodes);

        return rootNodes;
    }

    /**
     * 递归排序资源树
     * 优先显示顺序，其次更新时间
     */
    private void sortResourceTree(List<ResourceNodeTreeVO> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        // 排序当前层级
        nodes.sort((a, b) -> {
            int orderCompare = b.getOrderNum().compareTo(a.getOrderNum());
            if (orderCompare != 0) {
                return orderCompare;
            }
            return b.getUpdatedTime().compareTo(a.getUpdatedTime());
        });

        // 递归排序子节点
        for (ResourceNodeTreeVO node : nodes) {
            sortResourceTree(node.getChildren());
        }
    }

    /**
     * 构建层级Excel数据
     */
    public List<ResourceExcelVO> buildHierarchicalExcelData(List<ResourceNode> resources) {
        List<ResourceExcelVO> excelData = new ArrayList<>();
        
        // 构建资源ID到资源的映射
        Map<Long, ResourceNode> resourceMap = resources.stream()
                .collect(Collectors.toMap(ResourceNode::getId, resource -> resource));
        
        // 构建父子关系映射
        Map<Long, List<ResourceNode>> parentChildMap = resources.stream()
                .collect(Collectors.groupingBy(resource -> 
                    Objects.requireNonNullElse(resource.getParentId(), 0L)));
        
        // 获取所有根节点（parentId为null或0的资源）
        List<ResourceNode> rootNodes = parentChildMap.getOrDefault(0L, new ArrayList<>());
        
        // 按orderNum排序根节点
        rootNodes.sort(Comparator.comparing(ResourceNode::getOrderNum).reversed());
        
        // 递归处理每个根节点及其子节点
        for (ResourceNode rootNode : rootNodes) {
            processNodeForExcel(rootNode, resourceMap, parentChildMap, excelData);
        }
        
        return excelData;
    }
    
    /**
     * 递归处理节点，生成Excel数据
     */
    private void processNodeForExcel(ResourceNode node, 
                                   Map<Long, ResourceNode> resourceMap,
                                   Map<Long, List<ResourceNode>> parentChildMap,
                                   List<ResourceExcelVO> excelData) {
        // 获取父节点信息
        String parentCode = "";
        if (node.getParentId() != null && node.getParentId() != 0) {
            ResourceNode parentNode = resourceMap.get(node.getParentId());
            if (parentNode != null) {
                parentCode = parentNode.getCode();
            }
        }
        
        // 转换为Excel VO并添加到结果列表
        ResourceExcelVO excelVO = convertToExcelVO(node, parentCode);
        excelData.add(excelVO);
        
        // 获取子节点并按orderNum排序
        List<ResourceNode> children = parentChildMap.getOrDefault(node.getId(), new ArrayList<>());
        children.sort(Comparator.comparing(ResourceNode::getOrderNum).reversed());
        
        // 递归处理子节点
        for (ResourceNode child : children) {
            processNodeForExcel(child, resourceMap, parentChildMap, excelData);
        }
    }
    
    /**
     * 转换资源为Excel VO
     */
    public ResourceExcelVO convertToExcelVO(ResourceNode resource, String parentCode) {
        return ResourceExcelVO.builder()
                .parentCode(parentCode)
                .name(resource.getName())
                .code(resource.getCode())
                .type(ResourceTypeEnum.getDescByCode(resource.getType()))
                .orderNum(resource.getOrderNum())
                .state(StateEnum.getDescByCode(resource.getState()))
                .remark(resource.getRemark())
                .build();
    }

    /**
     * 从ResourceExcelVO转换为ResourceNodeImportVO
     */
    public ResourceNodeImportDTO toImportDTO(ResourceExcelVO excelVO) {
        return ResourceNodeImportDTO.builder()
                .parentCode(excelVO.getParentCode())
                .name(excelVO.getName())
                .code(excelVO.getCode())
                .type(ResourceTypeEnum.fromDesc(excelVO.getType()).getCode())
                .orderNum(excelVO.getOrderNum())
                .state(StateEnum.fromDesc(excelVO.getState()).getCode())
                .remark(excelVO.getRemark())
                .build();
    }
}