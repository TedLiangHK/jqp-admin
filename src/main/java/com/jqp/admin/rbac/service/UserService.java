package com.jqp.admin.rbac.service;

import com.jqp.admin.rbac.data.Enterprise;
import com.jqp.admin.rbac.data.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<Enterprise> getUserEnterpriseList(User user);
}


