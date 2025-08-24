package top.fblue.watermelon.application.service;

import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceRelationDTO;
import top.fblue.watermelon.application.dto.ResourceTreeQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceRelationDTO;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceRelationVO;
import top.fblue.watermelon.application.vo.ExcelImportResultVO;

import java.util.List;

/**
 * 资源关联应用服务接口
 */
public interface ResourceRelationApplicationService {

    /**
     * 查询资源树
     */
    List<ResourceNodeTreeVO> getResourceTree(ResourceTreeQueryDTO queryDTO);

    /**
     * 新增资源关联
     */
    ResourceRelationVO createResourceRelation(CreateResourceRelationDTO createResourceRelationDTO);
    
    /**
     * 查询资源关联详情
     */
    ResourceRelationVO getResourceRelationById(Long id);
    
    /**
     * 更新资源关联
     */
    boolean updateResourceRelation(UpdateResourceRelationDTO updateResourceRelationDTO);
    
    /**
     * 删除资源关联
     */
    boolean deleteResourceRelation(Long id);
    
    /**
     * 导出资源关系树Excel
     */
    byte[] exportResourceTreeExcel();

    /**
     * 导入资源关联关系Excel
     */
    ExcelImportResultVO importResourceRelationExcel(MultipartFile file);
}
