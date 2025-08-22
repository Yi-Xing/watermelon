package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.dto.CreateResourceRelationDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceRelationDTO;
import top.fblue.watermelon.application.service.ResourceRelationApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceRelationVO;
import top.fblue.watermelon.common.response.ApiResponse;

import java.util.List;

/**
 * 资源关联管理
 */
@RestController
@RequestMapping("/api/admin/resource/relation")
@Validated
public class ResourceRelationController {

    @Resource
    private ResourceRelationApplicationService resourceRelationApplicationService;


    /**
     * 新增资源关联
     */
    @PostMapping
    public ApiResponse<ResourceRelationVO> createResourceRelation(@Valid @RequestBody CreateResourceRelationDTO createResourceRelationDTO) {
        ResourceRelationVO resourceRelationVO = resourceRelationApplicationService.createResourceRelation(createResourceRelationDTO);
        return ApiResponse.success(resourceRelationVO, "资源关联创建成功");
    }

    /**
     * 查询资源树
     */
    @GetMapping("/tree")
    public ApiResponse<List<ResourceNodeTreeVO>> getResourceTree(@Valid ResourceQueryDTO queryDTO) {
        List<ResourceNodeTreeVO> tree = resourceRelationApplicationService.getResourceTree(queryDTO);
        return ApiResponse.success(tree, "获取资源树成功");
    }


    /**
     * 查询资源关联详情
     */
    @GetMapping
    public ApiResponse<ResourceRelationVO> getResourceRelationById(@RequestParam Long id) {
        ResourceRelationVO resourceRelation = resourceRelationApplicationService.getResourceRelationById(id);
        return ApiResponse.success(resourceRelation, "获取资源关联详情成功");
    }

    /**
     * 更新资源关联
     */
    @PutMapping
    public ApiResponse<Boolean> updateResourceRelation(@Valid @RequestBody UpdateResourceRelationDTO updateResourceRelationDTO) {
        boolean result = resourceRelationApplicationService.updateResourceRelation(updateResourceRelationDTO);
        if (result) {
            return ApiResponse.success(true, "资源关联更新成功");
        } else {
            return ApiResponse.error("资源关联更新失败", false);
        }
    }

    /**
     * 删除资源关联
     */
    @DeleteMapping
    public ApiResponse<Boolean> deleteResourceRelation(@RequestParam Long id) {
        boolean result = resourceRelationApplicationService.deleteResourceRelation(id);
        if (result) {
            return ApiResponse.success(true, "资源关联删除成功");
        } else {
            return ApiResponse.error("资源关联删除失败", false);
        }
    }
}
