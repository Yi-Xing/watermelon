package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            // 1. 业务校验
            List<String> businessErrors = validateBusinessRules(resourceNodes);
            if (!businessErrors.isEmpty()) {
                return ResourceImportResultVO.error(businessErrors);
            }
            
            // 2. 分离新增和更新操作
            List<ResourceNode> toInsert = new ArrayList<>();
            List<ResourceNode> toUpdate = new ArrayList<>();
            
            for (ResourceNode resourceNode : resourceNodes) {
                // 检查资源是否已存在
                ResourceNode existingResource = resourceDomainService.findByCode(resourceNode.getCode());
                
                if (existingResource != null) {
                    // 更新现有资源
                    resourceNode.setId(existingResource.getId());
                    toUpdate.add(resourceNode);
                } else {
                    // 创建新资源
                    toInsert.add(resourceNode);
                }
            }
            
            // 3. 批量新增
            if (!toInsert.isEmpty()) {
                for (ResourceNode resourceNode : toInsert) {
                    resourceDomainService.createResourceNode(resourceNode);
                    insertedRows++;
                }
            }
            
            // 4. 批量更新
            if (!toUpdate.isEmpty()) {
                for (ResourceNode resourceNode : toUpdate) {
                    resourceDomainService.updateResource(resourceNode);
                    updatedRows++;
                }
            }
            
            return new ResourceImportResultVO(totalRows, insertedRows, updatedRows, deletedRows);
            
        } catch (Exception e) {
            log.error("批量导入资源失败", e);
            return ResourceImportResultVO.error(List.of("批量导入失败: " + e.getMessage()));
        }
    }
    
    /**
     * 业务规则校验
     */
    private List<String> validateBusinessRules(List<ResourceNode> resourceNodes) {
        List<String> errors = new ArrayList<>();
        
        // 1. 校验名称和code在Excel内部是否重复
        Map<String, Integer> nameCountMap = new HashMap<>();
        Map<String, Integer> codeCountMap = new HashMap<>();
        
        for (int i = 0; i < resourceNodes.size(); i++) {
            ResourceNode resource = resourceNodes.get(i);
            int rowNumber = i + 2; // Excel行号从2开始
            
            // 统计名称出现次数
            String name = resource.getName();
            if (name != null) {
                nameCountMap.put(name, nameCountMap.getOrDefault(name, 0) + 1);
                if (nameCountMap.get(name) > 1) {
                    errors.add(String.format("第%d行: 资源名称'%s'在Excel中重复", rowNumber, name));
                }
            }
            
            // 统计code出现次数
            String code = resource.getCode();
            if (code != null) {
                codeCountMap.put(code, codeCountMap.getOrDefault(code, 0) + 1);
                if (codeCountMap.get(code) > 1) {
                    errors.add(String.format("第%d行: 资源code'%s'在Excel中重复", rowNumber, code));
                }
            }
        }
        
        // 2. 校验与数据库中的资源是否冲突
        for (int i = 0; i < resourceNodes.size(); i++) {
            ResourceNode resource = resourceNodes.get(i);
            int rowNumber = i + 2;
            
            // 检查名称在同级下是否重复
            if (resource.getName() != null && resource.getParentId() != null) {
                boolean nameExists = resourceDomainService.existsByNameAndParentIdExcludeId(
                    resource.getName(), resource.getParentId(), resource.getId());
                if (nameExists) {
                    errors.add(String.format("第%d行: 同级下已存在名称为'%s'的资源", rowNumber, resource.getName()));
                }
            }
            
            // 检查code是否重复（排除自己）
            if (resource.getCode() != null) {
                boolean codeExists = resourceDomainService.existsByCodeExcludeId(resource.getCode(), resource.getId());
                if (codeExists) {
                    errors.add(String.format("第%d行: 资源code'%s'已存在", rowNumber, resource.getCode()));
                }
            }
        }
        
        return errors;
    }
    
    @Override
    public Map<String, Long> getResourceIdMapByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new HashMap<>();
        }
        
        return resourceDomainService.getResourceIdMapByCodes(codes);
    }

    /**
     * 批量转换Excel数据为ResourceNode
     */
    private List<ResourceNode> convertExcelToResourceNodes(List<ResourceExcelVO> excelDataList) {
        // 获取所有父资源code，用于批量查询
        List<String> parentCodes = excelDataList.stream()
                .map(ResourceExcelVO::getParentCode)
                .filter(code -> code != null && !code.trim().isEmpty() && !"0".equals(code))
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询父资源ID映射
        Map<String, Long> parentCodeToIdMap = getResourceIdMapByCodes(parentCodes);
        
        return excelDataList.stream()
                .map(excelData -> convertExcelToResourceNode(excelData, parentCodeToIdMap))
                .collect(Collectors.toList());
    }
    
    /**
     * 转换Excel数据为ResourceNode
     */
    private ResourceNode convertExcelToResourceNode(ResourceExcelVO excelData, Map<String, Long> parentCodeToIdMap) {
        // 根据code查找父资源ID
        Long parentId = null;
        if (excelData.getParentCode() != null && !excelData.getParentCode().trim().isEmpty() 
                && !"0".equals(excelData.getParentCode())) {
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
} 