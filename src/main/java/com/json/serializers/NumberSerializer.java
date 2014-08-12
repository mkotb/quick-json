/*   Copyright 2013 Rajesh Putta

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHserializedData WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 * 
 * 
 */

package com.json.serializers;

import com.json.exceptions.SerializingException;
import com.json.serializers.pojos.SerializerOptions;

public class NumberSerializer implements JsonSerializer {
	public void serialize(Object toBeSerialized, StringBuilder serializedData,
			SerializerOptions options) {
		if(!(toBeSerialized instanceof Number))
			throw new SerializingException("input object is not of type number...");
		
		if(toBeSerialized instanceof Double)
		{
			double doubleVal=((Double)toBeSerialized).doubleValue();
			if(Double.isNaN(doubleVal) || Double.isInfinite(doubleVal))
				throw new SerializingException("invalid Double type value..."+toBeSerialized);
			
			serializedData.append(doubleVal);
		}
		else if(toBeSerialized instanceof Float)
		{
			float floatVal=((Float)toBeSerialized).floatValue();
			if(Float.isNaN(floatVal) || Float.isInfinite(floatVal))
				throw new SerializingException("invalid Float type value..."+toBeSerialized);
			
			serializedData.append(floatVal);
		}
		else
		{
			serializedData.append(toBeSerialized);
		}
	}
}
