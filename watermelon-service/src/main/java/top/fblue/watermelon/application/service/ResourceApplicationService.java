package top.fblue.watermelon.application.service;

import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;
import top.fblue.watermelon.common.response.Page;

import java.util.List;

/**
 * 资源应用服务接口
 */
public interface ResourceApplicationService {

    /**
     * 创建资源
     */
    ResourceNodeVO createResource(CreateResourceDTO createResourceDTO);
    
    /**
     * 分页查询资源列表
     */
    Page<ResourceNodeVO> getResourceList(ResourceQueryDTO queryDTO);
    
    /**
     * 根据ID获取资源详情
     */
    ResourceNodeVO getResourceDetailById(Long id);

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