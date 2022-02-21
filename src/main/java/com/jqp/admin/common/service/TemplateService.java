package com.jqp.admin.common.service;

public interface TemplateService {
    String findAllParent(String childSql,String tableName);
    String permission(String permissionCode,String field);
    String permissionTree(String permissionCode,String field,String tableName);
}
