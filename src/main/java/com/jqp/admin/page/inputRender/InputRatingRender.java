package com.jqp.admin.page.inputRender;

import cn.hutool.extra.spring.SpringUtil;
import com.jqp.admin.page.data.InputField;
import com.jqp.admin.page.service.DicService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputRatingRender  extends InputDefaultRender{
    @Override
    protected void extra(Map<String, Object> config, InputField field) {
        if(StringUtils.isNotBlank(field.getFormat())){
            DicService dicService = SpringUtil.getBean(DicService.class);
            List<Map<String, Object>> options = dicService.options(field.getFormat());

            config.put("count",options.size());

            Map<String,String> texts = new HashMap<>();
            for(Map<String,Object> en:options){
                texts.put((String)en.get("value"),(String)en.get("label"));
            }
            config.put("texts",texts);
        }
    }
}
