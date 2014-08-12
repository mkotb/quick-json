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
package com.json.serializers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.json.constants.JSONConstants;
import com.json.exceptions.SerializingException;
import com.json.generators.JSONGenerator;
import com.json.serializers.pojos.SerializerOptions;

public class ClassSerializer implements JsonSerializer {
	public void serialize(Object toBeSerialized, StringBuilder serializedData,
			SerializerOptions options) {
		
		try{
		Class clazz=toBeSerialized.getClass();
		
		Field[] fields=clazz.getDeclaredFields();
		
		serializedData.append(JSONConstants.OCBRACE);
		
		for(Field field:fields)
		{
			int modifier=field.getModifiers();
			
			serializedData.append(JSONConstants.DOUBLEQUOTES).append(field.getName()).append(JSONConstants.DOUBLEQUOTES).append(JSONConstants.COLON);
			
			if(Modifier.isPrivate(modifier))
			{
				field.setAccessible(true);
			}			
			
			Object obj=null;
			try {
				obj=field.get(toBeSerialized);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
			
			JSONGenerator.traverseObjects(obj, serializedData, true);
			
			serializedData.append(JSONConstants.COMMA);
		}
		
		serializedData.append(JSONConstants.CCBRACE);
		}
		catch (Exception e) {
			throw new SerializingException("Unexpected failure while serializing the object....");
		}
	}
}
