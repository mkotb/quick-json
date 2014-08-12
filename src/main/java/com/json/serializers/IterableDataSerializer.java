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

import com.json.constants.JSONConstants;
import com.json.generators.JSONGenerator;
import com.json.serializers.pojos.SerializerOptions;

public class IterableDataSerializer {
	public void serialize(Iterable toBeSerialized, StringBuilder serializedData,
			SerializerOptions options) {

		serializedData.append(JSONConstants.JSON_ARRAY_START);

		for(Object value:toBeSerialized)
		{
			if(!JSONGenerator.traverseObjects(value, serializedData, false))
				serializedData.append(JSONConstants.COMMA);
		}
		
		serializedData.deleteCharAt(serializedData.length()-1);
		serializedData.append(JSONConstants.JSON_ARRY_END);		
		
	}
}
