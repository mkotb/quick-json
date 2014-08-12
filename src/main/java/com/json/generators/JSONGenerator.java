/*   Copyright 2013 Rajesh Putta

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 * 
 * 
 */

package com.json.generators;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.json.constants.JSONConstants;
import com.json.serializers.ArraySerializer;
import com.json.serializers.ClassSerializer;
import com.json.serializers.DateSerializer;
import com.json.serializers.JsonSerializer;
import com.json.serializers.ListSerializer;
import com.json.serializers.MapSerializer;
import com.json.serializers.NumberSerializer;
import com.json.serializers.PropertiesSerializer;
import com.json.serializers.SetSerializer;

public class JSONGenerator {
	
	private static JsonSerializer arraySerializer=new ArraySerializer();
	private static JsonSerializer classSerializer=new ClassSerializer();
	private static JsonSerializer dateSerializer=new DateSerializer();
	private static JsonSerializer listSerializer=new ListSerializer();
	private static JsonSerializer mapSerializer=new MapSerializer();
	private static JsonSerializer propertiesSerializer=new PropertiesSerializer();
	private static JsonSerializer setSerializer=new SetSerializer();	
	private static JsonSerializer numberSerializer=new NumberSerializer();
	
	public String generateJson(Object toBeSerialized)
	{
		if(toBeSerialized==null)
		{
			return JSONConstants.EMPTY_JSON;
		}
		
		StringBuilder jsonStr=new StringBuilder();
		
		process(toBeSerialized,jsonStr);
		
		return jsonStr.toString();
	}
	
	private void process(Object toBeSerialized,StringBuilder jsonStr)
	{
			jsonStr.append(JSONConstants.JSON_ARRAY_START);
			traverseObjects(toBeSerialized,jsonStr,true);		
			jsonStr.append(JSONConstants.JSON_ARRY_END);
	}

	public static boolean traverseObjects(Object toBeSerialized, StringBuilder jsonStr,boolean isArrayAllowed) {
		
		boolean flag=false;
		
		if(toBeSerialized==null)
		{
			jsonStr.append(JSONConstants.NULL);
		}
		else if(toBeSerialized instanceof CharSequence)
		{
			jsonStr.append(JSONConstants.DOUBLEQUOTES).append(toBeSerialized).append(JSONConstants.DOUBLEQUOTES);
		}
		else if(toBeSerialized instanceof Number)
		{
			numberSerializer.serialize(toBeSerialized, jsonStr, null);
		}
		else if(toBeSerialized instanceof Date)
		{
			dateSerializer.serialize(toBeSerialized, jsonStr, null);
		}		
		else if(toBeSerialized instanceof Boolean)
		{
			jsonStr.append(toBeSerialized.toString());
		}
		else if(toBeSerialized instanceof Set)
		{
			flag=true;
			if(isArrayAllowed)
				setSerializer.serialize(toBeSerialized, jsonStr, null);
		}
		else if(toBeSerialized instanceof List)
		{
			flag=true;
			if(isArrayAllowed)
				listSerializer.serialize(toBeSerialized, jsonStr, null);
		}
		else if(toBeSerialized instanceof Map)
		{
			mapSerializer.serialize(toBeSerialized, jsonStr, null);
		}
		else if(toBeSerialized instanceof Properties)
		{
			propertiesSerializer.serialize(toBeSerialized, jsonStr, null);
		}
		else if(toBeSerialized.getClass().isArray())
		{
			flag=true;
			if(isArrayAllowed)
				arraySerializer.serialize(toBeSerialized, jsonStr, null);
		}
		else
		{
			classSerializer.serialize(toBeSerialized, jsonStr, null);
		}
		
		return flag;
	}
}
