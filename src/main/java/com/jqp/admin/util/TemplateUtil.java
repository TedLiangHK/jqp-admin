package com.jqp.admin.util;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.hutool.core.io.IoUtil;
import com.jqp.admin.common.service.TemplateService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class TemplateUtil {
	public static String getValue(String template,Map<String,? extends Object> params){
		Context context  = new VelocityContext();
		for(Entry<String,? extends Object> en : params.entrySet()){
			context.put(en.getKey(), en.getValue());
		}
		context.put("jq","$");
		context.put("service",SpringContextUtil.getBean(TemplateService.class));

		StringWriter sw = new StringWriter();

		try {
			Velocity.evaluate(context, sw, "velocity", template);
			sw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sw.toString();
	}

	public static String getUi(String path,Map<String,? extends Object> params){
		InputStream in = TemplateUtil.class.getClassLoader().getResourceAsStream("ui-json-template/"+path);
		String template = IoUtil.readUtf8(in);
		IoUtil.close(in);
		return TemplateUtil.getValue(template,params);
	}
	//definitions 属性只允许在最顶层定义
	public static void filterDefinitions(Map<String,Object> json){
		filterDefinitions(json,json);
	}
	public static void filterDefinitions(Map<String,Object> json,Map<String,Object> top){
		Map<String,Object> definitions = (Map<String, Object>) top.get("definitions");
		if(definitions == null){
			definitions = new HashMap<>();
			top.put("definitions",definitions);
		}
		Set<Entry<String, Object>> entries = json.entrySet();
		for(Entry<String, Object> en:entries){
			Object value = en.getValue();
			if(en.getKey().equals("definitions")){
				if(top != json ){
					definitions.putAll((Map<String, ?>) value);

				}
			}else if(value instanceof Map){
				filterDefinitions((Map<String, Object>) value,top);
			}else if(value instanceof List){
				for(Object item: ((List) value)){
					if(item instanceof Map){
						filterDefinitions((Map<String, Object>) item,top);
					}
				}
			}
		}
		if(json != top){
			json.remove("definitions");
		}
	}
}
