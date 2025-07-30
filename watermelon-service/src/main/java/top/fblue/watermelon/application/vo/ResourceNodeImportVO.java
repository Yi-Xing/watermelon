package top.fblue.watermelon.application.vo;

import lombok.Data;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;

/**
 * 资源节点导入VO
 * 用于Excel导入时的中间对象，使用parentCode来标识关联关系
 */
@Data
public class ResourceNodeImportVO {
    
    /**
     * 上级资源code
     */
    private String parentCode;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源code
     */
    private String code;
    
    /**
     * 资源类型
     */
    private Integer type;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 状态
     */
    private Integer state;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 从ResourceExcelVO转换为ResourceNodeImportVO
     */
    public static ResourceNodeImportVO fromExcelVO(ResourceExcelVO excelVO) {
        ResourceNodeImportVO importVO = new ResourceNodeImportVO();
        importVO.setParentCode(excelVO.getParentCode());
        importVO.setName(excelVO.getName());
        importVO.setCode(excelVO.getCode());
        importVO.setType(convertType(excelVO.getType()));
        importVO.setOrderNum(excelVO.getOrderNum());
        importVO.setState(convertState(excelVO.getState()));
        importVO.setRemark(excelVO.getRemark());
        return importVO;
    }
    
    /**
     * 转换为ResourceNode
     */
    public ResourceNode toResourceNode(Long parentId) {
        return ResourceNode.builder()
                .name(this.name)
                .code(this.code)
                .type(this.type)
                .orderNum(this.orderNum)
                .state(this.state)
                .remark(this.remark)
                .parentId(parentId)
                .build();
    }
    
    /**
     * 使用枚举转换资源类型
     */
    private static Integer convertType(String typeStr) {
        if (typeStr == null) {
            return ResourceTypeEnum.PAGE.getCode();
        }
        
        return switch (typeStr.trim()) {
            case "页面" -> ResourceTypeEnum.PAGE.getCode();
            case "按钮" -> ResourceTypeEnum.BUTTON.getCode();
            case "接口" -> ResourceTypeEnum.API.getCode();
            default -> ResourceTypeEnum.PAGE.getCode();
        };
    }
    
    /**
     * 使用枚举转换状态
     */
    private static Integer convertState(String stateStr) {
        if (stateStr == null) {
            return StateEnum.ENABLE.getCode();
        }
        
        return switch (stateStr.trim()) {
            case "启用" -> StateEnum.ENABLE.getCode();
            case "禁用" -> StateEnum.DISABLE.getCode();
            default -> StateEnum.ENABLE.getCode();
        };
    }
} 