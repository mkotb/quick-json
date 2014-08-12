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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import com.json.constants.JSONConstants;
import com.json.exceptions.JSONConfigInitializationException;
import com.json.exceptions.JSONParsingException;

public class JSONUtility {
	public static InputStream getStream(String classPathResource)
	{
		if(classPathResource==null || classPathResource.trim().equals(JSONConstants.EMPTYSTR))
			throw new IllegalArgumentException("input configuration file path is null/empty...");
		
		InputStream is=JSONUtility.class.getResourceAsStream(classPathResource);
		
		if(is==null)
			is=JSONUtility.class.getClassLoader().getParent().getResourceAsStream(classPathResource);
		
		if(is==null)
			is=Thread.currentThread().getContextClassLoader().getResourceAsStream(classPathResource);
		
		if(is==null)
			throw new JSONConfigInitializationException("classpath resource cannot be loaded...");
		
		return is;
	}
	
	public static String loadAndReadStream(String classPathResource,String encoding)
	{
		InputStream is=getStream(classPathResource);
		
		BufferedReader br=null;
		try {
			br=new BufferedReader(new InputStreamReader(is,Charset.forName(encoding)));			
			return readChunks(br,encoding);
		} catch (Exception e) {
			new JSONParsingException("JSON Reading failed for classpath resource..."+classPathResource);
		}
		finally{
			if(br!=null)
			{
				try{
				br.close();}catch (Exception e) {
				}
			}			
		}
		
		return null;
	}
	
	public static String readStream(InputStream is,String encoding)
	{
		BufferedReader br=null;
		try {
			br=new BufferedReader(new InputStreamReader(is,Charset.forName(encoding)));			
			return readChunks(br,encoding);
		} catch (Exception e) {
			new JSONParsingException("Configuration Reading failed for stream..."+is);
		}
		finally{
			if(br!=null)
			{
				try{
				br.close();}catch (Exception e) {
				}
			}			
		}
		
		return null;
	}
	
	public static String readChunks(BufferedReader br,String encoding)  throws Exception
	{
		char[] charBuffer = new char[8192];
		StringBuilder builder=new StringBuilder();
		int offset=-1;
		while((offset=br.read(charBuffer))>-1)
		{
			builder.append(charBuffer,0,offset);
		}
		return builder.toString();		
	}
	
	public static void handleFailure(List<String> heirarchyList, StringBuilder key,String... strings) {
		
		StringBuilder sb=new StringBuilder();
		sb.append("@Key-Heirarchy::");
		
		for(String ele:heirarchyList)
		{
			sb.append(ele).append("/");
		}
		
		sb.append("\t@Key::");
		
		sb.append(key).append("\t");
		
		for(String string:strings)
		{
			sb.append(string);
		}
		
		throw new JSONParsingException(sb.toString());
	}	
}
