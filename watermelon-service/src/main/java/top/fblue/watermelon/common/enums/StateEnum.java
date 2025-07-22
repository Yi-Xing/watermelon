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

    public static StateEnum fromCode(Integer code) {
        for (StateEnum state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid user state code: " + code);
    }
}
