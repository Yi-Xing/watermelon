package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.*;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceRelationVO;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.utils.DateTimeUtil;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;

import java.util.*;
import java.util.stream.Collectors;

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

        ResourceNode parentResource = resourceMap.get(resourceRelation.getParentId());
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
     *
     * @param resourceRelation 资源关联
     * @return 资源ID列表
     */
    public List<Long> collectResourceIds(ResourceRelation resourceRelation) {
        if (resourceRelation == null) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        if (resourceRelation.getParentId() != 0) {
            ids.add(resourceRelation.getParentId());
        }
        ids.add(resourceRelation.getChildId());
        return ids;
    }


    /**
     * 转换导入数据为资源关联关系列表
     */
    public List<ResourceRelationImportDTO> toResourceRelationList(List<ResourceRelationExcelDTO> importData, Map<String, Long> codeToIdMap) {
        List<ResourceRelationImportDTO> relations = new ArrayList<>();

        // 构建层级关系
        // k：深度，v:深度对应的资源ID
        Map<Integer, Long> parentDepthMap = new HashMap<>();
        // 初始化顶级
        parentDepthMap.put(0, 0L);

        for (ResourceRelationExcelDTO dto : importData) {
            // 获取父级ID
            Long parentId = parentDepthMap.get(dto.getResourcePath().size() - 1);

            String resourceInfo = dto.getResourcePath().getLast();
            String code = resourceInfo.substring(resourceInfo.indexOf('/') + 1);
            Long childId = codeToIdMap.get(code);

            ResourceRelationImportDTO relation = ResourceRelationImportDTO.builder()
                    .parentId(parentId)
                    .childId(childId)
                    .orderNum(dto.getOrderNum())
                    .build();
            relations.add(relation);
            // 用于下级获取父级ID
            parentDepthMap.put(dto.getResourcePath().size(), childId);
        }

        return relations;
    }
    /**
     * 构建基于ResourceRelation的资源树形结构
     */
    public List<ResourceNodeTreeVO> buildResourceTree(List<ResourceNode> resources,
                                                      List<ResourceRelation> relations) {
        // 1. 转换为VO并构建映射
        Map<Long, ResourceNode> resourceMap = resources.stream()
                .collect(Collectors.toMap(
                        ResourceNode::getId, resource -> resource
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

        // 4. 获取根节点（parentId = 0）
        List<ResourceRelation> rootRelations = relations.stream()
                .filter(relation -> relation.getParentId() == 0)
                .sorted(Comparator.comparing(ResourceRelation::getOrderNum).reversed())
                .toList();

        // 5. 构建树形结构并填充 resourcePath
        List<ResourceNodeTreeVO> rootNodes = new ArrayList<>();
        
        for (ResourceRelation rootRelation : rootRelations) {
            ResourceNode rootResource = resourceMap.get(rootRelation.getChildId());
            if (rootResource != null) {
                ResourceNodeTreeVO rootNode = toTreeVO(rootResource, rootRelation.getId(), rootRelation.getOrderNum());
                buildChildrenWithPath(rootNode, rootRelation.getChildId(), parentChildMap, resourceMap, "");
                rootNodes.add(rootNode);
            }
        }

        return rootNodes;
    }

    /**
     * 递归构建子节点并填充 resourcePath
     */
    private void buildChildrenWithPath(ResourceNodeTreeVO parentNode, Long parentId, 
                                      Map<Long, List<ResourceRelation>> parentChildMap,
                                      Map<Long, ResourceNode> resourceMap,
                                      String parentPath) {
        
        // 构建当前节点的路径
        String currentPath = parentPath + "/" + parentNode.getResourceId();
        parentNode.setResourcePath(currentPath);
        
        List<ResourceRelation> childRelations = parentChildMap.get(parentId);
        if (childRelations == null || childRelations.isEmpty()) {
            return;
        }

        // 构建当前节点的子节点
        List<ResourceNodeTreeVO> children = new ArrayList<>();
        for (ResourceRelation childRelation : childRelations) {
            ResourceNode childResource = resourceMap.get(childRelation.getChildId());
            if (childResource != null) {
                ResourceNodeTreeVO childNode = toTreeVO(childResource, childRelation.getId(), childRelation.getOrderNum());
                // 递归构建子节点的子节点，传递当前路径
                buildChildrenWithPath(childNode, childRelation.getChildId(), parentChildMap, resourceMap, currentPath);
                children.add(childNode);
            }
        }
        
        parentNode.setChildren(children);
    }


    /**
     * ResourceNode转换为ResourceNodeTreeVO
     */
    private ResourceNodeTreeVO toTreeVO(ResourceNode resource, Long id, Integer orderNum) {
        if (resource == null) {
            return null;
        }

        return ResourceNodeTreeVO.builder()
                .id(id)
                .resourceId(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .typeDesc(ResourceTypeEnum.getDescByCode(resource.getType()))
                .code(resource.getCode())
                .orderNum(orderNum)
                .state(resource.getState())
                .stateDesc(StateEnum.fromCode(resource.getState()).getDesc())
                .remark(resource.getRemark())
                .createdTime(DateTimeUtil.formatDateTime(resource.getCreatedTime()))
                .updatedTime(DateTimeUtil.formatDateTime(resource.getUpdatedTime()))
                .build();
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
                .depth(level)
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
     * 转换为ResourceNode
     */
    public ResourceRelation toResourceRelation(ResourceRelationImportDTO dto) {
        return ResourceRelation.builder()
                .parentId(dto.getParentId())
                .childId(dto.getChildId())
                .orderNum(dto.getOrderNum())
                .build();
    }
}
