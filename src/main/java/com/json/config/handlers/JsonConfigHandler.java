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

package com.json.config.handlers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.json.constants.JSONConstants;
import com.json.exceptions.JSONConfigInitializationException;
import com.json.parsers.JSONParser;

public class JsonConfigHandler implements ConfigHandler{
	@SuppressWarnings("rawtypes")
	private Map configMap=null;
	private InputStream stream=null;
	private String encoding=JSONConstants.DEFAULT_ENCODING;
	private JSONParser parser=null;
	
	public void setStream(InputStream is){
		stream=is;
	}
	
	public void setEncoding(String encoding)
	{
		this.encoding=encoding;
	}
	
	public void setParserSelfInstance(JSONParser parser)
	{
		this.parser=parser;
	}
	
	public void parse()
	{
		if(parser==null)
			throw new JSONConfigInitializationException("JSON Parser instance is not initialized...is required to parser json based validation config....");
		
		try{
			configMap=parser.parseJson(stream,encoding);
			
			transform();
		}
		catch (Exception e) {
			throw new JSONConfigInitializationException(e);
		}
		finally{
			if(stream!=null)
			{
				try{
					stream.close();
				}catch (Exception e) {
				}
			}
		}		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void transform()
	{
		Map rootMap=configMap;
		
		if(rootMap==null)
			throw new JSONConfigInitializationException("validation configuration is not available...");

		StringBuilder temp=new StringBuilder();
		StringBuilder sb=new StringBuilder();
		Map tempMap=null;
		
		configMap=new HashMap();
		
		for(Iterator i=rootMap.keySet().iterator();i.hasNext();)
		{
			String rootKey=(String)i.next();
			Map rootValueMap=(Map)rootMap.get(rootKey);
			
			tempMap=new HashMap();
			
			for(Iterator it=rootValueMap.entrySet().iterator();it.hasNext();)
			{
				Map.Entry entry=(Map.Entry)it.next();
				String name=(String)entry.getKey();
				Map valueMap=(Map)entry.getValue();
				
				sb.delete(0, sb.length());
				sb.append(name).append(JSONConstants.TILDE_DELE);
				
				for(Iterator ite=valueMap.entrySet().iterator();ite.hasNext();)
				{
					Map.Entry<String, String> ent=(Map.Entry<String,String>)ite.next();
					String key=ent.getKey();
					String value=ent.getValue();
					
					temp.delete(0, temp.length());
					tempMap.put(temp.append(sb).append(key).toString(),value);
				}
			}
			
			configMap.put(rootKey, tempMap);
		}
		
		rootMap.clear();
		rootMap=null;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String,String> getPatternMap(String path)
	{
		HashMap<String,String> patternMap=(HashMap<String,String>)this.configMap.get(path);
		
		if(patternMap==null)
		{
			patternMap=new HashMap<String,String>();
			patternMap.put("default~~valueType", JSONConstants.STRING_LITERAL);
		}
		
		return patternMap;
	}	
}
