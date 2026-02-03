package com.mi.nr.price.warehouse.response;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户基本信息对象
 */
@Data
public class UserBaseResponse implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名称
     */
    private String name;
}
