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
        Map<Long, ResourceRelation> rootNodeMap = relations.stream()
                .filter(relation -> relation.getParentId() == 0)
                .collect(Collectors.toMap(
                        ResourceRelation::getChildId, resourceRelation -> resourceRelation
                ));

        List<ResourceNodeTreeVO> rootNodes = new ArrayList<>();

        for (ResourceNodeTreeVO vo : resourceMap.values()) {
            // 存储根节点
            if (rootNodeMap.containsKey(vo.getResourceId())) {
                ResourceRelation relation = rootNodeMap.get(vo.getResourceId());
                // 使用关联关系中的显示顺序
                vo.setOrderNum(relation.getOrderNum());
                vo.setId(relation.getId());
                rootNodes.add(vo);
            }

            // 构建子节点
            List<ResourceRelation> childRelations = parentChildMap.get(vo.getResourceId());
            if (childRelations != null) {
                List<ResourceNodeTreeVO> children = childRelations.stream()
                        .map(relation -> {
                            ResourceNodeTreeVO child = resourceMap.get(relation.getChildId());
                            if (child != null) {
                                // 使用关联关系中的显示顺序
                                child.setOrderNum(relation.getOrderNum());
                                child.setId(relation.getId());
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

        // 5. 填充 resourcePath
        for (ResourceNodeTreeVO rootNode : rootNodes) {
            fillResourcePath(rootNode, "");
        }
        
        return rootNodes;
    }


    /**
     * ResourceNode转换为ResourceNodeTreeVO
     */
    private ResourceNodeTreeVO toTreeVO(ResourceNode resource) {
        if (resource == null) {
            return null;
        }

        return ResourceNodeTreeVO.builder()
                .resourceId(resource.getId())
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

    /**
     * 递归填充资源路径
     * @param node 当前节点
     * @param parentPath 父级路径
     */
    private void fillResourcePath(ResourceNodeTreeVO node, String parentPath) {
        if (node == null) {
            return;
        }

        // 构建当前节点的路径
        String currentPath = parentPath + "/" + node.getResourceId();
        node.setResourcePath(currentPath);

        // 递归处理子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (ResourceNodeTreeVO child : node.getChildren()) {
                fillResourcePath(child, currentPath);
            }
        }
    }
}
