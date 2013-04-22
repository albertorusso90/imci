package com.burkina.functions;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class GetDiagnosis {
	
	public boolean getResult(String data){
		boolean result = false;
		
		Context context = Context.enter();
		context.setOptimizationLevel(-1);
		
		try {
			Scriptable script = context.initStandardObjects();			
			result = (Boolean)context.evaluateString(script, data, "doit", 1, null);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}

}
