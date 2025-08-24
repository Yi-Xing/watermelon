package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.converter.ResourceRelationConverter;
import top.fblue.watermelon.application.dto.*;
import top.fblue.watermelon.application.service.ResourceRelationApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceRelationVO;
import top.fblue.watermelon.application.vo.ExcelImportResultVO;
import top.fblue.watermelon.common.exception.BusinessException;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceRelationDomainService;
import top.fblue.watermelon.application.service.ResourceExcelService;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 资源关联应用服务实现
 */
@Slf4j
@Service
public class ResourceRelationApplicationServiceImpl implements ResourceRelationApplicationService {

    @Resource
    private ResourceRelationDomainService resourceRelationDomainService;

    @Resource
    private ResourceDomainService resourceDomainService;

    @Resource
    private ResourceRelationConverter resourceRelationConverter;

    @Resource
    private ResourceExcelService resourceExcelService;

    /**
     * 资源关联操作锁
     * 确保新增、更新、删除操作同一时间只能有一个线程执行
     * 前期节约服务器资源没部署redis，后期可以换成 redisson
     */
    private final ReentrantLock resourceRelationLock = new ReentrantLock();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceRelationVO createResourceRelation(CreateResourceRelationDTO createResourceRelationDTO) {
        try {
            if (!resourceRelationLock.tryLock(1, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            // 转换DTO为领域实体
            ResourceRelation resourceRelation = resourceRelationConverter.toResourceRelation(createResourceRelationDTO);

            // 通过领域服务创建关联关系
            ResourceRelation created = resourceRelationDomainService.createResourceRelation(resourceRelation);
            return resourceRelationConverter.toVO(created);
        } catch (InterruptedException e) {
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            resourceRelationLock.unlock();
        }
    }

    @Override
    public List<ResourceNodeTreeVO> getResourceTree(ResourceTreeQueryDTO queryDTO) {
        // 1. 查询符合条件的资源
        List<ResourceNode> allResources = resourceDomainService.getResourceList(
                queryDTO.getName(),
                queryDTO.getCode(),
                queryDTO.getState()
        );

        // 2. 获取所有资源关联关系
        List<ResourceRelation> relations = resourceRelationDomainService.getAllResourceRelations();

        // 3. 如果有搜索条件，需要构建完整的树路径
        if (StringUtils.hasText(queryDTO.getName()) || StringUtils.hasText(queryDTO.getCode()) || queryDTO.getState() != null) {
            // 3.1. 构建完整的资源ID列表
            List<Long> allResourceIds = resourceRelationDomainService.buildCompleteResourceIds(allResources, relations);

            // 3.2 获取需要显示的所有资源
            allResources = resourceDomainService.getResourceListByIds(allResourceIds);
        }

        // 4. 转换为树形结构
        return resourceRelationConverter.buildResourceTree(allResources, relations);
    }

    @Override
    public ResourceRelationVO getResourceRelationById(Long id) {
        // 获取资源关联
        ResourceRelation resourceRelation = resourceRelationDomainService.getResourceRelationById(id);

        // 收集需要查询的资源ID
        List<Long> resourceIds = resourceRelationConverter.collectResourceIds(resourceRelation);

        // 批量查询资源并获取映射
        Map<Long, ResourceNode> resourceMap = resourceDomainService.getResourceMapByIds(resourceIds);

        // 使用转换器转换为VO
        return resourceRelationConverter.toVO(resourceRelation, resourceMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateResourceRelation(UpdateResourceRelationDTO updateResourceRelationDTO) {
        try {
            if (!resourceRelationLock.tryLock(1, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            // 转换DTO为领域实体
            ResourceRelation resourceRelation = resourceRelationConverter.toResourceRelation(updateResourceRelationDTO);

            // 通过领域服务更新关联关系
            return resourceRelationDomainService.updateResourceRelation(resourceRelation);

        } catch (InterruptedException e) {
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            resourceRelationLock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResourceRelation(Long id) {
        try {
            if (!resourceRelationLock.tryLock(1, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            // 通过领域服务删除关联关系
            return resourceRelationDomainService.deleteResourceRelation(id);
        } catch (InterruptedException e) {
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            resourceRelationLock.unlock();
        }
    }

    @Override
    public byte[] exportResourceTreeExcel() {
        // 1. 查询所有资源
        List<ResourceNode> allResources = resourceDomainService.findAll();

        // 2. 获取所有资源关联关系
        List<ResourceRelation> relations = resourceRelationDomainService.getAllResourceRelations();

        // 3. 转换为Excel数据
        List<ResourceTreeExcelDTO> excelData = resourceRelationConverter.buildResourceTreeExcelData(allResources, relations);

        // 4. 根据动态列生成Excel
        return resourceExcelService.writeResourceTreeExcel(excelData);
    }

    @Override
    public ExcelImportResultVO importResourceRelationExcel(MultipartFile file) {
        try {
            if (!resourceRelationLock.tryLock(1, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            // 1. 读取Excel文件
            List<ResourceRelationExcelDTO> importData = resourceExcelService.readResourceRelationExcel(file);

            // 2. 获取全部资源的 code 到 ResourceNodeID 的映射
            Map<String, Long> codeToIdMap = resourceDomainService.getResourceMapByCodes();

            // 3. 校验Excel数据
            List<String> validationErrors = resourceExcelService.validateResourceRelationExcelData(importData, codeToIdMap);
            if (!validationErrors.isEmpty()) {
                return ExcelImportResultVO.builder().success(false).errors(validationErrors).build();
            }

            // 4. 转换为资源关联关系列表
            List<ResourceRelationImportDTO> resourceRelationList = resourceRelationConverter.toResourceRelationList(importData, codeToIdMap);

            // 5. 批量处理资源关联关系（全量替换，带事务）
            return resourceExcelService.batchImportResourceRelations(resourceRelationList);

        } catch (InterruptedException e) {
            throw new BusinessException("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("导入资源关联关系失败", e);
            throw new BusinessException("导入失败: " + e.getMessage());
        } finally {
            resourceRelationLock.unlock();
        }
    }
}
