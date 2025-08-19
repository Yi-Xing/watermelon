package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.fblue.watermelon.application.dto.CreateResourceDTO;
import top.fblue.watermelon.application.dto.ResourceQueryDTO;
import top.fblue.watermelon.application.dto.UpdateResourceDTO;
import top.fblue.watermelon.application.service.ResourceApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeTreeVO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.application.vo.ResourceImportResultVO;
import top.fblue.watermelon.common.response.ApiResponse;

import java.util.List;

/**
 * 资源管理
 */
@RestController
@RequestMapping("/api/resource")
@Validated
public class ResourceController {

    @Resource
    private ResourceApplicationService resourceApplicationService;

    /**
     * 新增资源
     *
     * @param createResourceDTO 创建资源DTO
     * @return 资源基本信息
     */
    @PostMapping
    public ApiResponse<ResourceNodeVO> createResource(@Valid @RequestBody CreateResourceDTO createResourceDTO) {
        ResourceNodeVO resourceNodeVO = resourceApplicationService.createResource(createResourceDTO);
        return ApiResponse.success(resourceNodeVO, "资源创建成功");
    }

    /**
     * 查询资源树
     */
    @GetMapping("/tree")
    public ApiResponse<List<ResourceNodeTreeVO>> getResourceTree(@Valid ResourceQueryDTO queryDTO) {
        List<ResourceNodeTreeVO> tree = resourceApplicationService.getResourceTree(queryDTO);
        return ApiResponse.success(tree, "获取资源树成功");
    }

    /**
     * 根据ID获取资源详情（包含父资源名称）
     */
    @GetMapping
    public ApiResponse<ResourceNodeVO> getResourceById(@RequestParam Long id) {
        ResourceNodeVO resource = resourceApplicationService.getResourceDetailById(id);
        return ApiResponse.success(resource, "获取资源详情成功");
    }

    /**
     * 更新资源
     */
    @PutMapping
    public ApiResponse<Boolean> updateResource(@Valid @RequestBody UpdateResourceDTO updateResourceDTO) {
        boolean result = resourceApplicationService.updateResource(updateResourceDTO);
        if (result) {
            return ApiResponse.success(true, "资源更新成功");
        } else {
            return ApiResponse.error("资源更新失败", false);
        }
    }

    /**
     * 删除资源
     */
    @DeleteMapping
    public ApiResponse<Boolean> deleteResource(@RequestParam Long id) {
        boolean result = resourceApplicationService.deleteResource(id);
        if (result) {
            return ApiResponse.success(true, "资源删除成功");
        } else {
            return ApiResponse.error("资源删除失败", false);
        }
    }

    /**
     * 导出Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] excelBytes = resourceApplicationService.exportExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "resources.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * 导入Excel
     */
    @PostMapping("/import")
    public ApiResponse<ResourceImportResultVO> importExcel(@RequestParam("file") MultipartFile file) {
        ResourceImportResultVO result = resourceApplicationService.importExcel(file);
        return ApiResponse.success(result, "导入Excel完成");
    }
} 