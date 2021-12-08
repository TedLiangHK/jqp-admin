package com.jqp.admin.util;

import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

public class TemplateUtil {
	public static String getValue(String template,Map<String,? extends Object> params){
		Context context  = new VelocityContext();
		for(Entry<String,? extends Object> en : params.entrySet()){
			context.put(en.getKey(), en.getValue());
		}
		
		StringWriter sw = new StringWriter();
		
		try {
			Velocity.evaluate(context, sw, "velocity", template);
			sw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sw.toString();
	}
}
