package top.fblue.watermelon.application.dto;

import lombok.Builder;
import lombok.Data;
import top.fblue.watermelon.application.vo.ResourceExcelVO;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.resource.entity.ResourceNode;

/**
 * 资源节点导入VO
 * 用于Excel导入时的中间对象，使用parentCode来标识关联关系
 */
@Data
@Builder
public class ResourceNodeImportDTO {

    /**
     * 上级资源code
     */
    private String parentCode;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源类型
     */
    private Integer type;

    /**
     * 资源code
     */
    private String code;

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
}