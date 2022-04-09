package com.jqp.admin.util;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

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
}
