package top.fblue.watermelon.application.service;

import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;

import java.util.List;

/**
 * 资源应用服务接口
 */
public interface ResourceApplicationService {

    /**
     * 根据ID获取资源详情（包含父资源名称）
     */
    ResourceNodeVO getResourceDetailById(Long id);
    
    /**
     * 创建资源
     *
     * @param createResourceDTO 创建资源DTO
     * @return 资源视图对象
     */
    ResourceNodeVO createResource(CreateResourceDTO createResourceDTO);
    
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
    
    /**
     * 导出Excel
     */
    byte[] exportExcel();
    
    /**
     * 导入Excel
     */
    ResourceImportResultVO importExcel(MultipartFile file);
}