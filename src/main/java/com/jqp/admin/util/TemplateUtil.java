package com.jqp.admin.util;

import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import cn.hutool.core.io.FileUtil;
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
		URL url = TemplateUtil.class.getClassLoader().getResource("ui-json-template/"+path);
		String template = FileUtil.readUtf8String(url.getFile());
		return TemplateUtil.getValue(template,params);
	}
}
