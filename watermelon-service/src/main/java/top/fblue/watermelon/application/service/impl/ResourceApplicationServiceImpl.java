package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.ResourceConverter;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceImportDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.vo.*;
import top.fblue.watermelon.application.service.ResourceApplicationService;
import top.fblue.watermelon.application.service.ResourceExcelService;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;


import java.util.*;
import java.util.stream.Collectors;

import top.fblue.watermelon.common.response.Page;

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
    public ResourceNodeVO createResource(CreateResourceDTO createResourceDTO) {
        // 1. 转换DTO为Domain实体
        ResourceNode resourceNode = resourceConverter.toResourceNode(createResourceDTO);

        // 2. 通过领域服务创建资源
        ResourceNode createdResourceNode = resourceDomainService.createResourceNode(resourceNode);

        // 3. 转换为VO并返回
        return resourceConverter.toVO(createdResourceNode);
    }

    @Override
    public Page<ResourceNodeVO> getResourceList(ResourceQueryDTO queryDTO) {
        // 1. 分页查询资源列表
        List<ResourceNode> resources = resourceDomainService.getResourceList(queryDTO.getName(), queryDTO.getCode(), queryDTO.getState(), queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 统计总数
        Long total = resourceDomainService.countResources(queryDTO.getName(), queryDTO.getCode(), queryDTO.getState());

        // 3. 转换为VO
        List<ResourceNodeVO> resourceVOs = resources.stream().map(resourceConverter::toVO).collect(Collectors.toList());

        // 4. 构建分页响应
        return new Page<>(resourceVOs, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public ResourceNodeVO getResourceDetailById(Long id) {
        // 1. 获取资源基本信息
        ResourceNode resource = resourceDomainService.getResourceById(id);

        // 2. 获取关联的用户信息
        List<Long> userIds = List.of(resource.getCreatedBy(), resource.getUpdatedBy());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(userIds);

        // 3. 转换为VO
        return resourceConverter.toVO(resource, userMap);
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
                return ResourceImportResultVO.builder().success(false).errors(validationErrors).build();
            }

            // 3. 转换为导入DTO
            List<ResourceImportDTO> importDTOs = excelDataList.stream().map(resourceConverter::toImportDTO).collect(Collectors.toList());

            // 4. 批量导入（带事务）
            return resourceExcelService.batchImportResources(importDTOs);
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            return ResourceImportResultVO.builder().success(false).errors(List.of("导入Excel失败: " + e.getMessage())).build();
        }
    }

    @Override
    public byte[] exportExcel() {
        // 1. 获取所有资源
        List<ResourceNode> resources = resourceDomainService.getResourceList(null, null, null);

        // 2. 构建Excel数据
        List<ResourceExcelVO> excelData = resources.stream().map(resourceConverter::convertToExcelVO).collect(Collectors.toList());

        // 3. 生成Excel文件
        return resourceExcelService.writeExcel(excelData, "资源列表", ResourceExcelVO.class);
    }
} 