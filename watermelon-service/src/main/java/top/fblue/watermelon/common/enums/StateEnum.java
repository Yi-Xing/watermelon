package top.fblue.watermelon.common.enums;

import lombok.Getter;

/**
 * 状态枚举
 */
@Getter
public enum StateEnum {
    ENABLE(1, "启用"),
    DISABLE(2, "禁用");

    private final Integer code;
    private final String desc;

    StateEnum(Integer code, String description) {
        this.code = code;
        this.desc = description;
    }

    /**
     * 根据code获取枚举
     */
    public static StateEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StateEnum state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid state code: " + code);
    }

    /**
     * 根据描述获取枚举
     */
    public static StateEnum fromDesc(String desc) {
        if (desc == null) {
            throw new IllegalArgumentException("Invalid state type desc is null ");
        }
        for (StateEnum type : values()) {
            if (type.getDesc().equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid state type desc: " + desc);
    }

    /**
     * 根据code获取描述
     */
    public static String getDescByCode(Integer code) {
        StateEnum state = fromCode(code);
        return state != null ? state.getDesc() : null;
    }

    /**
     * 检查code是否有效
     */
    public static boolean isValidCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (StateEnum state : values()) {
            if (state.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查 desc 是否有效
     */
    public static boolean isValidDesc(String desc) {
        for (StateEnum type : values()) {
            if (type.getDesc().equals(desc)) {
                return true;
            }
        }
        return false;
    }
}
