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

import java.util.Map;
import java.util.Set;

import com.json.constants.JSONConstants;
import com.json.exceptions.SerializingException;
import com.json.generators.JSONGenerator;
import com.json.serializers.pojos.SerializerOptions;

public class MapSerializer implements JsonSerializer {
	public void serialize(Object toBeSerialized, StringBuilder serializedData,
			SerializerOptions options) {
		
		if(toBeSerialized==null || !(toBeSerialized instanceof Map))
		{
			throw new SerializingException("input object is not an instance of map");
		}
		
		serializedData.append(JSONConstants.OCBRACE);
		
		Set<Map.Entry> entrySet=((Map)toBeSerialized).entrySet();
		
		for(Map.Entry entry:entrySet)
		{
			String key=String.valueOf(entry.getKey());
			
			serializedData.append(JSONConstants.DOUBLEQUOTES).append(key).append(JSONConstants.DOUBLEQUOTES).append(JSONConstants.COLON);
			
			Object value=entry.getValue();
			
			JSONGenerator.traverseObjects(value, serializedData, true);
			
			serializedData.append(JSONConstants.COMMA);
		}
		
		serializedData.deleteCharAt(serializedData.length()-1);
		serializedData.append(JSONConstants.CCBRACE);
		
	}
}
