package top.fblue.watermelon.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.service.ResourceNodeApplicationService;
import top.fblue.watermelon.application.vo.ResourceNodeVO;
import top.fblue.watermelon.common.response.ApiResponse;

/**
 * 资源控制器
 */
@RestController
@RequestMapping("/api/resource")
@Validated
public class ResourceNodeController {

    @Resource
    private ResourceNodeApplicationService resourceNodeApplicationService;

    /**
     * 新增资源
     *
     * @param createResourceNodeDTO 创建资源DTO
     * @return 资源基本信息
     */
    @PostMapping
    public ApiResponse<ResourceNodeVO> createResource(@Valid @RequestBody CreateResourceNodeDTO createResourceNodeDTO) {
        ResourceNodeVO resourceNodeVO = resourceNodeApplicationService.createResource(createResourceNodeDTO);
        return ApiResponse.success(resourceNodeVO, "资源创建成功");
    }
}
