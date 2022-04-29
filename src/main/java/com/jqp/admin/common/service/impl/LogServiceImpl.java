package com.jqp.admin.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jqp.admin.common.BaseData;
import com.jqp.admin.common.Result;
import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.config.UserSession;
import com.jqp.admin.common.data.GlobalLog;
import com.jqp.admin.common.service.DataListenerService;
import com.jqp.admin.common.service.LogService;
import com.jqp.admin.db.data.ColumnInfo;
import com.jqp.admin.db.data.TableInfo;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.db.service.TableService;
import com.jqp.admin.rbac.data.User;
import com.jqp.admin.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("logService")
public class LogServiceImpl implements LogService {
    @Resource
    @Lazy
    private TableService tableService;
    @Resource
    @Lazy
    private JdbcService jdbcService;

    @Resource
    @Lazy
    private DataListenerService dataListenerService;
    @Override
    public void log(Map<String, Object> beforeObj, Map<String, Object> afterObj,String tableName) {
        if(beforeObj == null && afterObj == null){
            return;
        }
        if(tableName.equalsIgnoreCase("global_log")){
            return;
        }

        Long logTableId = jdbcService.findOneForObject("select id from log_table where lower(table_name) = ?", Long.class, tableName.toLowerCase());
        if(logTableId == null){
            return;
        }

        GlobalLog globalLog = new GlobalLog();
        globalLog.setCreateTime(new Date());
        globalLog.setTableName(tableName.toLowerCase());
        UserSession session = SessionContext.getSession();
        if(session != null){
            Long userId = session.getUserId();
            globalLog.setUserId(userId);
            User user = jdbcService.getById(User.class, userId);
            globalLog.setUserName(user.getName());
        }
        String operation = null;
        String now = DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss");
        if(beforeObj == null && afterObj != null){
            operation = "创建";
            globalLog.setAfterValue(JSONUtil.toJsonPrettyStr(afterObj));
            globalLog.setRemark(StrUtil.format("{}在{}创建了记录",globalLog.getUserName(),now));
            globalLog.setOptionType(operation);
            globalLog.setRefId(Long.valueOf(afterObj.get("id").toString()));
            jdbcService.saveOrUpdate(globalLog);
            dataListenerService.newObj(tableName,afterObj);
        }else if(beforeObj != null && afterObj == null){
            operation = "删除";
            globalLog.setBeforeValue(JSONUtil.toJsonPrettyStr(beforeObj));
            globalLog.setRemark(StrUtil.format("{}在{}删除了记录",globalLog.getUserName(),now));
            globalLog.setOptionType(operation);
            globalLog.setRefId(Long.valueOf(beforeObj.get("id").toString()));
            jdbcService.saveOrUpdate(globalLog);
            dataListenerService.deleteObj(tableName,beforeObj);
        }else{
            operation = "修改";
            globalLog.setOptionType(operation);
            globalLog.setRefId(Long.valueOf(beforeObj.get("id").toString()));
            Result<TableInfo> tableInfo = tableService.tableInfo(tableName);
            List<ColumnInfo> columnInfos = tableInfo.getData().getColumnInfos();
            boolean isUpdate = false;
            for(ColumnInfo columnInfo:columnInfos){
                String field = StringUtil.toFieldColumn(columnInfo.getColumnName());
                Object beforeValue = beforeObj.get(field);
                Object afterValue = afterObj.get(field);
                if(beforeValue == null && afterValue == null){
                    continue;
                }
                if(beforeValue != null && afterValue != null && beforeValue.equals(afterValue)){
                    continue;
                }
                GlobalLog pLog = BeanUtil.copyProperties(globalLog, GlobalLog.class);
                pLog.setField(columnInfo.getColumnName());
                pLog.setBeforeValue(getValueStr(beforeValue));
                pLog.setAfterValue(getValueStr(afterValue));
                if(pLog.getBeforeValue().equals(pLog.getAfterValue())){
                    continue;
                }
                pLog.setRemark(StrUtil.format("{}在{}将字段{}的值从{}改为{}",pLog.getUserName(),now,columnInfo.getColumnComment(),beforeValue,afterValue));
                jdbcService.saveOrUpdate(pLog);
                dataListenerService.updateObjColumn(tableName,columnInfo.getColumnName(),beforeObj,afterObj,beforeValue,afterValue);
                isUpdate = true;
            }
            if(isUpdate){
                dataListenerService.updateObj(tableName,beforeObj,afterObj);
            }
        }
    }

    private String getValueStr(Object value){
        if(value == null){
            return "";
        }
        String format = "yyyy-MM-dd HH:mm:ss";
        if(value instanceof LocalDateTime){
            value = DateUtil.format((LocalDateTime) value,format);
        }else if(value instanceof java.sql.Date){
            value = DateUtil.format((java.sql.Date) value,format);
        }else if(value instanceof java.util.Date){
            value = DateUtil.format((java.util.Date) value,format);
        }else{
            value = value.toString();
        }
        return (String) value;
    }

    @Override
    public void log(BaseData beforeObj, BaseData afterObj) {
        if(beforeObj == null && afterObj == null){
            return;
        }
        String tableName = StringUtil.toSqlColumn((beforeObj == null ? afterObj : beforeObj).getClass().getSimpleName());
        this.log(BeanUtil.beanToMap(beforeObj),BeanUtil.beanToMap(afterObj),tableName);
    }
}
