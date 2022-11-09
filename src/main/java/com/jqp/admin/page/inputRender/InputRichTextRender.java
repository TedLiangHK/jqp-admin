package com.jqp.admin.page.inputRender;

import com.jqp.admin.page.data.InputField;

import java.util.HashMap;
import java.util.Map;

public class InputRichTextRender  extends InputDefaultRender{
    @Override
    protected void extra(Map<String, Object> config, InputField field) {
        config.put("receiver","/admin/upload");
        config.put("size",field.getFormat());

        //使用绝对路径地址
        Map<String, Object> options = new HashMap<>();
        options.put("relative_urls",false);
        options.put("remove_script_host",true);

        config.put("options",options);
    }
}

