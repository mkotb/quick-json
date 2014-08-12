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

import java.util.Properties;
import java.util.Set;

import com.json.constants.JSONConstants;
import com.json.exceptions.SerializingException;
import com.json.serializers.pojos.SerializerOptions;

public class PropertiesSerializer implements JsonSerializer{
	public void serialize(Object toBeSerialized, StringBuilder serializedData,
			SerializerOptions options) {

		if(toBeSerialized==null || !(toBeSerialized instanceof Properties))
		{
			throw new SerializingException("input object is not an instance of Properties");
		}
		
		serializedData.append(JSONConstants.OCBRACE);
		
		Properties prop=(Properties)toBeSerialized;
		Set keySet=prop.keySet();
		
		for(Object keyOb:keySet)
		{
			String key=(String)keyOb;
			String value=prop.getProperty(key);
			
			serializedData.append(JSONConstants.DOUBLEQUOTES).append(key).append(JSONConstants.DOUBLEQUOTES).append(JSONConstants.COLON);
			serializedData.append(JSONConstants.DOUBLEQUOTES).append(value).append(JSONConstants.DOUBLEQUOTES).append(JSONConstants.COMMA);
		}
		
		serializedData.deleteCharAt(serializedData.length()-1);
		serializedData.append(JSONConstants.CCBRACE);
		
	}
}
