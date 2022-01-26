package com.jqp.admin.page.inputRender;

import cn.hutool.extra.spring.SpringUtil;
import com.jqp.admin.page.data.InputField;
import com.jqp.admin.page.service.DicService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class SwitchRender extends InputDefaultRender{
    @Override
    protected void extra(Map<String, Object> config, InputField field) {
        if(StringUtils.isNotBlank(field.getFormat())){
            DicService dicService = SpringUtil.getBean(DicService.class);
            List<Map<String, Object>> options = dicService.options(field.getFormat());
            if(options.size() == 2){
                Map<String, Object> trueObj = options.get(0);
                Map<String, Object> falseObj = options.get(0);

                config.put("trueValue",trueObj.get("value"));
                config.put("falseValue",falseObj.get("value"));
                config.put("onText",trueObj.get("label"));
                config.put("offText",falseObj.get("label"));
            }
        }else{
            config.put("trueValue","YES");
            config.put("falseValue","NO");
        }
    }
}
