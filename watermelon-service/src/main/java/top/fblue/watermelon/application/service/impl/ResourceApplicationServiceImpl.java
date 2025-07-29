package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.ResourceConverter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.ResourceExcelDTO;
import top.fblue.watermelon.application.listener.ResourceExcelListener;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.application.service.ResourceApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;
import top.fblue.watermelon.domain.resource.service.ResourceDomainService;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public ResourceNodeTreeVO getResourceDetailById(Long id) {
        // 1. 获取资源基本信息
        ResourceNode resource = resourceDomainService.getResourceById(id);
        
        // 2. 获取关联的用户信息
        List<Long> userIds = List.of(resource.getCreatedBy(), resource.getUpdatedBy());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(userIds);
        
        // 3. 转换为VO
        return resourceConverter.toTreeVO(resource, userMap);
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

    @Override
    @Transactional
    public boolean deleteResource(Long id) {
        // 通过领域服务删除资源
        return resourceDomainService.deleteResource(id);
    }

    @Override
    public Long getResourceIdByCode(String code) {
        // 通过领域服务根据code获取资源ID
        return resourceDomainService.getResourceIdByCode(code);
    }

    @Override
    @Transactional
    public void importResource(ResourceNode resourceNode) {
        // 通过领域服务导入资源
        resourceDomainService.importResource(resourceNode);
    }

    @Override
    public byte[] exportExcel() throws IOException {
        // 1. 获取所有资源
        List<ResourceNode> resources = resourceDomainService.getResourceList(null, null);
        
        // 2. 转换为Excel VO
        List<ResourceExcelVO> excelData = resources.stream()
                .map(this::convertToExcelVO)
                .collect(Collectors.toList());
        
        // 3. 生成Excel文件
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, ResourceExcelVO.class)
                .sheet("资源列表")
                .doWrite(excelData);
        
        return outputStream.toByteArray();
    }

    @Override
    public String importExcel(MultipartFile file) {
        try {
            // 使用EasyExcel读取文件
            EasyExcel.read(file.getInputStream(), ResourceExcelDTO.class, new ResourceExcelListener(this))
                    .sheet()
                    .doRead();
            
            return "导入成功";
        } catch (Exception e) {
            throw new RuntimeException("导入Excel失败: " + e.getMessage());
        }
    }
    
    /**
     * 转换资源为Excel VO
     */
    private ResourceExcelVO convertToExcelVO(ResourceNode resource) {
        // 获取父资源code
        String parentCode = null;
        if (resource.getParentId() != null) {
            ResourceNode parentResource = resourceDomainService.getResourceById(resource.getParentId());
            if (parentResource != null) {
                parentCode = parentResource.getCode();
            }
        }
        
        ResourceExcelVO excelVO = new ResourceExcelVO();
        excelVO.setParentCode(parentCode);
        excelVO.setName(resource.getName());
        excelVO.setCode(resource.getCode());
        excelVO.setType(ResourceTypeEnum.getDescByCode(resource.getType()));
        excelVO.setOrderNum(resource.getOrderNum());
        excelVO.setState(StateEnum.getDescByCode(resource.getState()));
        excelVO.setRemark(resource.getRemark());
        return excelVO;
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