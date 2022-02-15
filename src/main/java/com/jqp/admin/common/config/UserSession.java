package com.jqp.admin.common.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hyz
 * @date 2021/3/3 10:19
 */
@Data
public class UserSession implements java.io.Serializable{
    private static final long serialVersionUID = -7946270162186590546L;
    private Long userId;
    private String token;
    private List<String> menuCodes = new ArrayList<>();
    private String userType;
    private Long enterpriseId;
}
