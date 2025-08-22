package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.fblue.watermelon.application.converter.ResourceConverter;
import top.fblue.watermelon.application.converter.ResourceRelationConverter;
import top.fblue.watermelon.application.dto.CreateResourceRelationDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceRelationDTO;
import top.fblue.watermelon.application.service.ResourceRelationApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceRelationVO;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.entity.ResourceRelation;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.resource.service.ResourceRelationDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private UserDomainService userDomainService;
    
    @Resource
    private ResourceConverter resourceConverter;
    
    @Resource
    private ResourceRelationConverter resourceRelationConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceRelationVO createResourceRelation(CreateResourceRelationDTO createResourceRelationDTO) {
        // 转换DTO为领域实体
        ResourceRelation resourceRelation = resourceRelationConverter.toResourceRelation(createResourceRelationDTO);

        // 通过领域服务创建关联关系
        ResourceRelation created = resourceRelationDomainService.createResourceRelation(resourceRelation);
        return resourceRelationConverter.toVO(created);
    }

    @Override
    public List<ResourceNodeTreeVO> getResourceTree(ResourceQueryDTO queryDTO) {
        // 1. 查询符合条件的资源
        List<ResourceNode> filteredResources = resourceDomainService.getResourceList(
                queryDTO.getName(),
                queryDTO.getCode(),
                queryDTO.getState()
        );
        
        // 2. 如果有搜索条件，需要构建完整的树路径
        List<Long> allResourceIds;
        if (StringUtils.hasText(queryDTO.getName()) || StringUtils.hasText(queryDTO.getCode()) || queryDTO.getState() != null) {
            // 包含所有父级节点以构建完整树形结构
            List<Long> filteredIds = filteredResources.stream()
                    .map(ResourceNode::getId)
                    .collect(Collectors.toList());
            allResourceIds = resourceRelationDomainService.buildTreeResourceIds(filteredIds);
        } else {
            // 显示所有资源
            allResourceIds = resourceDomainService.getResourceList(null, null, null)
                    .stream()
                    .map(ResourceNode::getId)
                    .collect(Collectors.toList());
        }
        
        // 3. 获取需要显示的所有资源
        List<ResourceNode> allResources = resourceDomainService.getResourceListByIds(allResourceIds);
        
        // 4. 获取所有资源关联关系
        List<ResourceRelation> relations = resourceRelationDomainService.getAllResourceRelations();
        
        // 5. 获取所有关联的用户信息
        Set<Long> userIds = allResources.stream()
                .flatMap(resource -> Stream.of(resource.getCreatedBy(), resource.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userDomainService.getUserMapByIds(new ArrayList<>(userIds));
        
        // 6. 转换为树形结构
        return resourceConverter.buildResourceTree(allResources, relations, userMap);
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
        // 转换DTO为领域实体
        ResourceRelation resourceRelation = resourceRelationConverter.toResourceRelation(updateResourceRelationDTO);
        
        // 通过领域服务更新关联关系
        return resourceRelationDomainService.updateResourceRelation(resourceRelation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResourceRelation(Long id) {
        // 通过领域服务删除关联关系
        return resourceRelationDomainService.deleteResourceRelation(id);
    }
}
