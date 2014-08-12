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
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.json.constants.JSONConstants;
import com.json.exceptions.JSONConfigInitializationException;
import com.json.parsers.JSONParser;
import com.json.utils.CachedInstances;

@SuppressWarnings("unused")
public class XmlConfigHandler extends DefaultHandler implements ConfigHandler{
	private Map<String,HashMap<String,String>> configMap=new HashMap<String,HashMap<String,String>>(6,(float)0.5);
	private HashMap<String,String> tempMap=new HashMap<String,String>();
	private InputStream stream=null;
	
	private String encoding=JSONConstants.DEFAULT_ENCODING;
	
	public void setStream(InputStream is){
		stream=is;
	}
	
	public void setEncoding(String encoding)
	{
		this.encoding=encoding;
	}
	
	public void parse()
	{
		try{
			SAXParserFactory factory=SAXParserFactory.newInstance();
			SAXParser parser=factory.newSAXParser();
			
			parser.parse(stream, this);
			
//			//System.out.println(configMap);
		}
		catch (Exception e) {
			throw new JSONConfigInitializationException(e);
		}
		finally{
			tempMap=null;
			if(stream!=null)
			{
				try{
					stream.close();
				}catch (Exception e) {
				}
			}
		}		
	}
	
	public XmlConfigHandler() {
	}
	
	public HashMap<String,String> getPatternMap(String path)
	{
		HashMap<String,String> patternMap=this.configMap.get(path);
		
		if(patternMap==null)
		{
//			throw new JSONParsingException("Validation Configuration Map is null for JSON Heirarchy..."+path);
			
			patternMap=new HashMap<String,String>();
			patternMap.put("default~~valueType", JSONConstants.STRING_LITERAL);
		}
		
		return patternMap;
	}
	
	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes attrib) throws SAXException {
		
		if(arg2.equalsIgnoreCase("KeyValue"))
		{
			String name=attrib.getValue("name");
			String index=attrib.getValue("index");
			
			if(name==null && index==null)
				throw new JSONConfigInitializationException("Either name or index attribute is mandatory for KeyValue tag element...");
			
			name=(name==null)?index:name;
			
			String key=new StringBuilder(name).append("~~").toString();
			
			tempMap.put(new StringBuilder(key).append("keyPattern").toString(), attrib.getValue("keyPattern"));
			tempMap.put(new StringBuilder(key).append("valuePattern").toString(), attrib.getValue("valuePattern"));
			tempMap.put(new StringBuilder(key).append("valueType").toString(), attrib.getValue("valueType"));
			tempMap.put(new StringBuilder(key).append("keyValidator").toString(), attrib.getValue("keyValidator"));
			tempMap.put(new StringBuilder(key).append("valueValidator").toString(), attrib.getValue("valueValidator"));
		}
		else if(arg2.equalsIgnoreCase("json-heirarchy"))
		{
			HashMap<String,String> patternMap=configMap.get(attrib.getValue("path"));
			
			if(patternMap==null)
			{
				patternMap=new HashMap<String,String>();
				
				configMap.put(attrib.getValue("path"), patternMap);
			}
			
			tempMap=patternMap;
		}
		else if(arg2.equalsIgnoreCase("custom-validator"))
		{
			CachedInstances.getInstance().loadAndCacheInstance(attrib.getValue("alias"),attrib.getValue("class"));
		}
	}

	public void setParserSelfInstance(JSONParser parser) {
	}
}
