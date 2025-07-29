package top.fblue.watermelon.application.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.ResourceExcelDTO;
import top.fblue.watermelon.application.service.ResourceApplicationService;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源Excel导入监听器
 */
@Slf4j
@Component
public class ResourceExcelListener implements ReadListener<ResourceExcelDTO> {
    
    private final List<ResourceExcelDTO> dataList = new ArrayList<>();
    private final ResourceApplicationService resourceApplicationService;
    
    public ResourceExcelListener(ResourceApplicationService resourceApplicationService) {
        this.resourceApplicationService = resourceApplicationService;
    }
    
    @Override
    public void invoke(ResourceExcelDTO data, AnalysisContext context) {
        log.info("解析到一条数据: {}", data);
        dataList.add(data);
    }
    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完成，共{}条", dataList.size());
        // 处理导入逻辑
        processImport();
    }
    
    /**
     * 处理导入逻辑
     */
    private void processImport() {
        for (ResourceExcelDTO excelData : dataList) {
            try {
                // 转换Excel数据为ResourceNode
                ResourceNode resourceNode = convertToResourceNode(excelData);
                
                // 调用应用服务处理导入
                resourceApplicationService.importResource(resourceNode);
                
                log.info("成功导入资源: {}", excelData.getCode());
            } catch (Exception e) {
                log.error("导入资源失败: {}, 错误: {}", excelData.getCode(), e.getMessage());
            }
        }
    }
    
    /**
     * 转换Excel数据为ResourceNode
     */
    private ResourceNode convertToResourceNode(ResourceExcelDTO excelData) {
        // 根据code查找父资源ID
        Long parentId = null;
        if (excelData.getParentCode() != null && !excelData.getParentCode().trim().isEmpty()) {
            parentId = resourceApplicationService.getResourceIdByCode(excelData.getParentCode());
        }
        
        // 转换类型
        Integer type = convertType(excelData.getType());
        
        // 转换状态
        Integer state = convertState(excelData.getState());
        
        return ResourceNode.builder()
                .name(excelData.getName())
                .code(excelData.getCode())
                .type(type)
                .orderNum(excelData.getOrderNum())
                .state(state)
                .remark(excelData.getRemark())
                .parentId(parentId)
                .build();
    }
    
    /**
     * 转换资源类型
     */
    private Integer convertType(String typeStr) {
        if (typeStr == null) {
            return ResourceTypeEnum.PAGE.getCode();
        }
        
        switch (typeStr.trim()) {
            case "页面":
                return ResourceTypeEnum.PAGE.getCode();
            case "按钮":
                return ResourceTypeEnum.BUTTON.getCode();
            case "接口":
                return ResourceTypeEnum.API.getCode();
            default:
                return ResourceTypeEnum.PAGE.getCode();
        }
    }
    
    /**
     * 转换状态
     */
    private Integer convertState(String stateStr) {
        if (stateStr == null) {
            return StateEnum.ENABLE.getCode();
        }
        
        switch (stateStr.trim()) {
            case "启用":
                return StateEnum.ENABLE.getCode();
            case "禁用":
                return StateEnum.DISABLE.getCode();
            default:
                return StateEnum.ENABLE.getCode();
        }
    }
} 