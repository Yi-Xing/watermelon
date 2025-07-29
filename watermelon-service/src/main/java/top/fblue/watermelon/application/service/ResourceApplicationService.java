package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;

import java.util.List;

/**
 * 资源应用服务接口
 */
public interface ResourceApplicationService {

    /**
     * 根据ID获取资源详情（包含父资源名称）
     */
    ResourceNodeTreeVO getResourceDetailById(Long id);
    
    /**
     * 创建资源
     *
     * @param createResourceNodeDTO 创建资源DTO
     * @return 资源视图对象
     */
    ResourceNodeVO createResource(CreateResourceNodeDTO createResourceNodeDTO);
    
    /**
     * 查询资源树
     */
    List<ResourceNodeTreeVO> getResourceTree(ResourceQueryDTO queryDTO);

    /**
     * 更新资源
     */
    boolean updateResource(UpdateResourceDTO updateResourceDTO);
    
    /**
     * 删除资源
     */
    boolean deleteResource(Long id);
}