package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.fblue.watermelon.application.converter.ResourceConverter;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.service.ResourceApplicationService;
import top.fblue.watermelon.application.service.ResourceExcelService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;

/**
 * 资源应用服务实现
 */
@Slf4j
@Service
public class ResourceApplicationServiceImpl implements ResourceApplicationService {

    @Resource
    private ResourceDomainService resourceDomainService;
    
    @Resource
    private UserDomainService userDomainService;
    
    @Resource
    private ResourceConverter resourceConverter;
    
    @Resource
    private ResourceExcelService resourceExcelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceNodeVO createResource(CreateResourceNodeDTO createResourceNodeDTO) {
        // 1. 转换DTO为Domain实体
        ResourceNode resourceNode = resourceConverter.toResourceNode(createResourceNodeDTO);

        // 2. 通过领域服务创建资源（包含所有业务校验）
        ResourceNode createdResourceNode = resourceDomainService.createResourceNode(resourceNode);

        // 3. 转换为VO并返回
        return resourceConverter.toVO(createdResourceNode);
    }

    @Override
    public ResourceNodeVO getResourceDetailById(Long id) {
        // 1. 获取资源基本信息
        ResourceNode resource = resourceDomainService.getResourceById(id);

        // 2. 获取父节点信息
        ResourceNode parebtResourceNode = null;
        if (resource.getParentId() != null && resource.getParentId() != 0) {
            parebtResourceNode = resourceDomainService.getResourceById(resource.getParentId());
        }
        
        // 3. 获取关联的用户信息
        List<Long> userIds = List.of(resource.getCreatedBy(), resource.getUpdatedBy());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(userIds);
        
        // 4. 转换为VO
        return resourceConverter.toVO(resource, parebtResourceNode, userMap);
    }

    @Override
    public List<ResourceNodeTreeVO> getResourceTree(ResourceQueryDTO queryDTO) {
        // 1. 查询所有资源
        List<ResourceNode> resources = resourceDomainService.getResourceList(
                queryDTO.getName(),
                queryDTO.getCode(),
                queryDTO.getState()
        );
        
        // 2. 如果有搜索条件，需要包含父节点
        if (StringUtils.hasText(queryDTO.getName()) || StringUtils.hasText(queryDTO.getCode()) || queryDTO.getState() != null) {
            resources = resourceDomainService.buildFullPathNodes(resources);
        }
        
        // 3. 获取所有关联的用户信息
        Set<Long> userIds = resources.stream()
                .flatMap(resource -> Stream.of(resource.getCreatedBy(), resource.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userDomainService.getUserMapByIds(new ArrayList<>(userIds));
        
        // 4. 转换为树形结构
        return resourceConverter.buildResourceTree(resources, userMap);
    }

    @Override
    @Transactional
    public boolean updateResource(UpdateResourceDTO updateResourceDTO) {
        // 1. 转换DTO为Domain实体
        ResourceNode resource = resourceConverter.toResourceNode(updateResourceDTO);
        
        // 2. 通过领域服务更新资源
        return resourceDomainService.updateResource(resource);
    }

    @Override
    @Transactional
    public boolean deleteResource(Long id) {
        // 通过领域服务删除资源
        return resourceDomainService.deleteResource(id);
    }

    @Override
    public ResourceImportResultVO importExcel(MultipartFile file) {
        try {
            // 1. 读取Excel数据
            List<ResourceExcelVO> excelDataList = resourceExcelService.readExcel(file);
            
            // 2. 数据校验
            List<String> validationErrors = resourceExcelService.validateExcelData(excelDataList);
            if (!validationErrors.isEmpty()) {
                return ResourceImportResultVO.builder()
                        .success(false)
                        .errors(validationErrors)
                        .build();
            }

            // 3. 批量转换数据
            List<ResourceNode> resourceNodes = convertExcelToResourceNodes(excelDataList);

            // 4. 批量导入
            return batchImportResources(resourceNodes);
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            return ResourceImportResultVO.builder()
                    .success(false)
                    .errors(List.of("导入Excel失败: " + e.getMessage()))
                    .build();
        }
    }
    
    @Override
    public byte[] exportExcel() {
        // 1. 获取所有资源
        List<ResourceNode> resources = resourceDomainService.getResourceList(null, null, null);
        
        // 2. 构建层级Excel数据
        List<ResourceExcelVO> excelData = resourceConverter.buildHierarchicalExcelData(resources);
        
        // 3. 生成Excel文件
        return resourceExcelService.writeExcel(excelData);
    }
    
    @Override
    public ResourceImportResultVO batchImportResources(List<ResourceNode> resourceNodes) {
        int totalRows = resourceNodes.size();
        int insertedRows = 0;
        int updatedRows = 0;
        int deletedRows = 0;
        
        try {
            // 1. 获取Excel中的所有code
            Set<String> excelCodes = resourceNodes.stream()
                    .map(ResourceNode::getCode)
                    .collect(Collectors.toSet());
            
            // 2. 获取数据库中所有资源
            List<ResourceNode> existingResources = resourceDomainService.getResourceList(null, null, null);
            Map<String, ResourceNode> existingCodeToResource = existingResources.stream()
                    .collect(Collectors.toMap(ResourceNode::getCode, resource -> resource));
            
            // 3. 分类处理
            List<ResourceNode> toInsert = new ArrayList<>();
            List<ResourceNode> toUpdate = new ArrayList<>();
            List<String> toDelete = new ArrayList<>();
            
            // 处理Excel中的资源
            for (ResourceNode resourceNode : resourceNodes) {
                ResourceNode existingResource = existingCodeToResource.get(resourceNode.getCode());
                
                if (existingResource != null) {
                    // 数据库存在，Excel存在 -> 更新
                    resourceNode.setId(existingResource.getId());
                    toUpdate.add(resourceNode);
                } else {
                    // 数据库不存在，Excel存在 -> 新增
                    resourceNode.setId(null);
                    toInsert.add(resourceNode);
                }
            }
            
            // 处理需要删除的资源（数据库存在，Excel不存在）
            for (String existingCode : existingCodeToResource.keySet()) {
                if (!excelCodes.contains(existingCode)) {
                    toDelete.add(existingCode);
                }
            }
            
            // 4. 按拓扑排序处理新增资源
            Map<String, Long> codeToIdMap = new HashMap<>();
            if (!toInsert.isEmpty()) {
                // 按拓扑排序处理，确保父节点在子节点之前插入
                for (ResourceNode resourceNode : toInsert) {
                    // 如果父节点是临时ID，需要更新为真实ID
                    if (resourceNode.getParentId() != null && resourceNode.getParentId() < 0) {
                        String parentCode = findParentCodeByTempId(resourceNode.getParentId(), toInsert, codeToIdMap);
                        if (parentCode != null) {
                            Long realParentId = codeToIdMap.get(parentCode);
                            resourceNode.setParentId(realParentId);
                        }
                    }
                    
                    // 插入资源
                    ResourceNode insertedResource = resourceDomainService.createResourceNode(resourceNode);
                    insertedRows++;
                    
                    // 记录code到真实ID的映射
                    codeToIdMap.put(resourceNode.getCode(), insertedResource.getId());
                }
            }
            
            // 5. 批量更新
            if (!toUpdate.isEmpty()) {
                for (ResourceNode resourceNode : toUpdate) {
                    resourceDomainService.updateResource(resourceNode);
                    updatedRows++;
                }
            }
            
            // 6. 批量删除
            if (!toDelete.isEmpty()) {
                for (String code : toDelete) {
                    ResourceNode existingResource = existingCodeToResource.get(code);
                    if (existingResource != null) {
                        resourceDomainService.deleteResource(existingResource.getId());
                        deletedRows++;
                    }
                }
            }
            
            return ResourceImportResultVO.builder()
                    .success(true)
                    .totalRows(totalRows)
                    .insertedRows(insertedRows)
                    .updatedRows(updatedRows)
                    .deletedRows(deletedRows)
                    .build();
        } catch (Exception e) {
            log.error("批量导入资源失败", e);
            return ResourceImportResultVO.builder()
                    .success(false)
                    .errors(List.of("批量导入失败: " + e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 批量转换Excel数据为ResourceNode
     * 使用拓扑排序确保父节点在子节点之前处理
     */
    private List<ResourceNode> convertExcelToResourceNodes(List<ResourceExcelVO> excelDataList) {
        // 1. 构建code到Excel数据的映射
        Map<String, ResourceExcelVO> codeToExcelMap = excelDataList.stream()
                .collect(Collectors.toMap(ResourceExcelVO::getCode, data -> data));
        
        // 2. 获取所有父资源code（排除空值）
        List<String> parentCodes = excelDataList.stream()
                .map(ResourceExcelVO::getParentCode)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量查询数据库中已存在的父资源ID映射
        Map<String, Long> parentCodeToIdMap = resourceDomainService.getResourceIdMapByCodes(parentCodes);
        
        // 4. 拓扑排序，确保父节点在子节点之前
        List<ResourceExcelVO> sortedDataList = topologicalSort(excelDataList, codeToExcelMap);
        
        // 5. 按排序顺序转换数据
        List<ResourceNode> allResourceNodes = new ArrayList<>();
        Map<String, Long> completeCodeToIdMap = new HashMap<>(parentCodeToIdMap);
        
        for (ResourceExcelVO excelData : sortedDataList) {
            ResourceNode resourceNode = convertExcelToResourceNode(excelData, completeCodeToIdMap);
            allResourceNodes.add(resourceNode);
            
            // 记录code到ID的映射（使用负数作为临时ID）
            Long tempId = (long) -(allResourceNodes.size());
            completeCodeToIdMap.put(excelData.getCode(), tempId);
        }
        
        return allResourceNodes;
    }
    
    /**
     * 转换Excel数据为ResourceNode
     */
    private ResourceNode convertExcelToResourceNode(ResourceExcelVO excelData, Map<String, Long> parentCodeToIdMap) {
        // 根据code查找父资源ID
        Long parentId = null;
        if (excelData.getParentCode() != null && !excelData.getParentCode().trim().isEmpty() 
                && !excelData.getParentCode().isEmpty()) {
            parentId = parentCodeToIdMap.get(excelData.getParentCode());
        }
        
        // 使用枚举转换类型
        Integer type = convertType(excelData.getType());
        
        // 使用枚举转换状态
        Integer state = convertState(excelData.getState());
        
        return ResourceNode.builder()
                .name(excelData.getName())
                .code(excelData.getCode())
                .type(type)
                .orderNum(excelData.getOrderNum())
                .state(state)
                .remark(excelData.getRemark())
                .parentId(parentId)
                .build();
    }
    
    /**
     * 使用枚举转换资源类型
     */
    private Integer convertType(String typeStr) {
        if (typeStr == null) {
            return ResourceTypeEnum.PAGE.getCode();
        }
        
        return switch (typeStr.trim()) {
            case "页面" -> ResourceTypeEnum.PAGE.getCode();
            case "按钮" -> ResourceTypeEnum.BUTTON.getCode();
            case "接口" -> ResourceTypeEnum.API.getCode();
            default -> ResourceTypeEnum.PAGE.getCode();
        };
    }
    
    /**
     * 使用枚举转换状态
     */
    private Integer convertState(String stateStr) {
        if (stateStr == null) {
            return StateEnum.ENABLE.getCode();
        }
        
        return switch (stateStr.trim()) {
            case "启用" -> StateEnum.ENABLE.getCode();
            case "禁用" -> StateEnum.DISABLE.getCode();
            default -> StateEnum.ENABLE.getCode();
        };
    }
    
    /**
     * 拓扑排序，确保父节点在子节点之前处理
     */
    private List<ResourceExcelVO> topologicalSort(List<ResourceExcelVO> dataList, Map<String, ResourceExcelVO> codeToExcelMap) {
        // 构建邻接表
        Map<String, List<String>> adjacencyList = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        
        // 初始化
        for (ResourceExcelVO data : dataList) {
            adjacencyList.put(data.getCode(), new ArrayList<>());
            inDegree.put(data.getCode(), 0);
        }
        
        // 构建依赖关系
        for (ResourceExcelVO data : dataList) {
            if (data.getParentCode() != null && !data.getParentCode().isEmpty() 
                    && codeToExcelMap.containsKey(data.getParentCode())) {
                // 如果父节点也在Excel中，建立依赖关系
                adjacencyList.get(data.getParentCode()).add(data.getCode());
                inDegree.put(data.getCode(), inDegree.get(data.getCode()) + 1);
            }
        }
        
        // 拓扑排序
        List<ResourceExcelVO> result = new ArrayList<>();
        Queue<String> queue = new LinkedList<>();
        
        // 将所有入度为0的节点加入队列
        for (String code : inDegree.keySet()) {
            if (inDegree.get(code) == 0) {
                queue.offer(code);
            }
        }
        
        // 处理队列中的节点
        while (!queue.isEmpty()) {
            String currentCode = queue.poll();
            ResourceExcelVO currentData = codeToExcelMap.get(currentCode);
            result.add(currentData);
            
            // 减少所有邻居的入度
            for (String neighbor : adjacencyList.get(currentCode)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }
        
        // 如果结果数量不等于输入数量，说明存在环
        if (result.size() != dataList.size()) {
            throw new RuntimeException("资源树中存在循环引用");
        }
        
        return result;
    }
    
    /**
     * 根据临时ID查找父节点的code
     * 临时ID是负数，用于在内存中标识节点
     */
    private String findParentCodeByTempId(Long tempId, List<ResourceNode> resourceNodes, Map<String, Long> codeToIdMap) {
        // 临时ID是负数，我们需要找到对应的资源
        for (ResourceNode node : resourceNodes) {
            // 如果这个节点的临时ID等于我们要找的tempId
            // 我们需要通过codeToIdMap来反向查找
            if (codeToIdMap.containsKey(node.getCode())) {
                Long mappedId = codeToIdMap.get(node.getCode());
                if (mappedId != null && mappedId.equals(tempId)) {
                    return node.getCode();
                }
            }
        }
        return null;
    }
} 