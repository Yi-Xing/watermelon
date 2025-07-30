package top.fblue.watermelon.common.enums;

import lombok.Getter;

/**
 * 资源类型枚举
 * 1 页面，2 按钮，3 接口
 */
@Getter
public enum ResourceTypeEnum {
    PAGE(1, "页面"),
    BUTTON(2, "按钮"),
    API(3, "接口");

    private final Integer code;
    private final String desc;

    ResourceTypeEnum(Integer code, String description) {
        this.code = code;
        this.desc = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ResourceTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResourceTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid resource type code: " + code);
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        ResourceTypeEnum type = fromCode(code);
        return type != null ? type.getDesc() : null;
    }

    /**
     * 检查code是否有效
     */
    public static boolean isValidCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (ResourceTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查 desc 是否有效
     */
    public static boolean isValidDesc(String desc) {
        for (ResourceTypeEnum type : values()) {
            if (type.getDesc().equals(desc)) {
                return true;
            }
        }
        return false;
    }
}
