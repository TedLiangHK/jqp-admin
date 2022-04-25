package com.jqp.admin.rbac.service.impl;

import com.jqp.admin.common.Result;
import com.jqp.admin.rbac.service.ApiService;
import com.jqp.admin.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.service.MagicAPIService;

import javax.annotation.Resource;
import java.util.Map;

@Service("apiService")
public class ApiServiceImpl implements ApiService {

    @Resource
    private MagicAPIService magicAPIService;

    @Override
    public Result<String> call(String api, Map<String, Object> context) {
        if(StringUtils.isNotBlank(api)){
            String[] apis = StringUtil.splitStr(api, "\n");
            for(String a:apis){
                if(StringUtils.isNotBlank(a)){
                    JsonBean<String> result = magicAPIService.call("post", a, context);
                    if(result.getCode() != 1){
                        return Result.error(result.getMessage());
                    }
                    String data = result.getData();
                    if(StringUtils.isNotBlank(data)){
                        return Result.error(data);
                    }

                }
            }
        }
        return Result.success();
    }
}
