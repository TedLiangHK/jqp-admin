package com.jqp.admin.common.config;

import com.jqp.admin.common.CrudData;
import com.jqp.admin.common.Result;
import lombok.Data;

import java.util.*;

/**
 * @author hyz
 * @date 2021/3/3 10:19
 */
@Data
public class UserSession implements java.io.Serializable{
    private static final long serialVersionUID = -7946270162186590546L;
    private Long userId;
    private String token;
    private String userType;
    private Long enterpriseId;
    private Set<String> buttonCodes;
    List<Map<String, Object>> currentUserMenu;

}
