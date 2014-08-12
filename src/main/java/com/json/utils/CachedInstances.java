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

package com.json.utils;

import java.util.HashMap;
import java.util.Map;

import com.json.validations.custom.SpecialValidator;

public class CachedInstances {
	private Map<String,SpecialValidator> validatorInstances=new HashMap<String,SpecialValidator>(1<<4,(float)0.25);
	
	private static CachedInstances instance=null;
	
	private CachedInstances()
	{
	}
	
	public static CachedInstances getInstance()
	{
		if(instance==null)
		{
			synchronized(CachedInstances.class)
			{
				if(instance==null)
					instance=new CachedInstances();
			}
		}
		
		return instance;
	}
	
	public void loadAndCacheInstance(String alias, String className)
	{
		validatorInstances.put(alias,ClassUtils.loadClass(className));
	}
	
	public SpecialValidator getValidatorInstance(String alias)
	{
		SpecialValidator sv=validatorInstances.get(alias);
		if(sv==null)
		{
			throw new IllegalArgumentException("Validator Instance Not Found with alias name..."+alias);
		}
		
		return sv;
	}
}
