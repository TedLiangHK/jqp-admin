package com.jqp.admin.common.service.impl;

import com.jqp.admin.common.config.SessionContext;
import com.jqp.admin.common.constants.EventType;
import com.jqp.admin.common.data.DataListener;
import com.jqp.admin.common.service.DataListenerService;
import com.jqp.admin.db.service.JdbcService;
import com.jqp.admin.rbac.service.ApiService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("dataListenerService")
public class DataListenerServiceImpl implements DataListenerService {
    @Resource
    private JdbcService jdbcService;
    @Resource
    private ApiService apiService;
    @Override
    public void newObj(String tableName, Map<String, Object> obj) {
        List<DataListener> list = jdbcService.find("select * from data_listener " +
                "where table_name =? " +
                "and event_type = ? " +
                "order by seq asc ",
                DataListener.class,
                tableName,
                EventType.NEW);
        Map<String,Object> context = new HashMap<>();
        context.put("obj",obj);
        context.put("tableName",tableName);
        SessionContext.putUserSessionParams(context);
        for(DataListener dataListener:list){
            apiService.call(dataListener.getAfterApi(),context);
        }
    }

    @Override
    public void deleteObj(String tableName, Map<String, Object> obj) {
        List<DataListener> list = jdbcService.find("select * from data_listener " +
                        "where table_name =? " +
                        "and event_type = ? " +
                        "order by seq asc ",
                DataListener.class,
                tableName,
                EventType.DELETE);
        Map<String,Object> context = new HashMap<>();
        context.put("obj",obj);
        context.put("tableName",tableName);
        SessionContext.putUserSessionParams(context);
        for(DataListener dataListener:list){
            apiService.call(dataListener.getAfterApi(),context);
        }
    }

    @Override
    public void updateObj(String tableName, Map<String, Object> beforeObj, Map<String, Object> afterObj) {
        List<DataListener> list = jdbcService.find("select * from data_listener " +
                        "where table_name =? " +
                        "and event_type = ? " +
                        "order by seq asc ",
                DataListener.class,
                tableName,
                EventType.UPDATE);
        Map<String,Object> context = new HashMap<>();
        context.put("beforeObj",beforeObj);
        context.put("afterObj",afterObj);
        context.put("tableName",tableName);
        SessionContext.putUserSessionParams(context);
        for(DataListener dataListener:list){
            apiService.call(dataListener.getAfterApi(),context);
        }
    }

    @Override
    public void updateObjColumn(String tableName, String columnName, Map<String, Object> beforeObj, Map<String, Object> afterObj, Object beforeValue, Object afterValue) {
        List<DataListener> list = jdbcService.find("select * from data_listener " +
                        "where table_name =? " +
                        "and event_type = ? " +
                        "and column_name =? " +
                        "order by seq asc ",
                DataListener.class,
                tableName,
                EventType.UPDATE_COLUMN,
                columnName);
        Map<String,Object> context = new HashMap<>();
        context.put("beforeObj",beforeObj);
        context.put("afterObj",afterObj);
        context.put("tableName",tableName);
        context.put("beforeValue",beforeValue);
        context.put("afterValue",afterValue);
        SessionContext.putUserSessionParams(context);
        for(DataListener dataListener:list){
            apiService.call(dataListener.getAfterApi(),context);
        }
    }
}
