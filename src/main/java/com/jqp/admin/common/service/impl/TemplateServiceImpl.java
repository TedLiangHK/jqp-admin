package com.jqp.admin.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.config.UserSession;
import com.jqp.admin.common.service.TemplateService;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.page.service.PageService;
import com.jqp.admin.rbac.constants.UserType;
import com.jqp.admin.rbac.data.Permission;
import com.jqp.admin.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {
    @Resource
    private JdbcService jdbcService;
    @Resource
    private PageService pageService;
    @Override
    public String findAllParent(String childSql, String tableName) {
        childSql = pageService.getQuerySql(childSql);

        String nextChildSql = childSql;
        Set<Long> parentIds = new HashSet<>();
        List<Long> childIds = jdbcService.findForObject(childSql, Long.class);
        parentIds.addAll(childIds);
        if(childIds.isEmpty()){
            return "-1";
        }
        String parentSql = "select parent_id from "+tableName+" where id in ({}) and parent_id is not null ";
        while (true){
            String sql = StrUtil.format(parentSql,nextChildSql);
            List<Long> ids = jdbcService.findForObject(sql, Long.class);
            parentIds.addAll(ids);
            if(ids.isEmpty()){
                break;
            }
            nextChildSql = StringUtil.concatStr(ids,",");
        }
        return StringUtil.concatStr(parentIds,",");
    }

    @Override
    public String permission(String permissionCode, String field) {
        return this.permission(permissionCode,field,null);
    }

    private String permission(String permissionCode, String field,String tableName) {
        UserSession session = SessionContext.getSession();
        if(UserType.Admin.equals(session.getUserType())){
            return "";
        }
        Permission permission = jdbcService.findOne(Permission.class, "code", permissionCode);
        if(permission == null){
            return " and 1=-1 \n -- 权限编号错误"+permissionCode+" \n";
        }
        List<String> configValues = jdbcService.findForObject("select distinct t.config_value from (" +
                        "select config_value from position_permission " +
                        "where permission_id = ? " +
                        "and enterprise_id = ? " +
                        "and position_id in " +
                        "(" +
                        "select position_id from enterprise_user_position where enterprise_user_id in " +
                        "(" +
                        "select id from enterprise_user where user_id = ? and enterprise_id = ? " +
                        ")" +
                        ") union all " +
                        "select config_value from dept_permission " +
                        "where permission_id = ? " +
                        "and dept_id in (" +
                        "select dept_id from enterprise_user where user_id = ? and enterprise_id = ? " +
                        "and dept_id is not null" +
                        ")" +
                        ") t"
                , String.class,
                permission.getId(),
                session.getEnterpriseId(),
                session.getUserId(),
                session.getEnterpriseId(),

                permission.getId(),
                session.getUserId(),
                session.getEnterpriseId()
        );
        if(configValues.isEmpty()){
            return " and 1=-1 \n -- 没有配置数据权限"+permissionCode+" \n";
        }
        if("dic".equals(permission.getPermissionType()) && "selfOrAll".equals(permission.getPermissionValue())){
            //查看自己/全部
            if(configValues.contains("all")){
                return "";
            }
            return " and "+field+"="+session.getUserId()+" ";
        }else if("dic".equals(permission.getPermissionType()) && "dept".equals(permission.getPermissionValue())){
            //部门数据权限
            if(configValues.contains("all")){
                return "";
            }
            if(configValues.contains("dept") && configValues.contains("deptAndChild")){
                //所属,所属及子部门
                configValues.remove("dept");
            }
            if(configValues.contains("manageDept") && configValues.contains("manageDeptAndChild")){
                //负责,负责及子部门
                configValues.remove("manageDept");
            }
            Set<Long> ids = new HashSet<>();
            for(String value:configValues){
                if("dept".equals(value)){
                    List<Long> _ids = jdbcService.findForObject("select dept_id from enterprise_user " +
                            "where user_id = ? " +
                            "and enterprise_id = ? " +
                            "and dept_id is not null", Long.class, session.getUserId(), session.getEnterpriseId());
                    ids.addAll(_ids);
                }else if("deptAndChild".equals(value)){
                    Set<Long> _ids = jdbcService.findChildIds(StrUtil.format("select dept_id from enterprise_user " +
                                    "where user_id = {} " +
                                    "and enterprise_id = {} " +
                                    "and dept_id is not null",
                            session.getUserId(),
                            session.getEnterpriseId()), "select id from dept where parent_id in ({})");
                    ids.addAll(_ids);
                }else if("manageDept".equals(value)){
                    List<Long> _ids = jdbcService.findForObject("select dept_id from dept_manager where enterprise_user_id in(" +
                            "select id from enterprise_user " +
                            "where user_id = ? " +
                            "and enterprise_id = ? " +
                            "and dept_id is not null" +
                            ")",Long.class,session.getUserId(),session.getEnterpriseId());
                    ids.addAll(_ids);
                }else if("manageDeptAndChild".equals(value)){
                    Set<Long> _ids = jdbcService.findChildIds(StrUtil.format("select dept_id from dept_manager where enterprise_user_id in(" +
                                    "select id from enterprise_user " +
                                    "where user_id = {} " +
                                    "and enterprise_id = {} " +
                                    "and dept_id is not null" +
                                    ")",
                            session.getUserId(),
                            session.getEnterpriseId()), "select id from dept where parent_id in ({})");
                    ids.addAll(_ids);
                }
            }
            if(ids.isEmpty()){
                return "and 1=-1 \n -- 配置的部门数据权限"+permissionCode+"关联不到部门 \n";
            }
            return StrUtil.format(" and {} in ({}) ",field,StringUtil.concatStr(ids,","));
        }
        if(StringUtils.isBlank(tableName)){
            return StrUtil.format(" and {} in ({}) ",field,StringUtil.concatStr(configValues,",","'"));
        }
        return StrUtil.format("and {} in ({})",
                field,
                this.findAllParent(StrUtil.format("select id from {} where id in({})",tableName,StringUtil.concatStr(configValues,",")),tableName)
        );
    }

    @Override
    public String permissionTree(String permissionCode, String field, String tableName) {
        return this.permission(permissionCode,field,tableName);
    }
}
