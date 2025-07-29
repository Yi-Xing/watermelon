package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.ResourceConverter;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.service.ResourceApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 资源应用服务实现
 */
@Service
public class ResourceApplicationServiceImpl implements ResourceApplicationService {

    @Resource
    private ResourceDomainService resourceDomainService;
    
    @Resource
    private UserDomainService userDomainService;
    
    @Resource
    private ResourceConverter resourceConverter;

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
    public List<ResourceNodeTreeVO> getResourceTree(ResourceQueryDTO queryDTO) {
        // 1. 查询所有资源
        List<ResourceNode> resources = resourceDomainService.getResourceList(
                queryDTO.getName(),
                queryDTO.getState()
        );
        
        // 2. 获取所有关联的用户信息
        Set<Long> userIds = resources.stream()
                .flatMap(resource -> Stream.of(resource.getCreatedBy(), resource.getUpdatedBy()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(new ArrayList<>(userIds));
        
        // 3. 转换为树形结构
        return buildResourceTree(resources, userMap);
    }

    @Override
    @Transactional
    public boolean updateResource(UpdateResourceDTO updateResourceDTO) {
        // 1. 转换DTO为Domain实体
        ResourceNode resource = resourceConverter.toResourceNode(updateResourceDTO);
        
        // 2. 通过领域服务更新资源
        return resourceDomainService.updateResource(resource);
    }
    
    /**
     * 构建资源树形结构
     */
    private List<ResourceNodeTreeVO> buildResourceTree(List<ResourceNode> resources, Map<Long, User> userMap) {
        // 1. 转换为VO
        List<ResourceNodeTreeVO> resourceVOs = resources.stream()
                .map(resource -> resourceConverter.toTreeVO(resource, userMap))
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
            int orderCompare = a.getOrderNum().compareTo(b.getOrderNum());
            if (orderCompare != 0) {
                return orderCompare;
            }
            return b.getUpdatedTime().compareTo(a.getUpdatedTime());
        });
        
        // 递归排序子节点
        for (ResourceNodeTreeVO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortResourceTree(node.getChildren());
            }
        }
    }
} 