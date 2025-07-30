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
    @Transactional(rollbackFor = Exception.class)
    public boolean updateResource(UpdateResourceDTO updateResourceDTO) {
        // 1. 转换DTO为Domain实体
        ResourceNode resource = resourceConverter.toResourceNode(updateResourceDTO);
        
        // 2. 通过领域服务更新资源
        return resourceDomainService.updateResource(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

            // 3. 批量导入（带事务）
            return resourceExcelService.batchImportExcelData(excelDataList);
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
} 