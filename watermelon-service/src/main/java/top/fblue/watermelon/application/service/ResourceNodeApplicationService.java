package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.CreateResourceNodeDTO;
import top.fblue.watermelon.application.vo.ResourceNodeVO;

/**
 * 资源应用服务接口
 */
public interface ResourceNodeApplicationService {

    /**
     * 创建资源
     *
     * @param createResourceNodeDTO 创建资源DTO
     * @return 资源视图对象
     */
    ResourceNodeVO createResource(CreateResourceNodeDTO createResourceNodeDTO);
}