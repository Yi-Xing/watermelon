package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.ResourceConverter;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.service.ResourceNodeApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.service.ResourceNodeDomainService;
import top.fblue.watermelon.infrastructure.mapper.UserMapper;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;
import top.fblue.watermelon.infrastructure.po.UserPO;


/**
 * 资源应用服务实现
 */
@Service
public class ResourceNodeApplicationServiceImpl implements ResourceNodeApplicationService {

    @Resource
    private ResourceNodeDomainService resourceNodeDomainService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ResourceConverter resourceConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceNodeVO createResource(CreateResourceNodeDTO createResourceNodeDTO) {
        // 1. 转换DTO为Domain实体
        ResourceNode resourceNode = resourceConverter.toResourceNode(createResourceNodeDTO);
        
        // 2. 通过领域服务创建资源（包含所有业务校验）
        ResourceNode createdResourceNode = resourceNodeDomainService.createResourceNode(resourceNode);
        
        // 3. 查询完整的资源信息并返回
        return buildResourceVO(createdResourceNode.getId());
    }
    
    /**
     * 构建ResourceNodeVO
     */
    private ResourceNodeVO buildResourceVO(Long resourceId) {
        // 通过领域服务查询资源节点及其关联信息
        ResourceNode resourceNode = resourceNodeDomainService.findByIdWithAssociations(resourceId);
        if (resourceNode == null) {
            throw new IllegalArgumentException("资源不存在");
        }
        
        // 转换为ResourceNodePO以便使用现有的转换器
        ResourceNodePO resourceNodePO = ResourceNodePO.builder()
                .id(resourceNode.getId())
                .name(resourceNode.getName())
                .type(resourceNode.getType())
                .code(resourceNode.getCode())
                .orderNum(resourceNode.getOrderNum())
                .parentId(resourceNode.getParentId())
                .state(resourceNode.getState())
                .remark(resourceNode.getRemark())
                .createdBy(resourceNode.getCreatedBy())
                .createdTime(resourceNode.getCreatedTime())
                .updatedBy(resourceNode.getUpdatedBy())
                .updatedTime(resourceNode.getUpdatedTime())
                .build();
        
        // 转换用户信息
        UserPO createdByUser = resourceNode.getCreatedByUser() != null ? 
                UserPO.builder()
                        .id(resourceNode.getCreatedByUser().getId())
                        .name(resourceNode.getCreatedByUser().getUsername())
                        .build() : null;
                        
        UserPO updatedByUser = resourceNode.getUpdatedByUser() != null ? 
                UserPO.builder()
                        .id(resourceNode.getUpdatedByUser().getId())
                        .name(resourceNode.getUpdatedByUser().getUsername())
                        .build() : null;
        
        // 获取父节点名称
        String parentName = resourceNode.getParentNode() != null ? resourceNode.getParentNode().getName() : null;
        
        return resourceConverter.toResourceVO(resourceNodePO, createdByUser, updatedByUser, parentName);
    }
} 