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

import com.json.constants.JSONConstants;
import com.json.exceptions.SerializingException;
import com.json.generators.JSONGenerator;
import com.json.serializers.pojos.SerializerOptions;

public class ArraySerializer implements JsonSerializer {
	
	public void serialize(Object toBeSerialized, StringBuilder serializedData,SerializerOptions options) {
		
		if(toBeSerialized==null)
			throw new SerializingException("input object is null");
		
		Class<?> clazz=toBeSerialized.getClass();
		
		if(!clazz.isArray())
		{
			throw new SerializingException("input object is not an Array");
		}
		
      Class<?> c = clazz.getComponentType();

      serializedData.append(JSONConstants.JSON_ARRAY_START);
      
      String type=c.getCanonicalName();
      
      boolean flag=true;

	  if (c.isPrimitive()) 
	  {
	        if (type.equals(JSONConstants.I))
	        {
		          for (int integer : (int[])toBeSerialized) {
		        	  	serializedData.append(Integer.toString(integer)).append(JSONConstants.COMMA);
		          }
	        }
	        else if (type.equals(JSONConstants.S))
	        {
		          for (short sh : (short[])toBeSerialized) {
		        	  serializedData.append(Short.toString(sh)).append(JSONConstants.COMMA);
		          }
	        }
	        else if (type.equals(JSONConstants.B))
	        {
			      for (boolean bool : (boolean[])(boolean[])toBeSerialized) {
			    	  serializedData.append(bool).append(JSONConstants.COMMA);
			      }
	        }
	        else if (type.equals(JSONConstants.C))
	        {
		          for (char ch : (char[])toBeSerialized) {
		        	  serializedData.append(ch).append(JSONConstants.COMMA);
		          }
	        }
	        else if (type.equals(JSONConstants.BY))
	        {
		          for (byte b : (byte[])toBeSerialized) {
		        	  serializedData.append(Byte.toString(b)).append(JSONConstants.COMMA);
		          }
	        }
	        else if (type.equals(JSONConstants.L))
	        {
		          for (long l : (long[])toBeSerialized) {
		        	  serializedData.append(Long.toString(l)).append(JSONConstants.COMMA);
		          }
	        }
	        else if (type.equals(JSONConstants.F))
	        {
		          for (float f : (float[])toBeSerialized) {
		        	  serializedData.append(Float.toString(f)).append(JSONConstants.COMMA);
		          }
	        }
	        else if (type.equals(JSONConstants.D))
	        {
		          for (double d : (double[])(double[])toBeSerialized) {
		        	  serializedData.append(Double.toString(d)).append(JSONConstants.COMMA);
		          }
	        }
	        else
	        	flag=false;
	  }
      else 
      {
			for(Object obj:(Object[])toBeSerialized)
			{
				boolean isIterable=JSONGenerator.traverseObjects(obj,serializedData,false);
				
				if(!isIterable)
				{
					serializedData.append(JSONConstants.COMMA);
				}
			}
      }
	  
      if(flag)
      	serializedData.deleteCharAt(serializedData.length()-1);	  
	  
      serializedData.append(JSONConstants.JSON_ARRY_END);
	}
}
