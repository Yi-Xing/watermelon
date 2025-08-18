package top.fblue.watermelon.application.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurrentUserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createdTime;

    /**
     * 更新时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String updatedTime;

    /**
     * 过期（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String expireTime;

    /**
     * 可访问的页面列表
     * 可访问的按钮列表
     */
    private List<Object> roles;
}
