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


package com.json.parsers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.json.config.handlers.ConfigHandler;
import com.json.constants.JSONConstants;
import com.json.types.CollectionTypes;
import com.json.types.DefaultCollectionType;
import com.json.utils.CachedInstances;
import com.json.utils.JSONUtility;
import com.json.validations.custom.SpecialValidator;


public class JSONParser {
	
	private int maxLength=-1;
	private Stack<String> tokenStack=new Stack<String>();
	private List<String> heirarchyList=new ArrayList<String>(6);
	private StringBuilder key=new StringBuilder();
	private StringBuilder value=new StringBuilder();
	private String jsonBlockName=null;
	private boolean isValidating=false;
	private ConfigHandler configHandler=null;
	private CollectionTypes collectionTypes=null;
	
	
	public void setValidating(boolean isValidating)
	{
		this.isValidating=isValidating;
	}
	
	public void setConfigHandler(ConfigHandler configHandler)
	{
		this.configHandler=configHandler;
	}
	
	public void setCollectionTypes(CollectionTypes collectionTypes)
	{
		this.collectionTypes=collectionTypes;
	}
	
	public void initialize(InputStream is)
	{
		configHandler.setStream(is);
		configHandler.parse();
	}

	public void initializeWithJson(InputStream is)
	{
		configHandler.setStream(is);
		configHandler.parse();
	}
	
	public void initialize(String classPathResource)
	{
		InputStream is=JSONUtility.getStream(classPathResource);
		configHandler.setStream(is);
		configHandler.parse();
	}	
	
	@SuppressWarnings("rawtypes")
	public Map parseJson(InputStream stream,String encoding) {
		String content=JSONUtility.readStream(stream, encoding);
		return parseJson(content);
	}	
	
	@SuppressWarnings("rawtypes")
	public Map parseJson(String classPathResource,String encoding) {
		String content=JSONUtility.loadAndReadStream(classPathResource, encoding);
		return parseJson(content);
	}
	
	@SuppressWarnings("rawtypes")
	private Map getAssociatedMapType()
	{
		if(this.collectionTypes==null)
			this.collectionTypes=new DefaultCollectionType();
		
		return this.collectionTypes.getMapType();
	}
	
	@SuppressWarnings("rawtypes")
	private List getAssociatedListType()
	{
		if(this.collectionTypes==null)
			this.collectionTypes=new DefaultCollectionType();
		
		return this.collectionTypes.getListType();
	}	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map parseJson(String content) {
		if(content==null || content.trim().equals(JSONConstants.EMPTYSTR))
			throw new IllegalArgumentException("input JSON string is null/empty...");
		int index=0;
		maxLength=content.length();
		
		heirarchyList.add(JSONConstants.ROOT_SMALL);
		String pattern=null;
		
		if(!isValidating)
		{
			pattern=identifyApplicablePattern(content,index);
		}
		else pattern=getPattern(JSONConstants.ROOT, "valueType");
		
		heirarchyList.remove(0);
		
		Map completeJsonData=getAssociatedMapType();
		
		if(pattern==null || pattern.equalsIgnoreCase(JSONConstants.JSON))
		{
			for(;;)
			{
					Map jsonData=getAssociatedMapType();
					index=parseJsonBlock(content, index, jsonData);
					
					index=skipWhiteSpaceEOF(content, index);
					
					if(jsonBlockName.equals(JSONConstants.ROOT_SMALL))
					{
						completeJsonData=jsonData;
						break;
					}
					else	
						completeJsonData.put(jsonBlockName,jsonData);
					
					if(index==-1)break;
			}
		}
		else if(pattern.equalsIgnoreCase(JSONConstants.JSON_ARRAY))
		{
			index=skipWhiteSpace(content, index,String.valueOf(JSONConstants.JSON_ARRAY_START));
			
			/*To handle junk data japanese characters*/	
			if(!Character.isLetterOrDigit(content.charAt(index)) && content.charAt(index)!=JSONConstants.JSON_ARRAY_START)
			{
				index++;
			}
			/*To handle junk data japanese characters*/	
			
			index=skipWhiteSpace(content, index,String.valueOf(JSONConstants.JSON_ARRAY_START));
			
			if(content.charAt(index)==JSONConstants.JSON_ARRAY_START)
			{
				key.append(JSONConstants.ROOT_SMALL);
				index=jsonArrayTemplate(content, index,completeJsonData,false);
			}
			else
				JSONUtility.handleFailure(heirarchyList,key,"JSON_ARRAY_START_TOKEN is expected but not found...@Position::",String.valueOf(index));			
		

			index=skipWhiteSpaceEOF(content, index);
		}
		
		if(index!=-1)
			JSONUtility.handleFailure(heirarchyList,key,"JSON_EOF is expected but unexpectedly found some data...@Position::",String.valueOf(index));
		
		return completeJsonData;		
	}
	
	private String identifyApplicablePattern(String content,int index) {
		index=skipWhiteSpace(content, index, String.valueOf(JSONConstants.JSON_ARRAY_START));

		/*To handle junk data japanese characters*/
		if(content.charAt(index)!=JSONConstants.JSON_ARRAY_START && content.charAt(index)!=JSONConstants.OCBRACE && !Character.isLetterOrDigit(content.charAt(index)))
			index++;
		
		/*To handle junk data japanese characters*/		
		
		if(content.charAt(index)==JSONConstants.JSON_ARRAY_START)
			return JSONConstants.JSON_ARRAY;
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	private int parseJsonBlock(String content,int index,Map jsonData)
	{
		for(;;)
		{
			index=skipWhiteSpace(content,index,JSONConstants.VAR);
			index=consumeVarDeclarationIfAny(content, index);
			StringBuilder nameToken=new StringBuilder();
			index=consumeKeyOrName(content, index,nameToken,JSONConstants.EQUAL);
			verifyIfPatternMatches(nameToken.toString(),JSONConstants.NM_TOKEN);
			heirarchyList.add(nameToken.toString());
			jsonBlockName=nameToken.toString();
			index=parseJsonBody(content, index,jsonData);
			index=consumeSemiColonIfPresent(content,index,JSONConstants.SEMICOLON);
			break;
		}

		return index;
	}
	
	private int consumeSemiColonIfPresent(String content, int index, char semicolon) {
		int tmpindex=skipWhiteSpaceEOF(content,index);
		if(tmpindex!=-1 && content.charAt(tmpindex)==JSONConstants.SEMICOLON)
			return tmpindex+1;
		
		return index;
			
	}

	@SuppressWarnings("rawtypes")
	private int parseJsonBody(String content, int index,Map jsonData) {
		index=consumeJsonBodyStartToken(content,index);
		index=parseKeysValues(content,index,jsonData);
		consumeJsonBodyEndToken(content,index);
		return index;
	}
	
	private void consumeJsonBodyEndToken(String content, int index) {
		if(tokenStack.isEmpty())
			JSONUtility.handleFailure(heirarchyList,key,"Extra Close Curly Brace Occured...@Position::",String.valueOf(index));
		tokenStack.pop();
		
		if(!heirarchyList.isEmpty())
			heirarchyList.remove(heirarchyList.size()-1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int parseKeysValues(String content, int index,Map jsonData) {
		for(;;)
		{
			key.delete(0, key.length());
			index=parseKey(content,index);
			
			value.delete(0, value.length());
			int tmpIndex=parseValue(content, index, jsonData);
			
			if(tmpIndex==-1)
			{
				Map tmpData=getAssociatedMapType();
				String tmpKey=key.toString();
				heirarchyList.add(tmpKey);
				
				index=parseJsonBody(content, index,tmpData);
				jsonData.put(tmpKey, tmpData);

				key.delete(0, key.length());

				index=skipWhiteSpace(content, index,JSONConstants.COMMAORCCB);
				if(content.charAt(index)==JSONConstants.COMMA || content.charAt(index)==JSONConstants.CCBRACE)
					index++;
				else
				{
					JSONUtility.handleFailure(heirarchyList,key,"COMMA or Close Curly Brace is expected....@Position",String.valueOf(index));
				}
			}
			else
			{
				index=tmpIndex;
			}
			
			int braceInd=verifyIfBracePresent(content,index);
			
			if(braceInd==-1)
				continue;
			
			return braceInd;
		}
	}
	
	private void validateKey(int index)
	{
		String keyValidator=getPattern(key.toString(),"keyValidator");
		
		if(keyValidator!=null)
		{
			SpecialValidator validator=CachedInstances.getInstance().getValidatorInstance(keyValidator);
			if(!validator.validate(key.toString(),heirarchyList))
			{
				JSONUtility.handleFailure(heirarchyList,key,"Key validation failed...json-block::",jsonBlockName,"\tKey::"+key+"\tValidator::",String.valueOf(keyValidator));
			}
			
			return;
		}
		
		String keyPattern=getPattern(key.toString(),"keyPattern");
		
		if(keyPattern!=null && !key.toString().matches(keyPattern))
		{
			JSONUtility.handleFailure(heirarchyList,key," is not matching the key pattern...@Position::",String.valueOf(index));
		}		
	}
	
	private int verifyIfBracePresent(String content, int index) {
		
		if(content.charAt(index-1)==JSONConstants.CCBRACE)
			return index;

		index=skipWhiteSpace(content, index, JSONConstants.KEYORCCB);
		
		if(content.charAt(index)==JSONConstants.CCBRACE)
			return index+1;
		
		return -1;
	}

	private int parseKey(String content,int index)
	{
		index=skipWhiteSpace(content, index, String.valueOf(JSONConstants.COLON));
		
		int tempindex=index;
		
		index=consumeKeyOrName(content, index, key, JSONConstants.COLON);
		
		if(isValidating)
			validateKey(tempindex);
		
		return index;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int parseValue(String content,int index,Map jsonData)
	{
		index=skipWhiteSpace(content, index,String.valueOf(JSONConstants.OCBRACE));

		String valueType=null;
		
		if(!isValidating)
		{
			valueType=identifyValueTypePattern(content,index);
		}
		else valueType=getPattern(key.toString(),"valueType");
		
		// process if body is the JSON
		if(valueType!=null && valueType.equalsIgnoreCase(JSONConstants.JSON))
		{
			if(content.charAt(index)==JSONConstants.OCBRACE)
				return -1;
			else
				JSONUtility.handleFailure(heirarchyList,key,"JSON_START_TOKEN is expected but not found...@Position::",String.valueOf(index));
		}
		else if(content.charAt(index)==JSONConstants.OCBRACE)
			JSONUtility.handleFailure(heirarchyList,key,"JSON_START_TOKEN is identified but not expected...@Position::",String.valueOf(index));
			
		// process if body is JSON Array		
		if(valueType!=null && valueType.equalsIgnoreCase(JSONConstants.JSON_ARRAY))
		{
			if(content.charAt(index)==JSONConstants.JSON_ARRAY_START)
			{
				return jsonArrayTemplate(content, index,jsonData,true);
			}
			else
				JSONUtility.handleFailure(heirarchyList,key,"JSON_ARRAY_START_TOKEN is expected but not found...@Position::",String.valueOf(index));
		}
		else if(content.charAt(index)==JSONConstants.JSON_ARRAY_START)
			JSONUtility.handleFailure(heirarchyList,key,"JSON_ARRAY_START_TOKEN is identified but not expected...@Position::",String.valueOf(index));
		
		if(!isValidating && valueType.equalsIgnoreCase(JSONConstants.VALUE_DEFAULT))
			index=nonValidatingValueTemplate(content, index, JSONConstants.CCBRACE,value);
		else	
			index=determineTemplate(valueType,content,index,JSONConstants.CCBRACE);
		
		jsonData.put(key.toString(), value.toString());
		
		return index+1;		
	}
	
	private String identifyValueTypePattern(String content, int index) {
		switch(content.charAt(index)){
			case JSONConstants.OCBRACE: return JSONConstants.JSON;
			case JSONConstants.JSON_ARRAY_START: return JSONConstants.JSON_ARRAY;
			default: return JSONConstants.VALUE_DEFAULT;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int jsonArrayTemplate(String content,int index,Map jsonData,boolean skipCommaOrCurlyBrace)
	{
		List dataList=getAssociatedListType();
		String tmpKey=key.toString();
		int arrayIndex=0;
		boolean flag;
		for(;;)
		{
			index=skipWhiteSpace(content, index+1, JSONConstants.COMMAORCARRAY);
			/*
			 * making changes to support array index based validation - start
			 * 
			 */
			flag=false;
			heirarchyList.add(tmpKey);
			
			String valueType=null;
			
			if(!isValidating)
			{
				valueType=identifyValueTypePattern(content,index);
			}				
			else valueType=getPattern(String.valueOf(arrayIndex), "valueType");

			if((valueType!=null && valueType.equalsIgnoreCase("JSON_ARRAY")) || content.charAt(index)==JSONConstants.JSON_ARRAY_START) 
				JSONUtility.handleFailure(heirarchyList,key,"JSON_ARRAY cannot be the direct child of another JSON_ARRAY...@Position::",String.valueOf(index));
		
		
			
			heirarchyList.remove(heirarchyList.size()-1);
			heirarchyList.add(new StringBuilder(tmpKey).append(JSONConstants.JSON_ARRAY_START).append(arrayIndex).append(JSONConstants.JSON_ARRY_END).toString());
			
			
			if(valueType!=null && valueType.equalsIgnoreCase("JSON"))
			{
				if(content.charAt(index)==JSONConstants.OCBRACE)
				{
					Map dataMap=getAssociatedMapType();
					index=parseJsonBody(content,index,dataMap);
					dataList.add(dataMap);
					index=skipWhiteSpace(content, index, String.valueOf(JSONConstants.COMMA));
					flag=true;
				}
				else
					JSONUtility.handleFailure(heirarchyList,key,"JSON_START_TOKEN is expected but not found...@Position::",String.valueOf(index));
			}
			else if(content.charAt(index)==JSONConstants.OCBRACE)
				JSONUtility.handleFailure(heirarchyList,key,"JSON_START_TOKEN is identified but not expected...@Position::",String.valueOf(index));
			
			/*
			 * making changes to support array index based validation - end
			 * 
			 */
			if(!flag)
			{
				value.delete(0, value.length());
				key.delete(0,key.length());
				
				if(!isValidating && valueType.equalsIgnoreCase(JSONConstants.VALUE_DEFAULT))
					index=nonValidatingValueTemplate(content, index, JSONConstants.JSON_ARRY_END,value);
				else
					index=determineTemplate(valueType,content,index,JSONConstants.JSON_ARRY_END);
				
				dataList.add(value.toString());
				heirarchyList.remove(heirarchyList.size()-1);
			}
			
			arrayIndex++;
			
			if(content.charAt(index)!=JSONConstants.COMMA && content.charAt(index)!=JSONConstants.JSON_ARRY_END)
				JSONUtility.handleFailure(heirarchyList,key,", or ] is expected...@Position::",String.valueOf(index));
			
			if(content.charAt(index)==JSONConstants.JSON_ARRY_END)
				break;

		}
		
		jsonData.put(tmpKey, dataList);
		
		if(skipCommaOrCurlyBrace)
		{
			index=skipWhiteSpace(content, index+1, JSONConstants.COMMAORCCB);
			if(content.charAt(index)!=JSONConstants.COMMA && content.charAt(index)!=JSONConstants.CCBRACE)
				JSONUtility.handleFailure(heirarchyList,key,", or } is expected...@Position::",String.valueOf(index));
		}
		
		return index+1;
	}
	
	private int determineTemplate(String valueType,String content,int index,char expected)
	{
		if(valueType==null || valueType.equals(JSONConstants.STRING_LITERAL))
		{
			index=stringLiteralTemplate(content,index,expected,true,value);
		}
		else if(valueType.equals(JSONConstants.STRING))
		{
			index=stringTemplate(content, index, expected,value);
		}
		else if(valueType.equals(JSONConstants.INTEGER))
		{
			index=numberTemplate(content,index,expected,false,value);
		}
		else if(valueType.equals(JSONConstants.DOUBLE))
		{
			index=numberTemplate(content,index,expected,true,value);
		}
		else if(valueType.equals(JSONConstants.BOOLEAN))
		{
			int tmpindex=index;
			index=stringTemplate(content, index, expected,value);
			String tmp=value.toString();
			if(!tmp.equalsIgnoreCase(JSONConstants.TRUE) && !tmp.equalsIgnoreCase(JSONConstants.FALSE))
				JSONUtility.handleFailure(heirarchyList,key,"Boolean value is expected...@Position::",String.valueOf(tmpindex));
		}
		else if(valueType.equals(JSONConstants.NULL))
		{
			int tmpindex=index;
			index=stringTemplate(content, index, expected,value);
			String tmp=value.toString();
			if(!tmp.equalsIgnoreCase(JSONConstants.NULL))
				JSONUtility.handleFailure(heirarchyList,key,"Null is expected as value...@Position::",String.valueOf(tmpindex));
		}
		else
			JSONUtility.handleFailure(heirarchyList,key,"Unexpected value type is configured..."+valueType);	
		
		return index;
	}
	
	private int stringLiteralTemplate(String content,int index,final char expected,final boolean flag,StringBuilder temp)
	{
		int tempindex=index;
		
		boolean valueFound=false;

		index=skipWhiteSpace(content, index,String.valueOf(JSONConstants.DOUBLEQUOTES));
		if(content.charAt(index)!=JSONConstants.DOUBLEQUOTES && content.charAt(index)!=JSONConstants.SINGLE_QUOTE)
			JSONUtility.handleFailure(heirarchyList,key,"Quote is expected...@Position::",String.valueOf(index));
		
		char finalType=content.charAt(index);
		
		index++;
		
		label:
		for(;;)
		{
			if(index>=maxLength)
			{
				JSONUtility.handleFailure(heirarchyList,key,"Quote/COMMA/"+expected+" is expected. Unexpected EOF...@Position::",String.valueOf(index));
			}	
			
			if(content.charAt(index)!=finalType)
			{
				temp.append(content.charAt(index++));
				valueFound=true;
				continue label;				
			}
			else
			{
				if(!backtraceToVerifyIfEscaped(content,index,JSONConstants.BACKSLASH))
				{
					index=skipWhiteSpace(content, index+1, String.valueOf(JSONConstants.PLUS));
					
					if(content.charAt(index)==JSONConstants.PLUS)
					{
						index=stringLiteralTemplate(content, index+1, expected,flag,temp);
						return index;
					}
				}
				else 
				{
					index++;
					continue label;
				}
			}
			
			if((flag && content.charAt(index)==JSONConstants.COMMA) || content.charAt(index)==expected)
				break;
			else
				JSONUtility.handleFailure(heirarchyList,key,flag?"COMMA or ":JSONConstants.EMPTYSTR,String.valueOf(expected)," is expected. but found ",String.valueOf(content.charAt(index)),"...@Position::",String.valueOf(index));
			
			index++;
		}
		
		if(!valueFound)
			JSONUtility.handleFailure(heirarchyList,key,flag?"Value":JSONConstants.KEYORNAME," is expected but found empty...@Position::",String.valueOf(index));
		
		if(flag)
			validateValue(tempindex,temp);
	
		return index;
	}
	
	private int numberTemplate(String content,int index,final char expected,final boolean isDouble,StringBuilder temp)
	{
		int tempindex=index;
		boolean valueFound=false;
		boolean isDecimal=false;
		
		index=skipWhiteSpace(content, index, JSONConstants.VALUE);

		
		if(content.charAt(index)==JSONConstants.HYPHEN)
		{
			temp.append(JSONConstants.HYPHEN);
			index=skipWhiteSpace(content, index+1, JSONConstants.VALUE);	
		}
		
		char ch;
		label:
		for(;;)
		{
			if(index>=maxLength)
			{
				JSONUtility.handleFailure(heirarchyList,key,"COMMA or ",String.valueOf(expected)," is expected. Unexpected EOF...@Position::",String.valueOf(index));
			}				
			
			if(Character.isDigit(content.charAt(index)))
			{
				valueFound=true;
				temp.append(content.charAt(index++));
				continue label;
			}
			else if(content.charAt(index)=='.')
			{
				if(isDecimal)
					JSONUtility.handleFailure(heirarchyList,key,"Decimal Point is not allowed multiple times in the number...@Position::",String.valueOf(index));
				temp.append(content.charAt(index++));
				isDecimal=true;
			}
			else
			{
				ch=content.charAt(index);
				break;
			}
		}
		
		index=skipWhiteSpace(content, index, String.valueOf(JSONConstants.COMMA));
		
		if(content.charAt(index)!=JSONConstants.COMMA && content.charAt(index)!=expected)
			JSONUtility.handleFailure(heirarchyList,key,"Illegal character '",String.valueOf(ch),"' is identified while parsing for NUMBER_TOKEN...@Position::",String.valueOf(index));
			
		if(!valueFound)
			JSONUtility.handleFailure(heirarchyList,key,"Value is expected but found empty...@Position::",String.valueOf(index));		
		
		if(isDouble && !isDecimal)
			JSONUtility.handleFailure(heirarchyList,key,"DOUBLE_NUM_TOKEN is expected.But found NUM_TOKEN...@Position::"+tempindex);
		
		if(isDecimal && !isDouble)
			JSONUtility.handleFailure(heirarchyList,key,"NUM_TOKEN is expected.But found DOUBLE_NUM_TOKEN...@Position::"+tempindex);
		
		
		validateValue(tempindex,temp);
	
		return index;
			
	}
	
	private int stringTemplate(String content,int index,final char expected,StringBuilder temp)
	{
		int tempindex=index;
		
		boolean valueFound=false;
		
		index=skipWhiteSpace(content, index, JSONConstants.VALUE);
		char ch;
		for(;;)
		{
			if(Character.isLetter(content.charAt(index)) || Character.isDigit(content.charAt(index)))
			{
				valueFound=true;
				temp.append(content.charAt(index));
			}
			else
			{
				ch=content.charAt(index);
				break;
			}
			
			if(++index>=maxLength)
			{
				JSONUtility.handleFailure(heirarchyList,key,"COMMA or '"+expected+"' is expected. Unexpected EOF...@Position::",String.valueOf(index));
			}					
		}
		
		index=skipWhiteSpace(content, index, String.valueOf(JSONConstants.COMMA));
		
		if(content.charAt(index)!=JSONConstants.COMMA && content.charAt(index)!=expected)
			JSONUtility.handleFailure(heirarchyList,key,"Illegal character '",String.valueOf(ch),"' is identified while parsing for STRING_TOKEN...@Position::",String.valueOf(index));
		
		if(!valueFound)
			JSONUtility.handleFailure(heirarchyList,key,"Value is expected but found empty...@Position::",String.valueOf(index));
		
		validateValue(tempindex,temp);
	
		return index;
	}
	
	private int nonValidatingValueTemplate(String content,int index,final char expected,StringBuilder temp)
	{
		index=skipWhiteSpace(content, index, JSONConstants.VALUE);
		
		if(content.charAt(index)==JSONConstants.DOUBLEQUOTES || content.charAt(index)==JSONConstants.SINGLE_QUOTE)
			return stringLiteralTemplate(content, index, expected, true, temp);
		
		boolean valueFound=false;
		boolean isDotPresent=false;
		boolean isNumber=true;
		boolean isNegative=false;
		int tempindex=index;
		
		if(content.charAt(index)==JSONConstants.HYPHEN)
		{
			isNegative=true;
			temp.append(JSONConstants.HYPHEN);
			index=skipWhiteSpace(content, index+1, JSONConstants.VALUE);
		}
		
		char ch;
		
		for(;;)
		{
			if(Character.isLetter(content.charAt(index)))
			{
				isNumber=false;
				valueFound=true;
				temp.append(content.charAt(index));
			}
			else if(Character.isDigit(content.charAt(index)))
			{
				valueFound=true;
				temp.append(content.charAt(index));
			}
			else if(content.charAt(index)=='.')
			{
				if(isDotPresent || !isNumber)
					JSONUtility.handleFailure(heirarchyList, key, "Invalid '.' is identified but not expected...@Position::",String.valueOf(index));
					
				temp.append(content.charAt(index));
				isDotPresent=true;
			}
			else 
			{	
				ch=content.charAt(index);
				break;
			}
			if(++index>=maxLength)
			{
				JSONUtility.handleFailure(heirarchyList,key,"COMMA or "+expected+" is expected. Unexpected EOF...@Position::",String.valueOf(index));
			}					
		}
		
		if(valueFound && !isNumber && isDotPresent)
			JSONUtility.handleFailure(heirarchyList, key, "Invalid VALUE_TOKEN...@Position::",String.valueOf(tempindex));
		
		if(isNegative && !isNumber)
			JSONUtility.handleFailure(heirarchyList, key, "Invalid VALUE_TOKEN...@Position::",String.valueOf(tempindex));
		
		index=skipWhiteSpace(content, index, String.valueOf(JSONConstants.COMMA));
		
		if(content.charAt(index)!=JSONConstants.COMMA && content.charAt(index)!=expected)
			JSONUtility.handleFailure(heirarchyList,key,"Illegal character '",String.valueOf(ch),"' is identified while parsing for VALUE_TOKEN...@Position::",String.valueOf(index));
		
		if(!valueFound)
			JSONUtility.handleFailure(heirarchyList,key,"Value is expected but found empty...@Position::",String.valueOf(index));
		
		return index;		
		
	}
	
	private void validateValue(int index, StringBuilder temp)
	{
		if(!isValidating)
			return;
		
		String valueValidator=getPattern(key.toString(),"valueValidator");
		
		if(valueValidator!=null)
		{
			SpecialValidator validator=CachedInstances.getInstance().getValidatorInstance(valueValidator);
			if(!validator.validate(temp.toString(),heirarchyList))
			{
				JSONUtility.handleFailure(heirarchyList,key,"Value validation failed...json-block::",jsonBlockName,"\tKey::",key.toString(),"\tValidator::",valueValidator);
			}
			
			return;
		}		
		
		String valuePattern=getPattern(key.toString(),"valuePattern");
		
		if(valuePattern!=null && !temp.toString().trim().matches(valuePattern))
		{
			JSONUtility.handleFailure(heirarchyList,key,temp.toString()," is not matching the value pattern...@Position::",String.valueOf(index));
		}		
	}
	
	private boolean backtraceToVerifyIfEscaped(String content,int index, final char token)
	{
		int tmpindex=index;
		for(;;)
		{
			if(Character.isWhitespace(content.charAt(--tmpindex)))
				continue;
			
			if(content.charAt(tmpindex)==token)
				return true;
			
			return false;
		}
	}
	
	private String getPattern(String key,String attribute)
	{
		StringBuilder sb=new StringBuilder();
		for(String s:heirarchyList)
		{
			sb.append(s).append(JSONConstants.SLASH);
		}
		
		int index=sb.length()-1;
		
		
		if(key.trim().equals(""))
		{
			if(sb.charAt(--index)==JSONConstants.JSON_ARRY_END)
			{
				for(;index>0;)
				{
					if(sb.charAt(--index)==JSONConstants.JSON_ARRAY_START)
					{
						index--;
						break;
					}
				}
			}
		}
		else
			index--;
		
		HashMap<String,String> patternMap=configHandler.getPatternMap(sb.substring(0,index+1));
		
		String pattern=patternMap.get(new StringBuilder(key).append(JSONConstants.TILDE_DELE).append(attribute).toString());
		
		if(pattern==null)
		{
			pattern=patternMap.get(new StringBuilder("default~~").append(attribute).toString());
		}
		
		return pattern;
	}

	private int consumeJsonBodyStartToken(String content,int index)
	{
		index=skipWhiteSpace(content, index,String.valueOf(JSONConstants.OCBRACE));
		
		if(index>=maxLength)
			JSONUtility.handleFailure(heirarchyList,key,"'{' is expected...@Position::",String.valueOf(index));
		
		if(content.charAt(index)!=JSONConstants.OCBRACE)
			JSONUtility.handleFailure(heirarchyList,key,"'{' is expected, Found ",String.valueOf(content.charAt(index)),"...@Position::",String.valueOf(index));
		
		tokenStack.push(JSONConstants.OCBRACE_TOKEN);
		
		return index+1;
	}

	private void verifyIfPatternMatches(String token, String pattern)
	{
		if(!token.matches(pattern))
			JSONUtility.handleFailure(heirarchyList,key,"'",token,"' doesn't match with pattern '",pattern,"' ...");
	}
	
	private int consumeExpectedWhiteSpace(String content,int index)
	{
		if(!Character.isWhitespace(content.charAt(index)))
		{
			JSONUtility.handleFailure(heirarchyList,key,"Whitespace expected...@Position::",String.valueOf(index));
		}
		
		return skipWhiteSpace(content,index+1,JSONConstants.NONWHITESPACE);
	}
	
	private int consumeKeyOrName(String content,int index,StringBuilder temp,final char token)
	{
		boolean isKeyFound=false;
		boolean underscore=false;
		
		index=skipWhiteSpace(content, index, JSONConstants.KEYORNAME);
		
		
		if(content.charAt(index)==JSONConstants.DOUBLEQUOTES || content.charAt(index)==JSONConstants.SINGLE_QUOTE)
		{
			index=stringLiteralTemplate(content, index, token, false,temp);			
		}
		else
		{
			/*To handle junk data japanese characters*/	
			if(!Character.isLetterOrDigit(content.charAt(index)) && content.charAt(index)!=JSONConstants.OCBRACE)
				index++;
			/*To handle junk data japanese characters*/	
			
			if(content.charAt(index)==JSONConstants.OCBRACE)
			{
				temp.append(JSONConstants.ROOT_SMALL);
				return index;
			}
			
			for(;;)
			{
				if(index>=maxLength)
				{
					JSONUtility.handleFailure(heirarchyList,key,String.valueOf(token)," is expected. Unexpected EOF...@Position::",String.valueOf(index));
				}				
				
				if(Character.isLetterOrDigit(content.charAt(index)))
				{
						temp.append(content.charAt(index));
						underscore=false;
						isKeyFound=true;
				}
				else if(content.charAt(index)==JSONConstants.UNDERSCORE)
				{
					if(underscore)
						JSONUtility.handleFailure(heirarchyList, temp, "Two UNDERSCOREs are not allowed immediately in key/name...");
					underscore=true;
					temp.append(JSONConstants.UNDERSCORE);
				}
				else break;
				
				++index;
			}
			
			if(!isKeyFound)
				JSONUtility.handleFailure(heirarchyList, temp, "Key/Name is expected...but found empty@Position::",String.valueOf(index));
			
			index=skipWhiteSpace(content, index, String.valueOf(token));
			
			if(content.charAt(index)!=token)
				JSONUtility.handleFailure(heirarchyList, temp, String.valueOf(token)," is expected...but found ",String.valueOf(content.charAt(index)),"@Position::",String.valueOf(index));
		}

		
		return index+1;
	}	
	
	private int consumeVarDeclarationIfAny(String content, int index)
	{
		if(index+2>=maxLength)
		{
			JSONUtility.handleFailure(heirarchyList,key,"Unexpected End Of File while looking for var declaration...@Position::",String.valueOf(index));
		}
		
		if(content.substring(index, index+3).equals(JSONConstants.VAR))
		{
			if(Character.isWhitespace(content.charAt(index+3)))
				index=consumeExpectedWhiteSpace(content, index+3);
		}
		
		return index;
	}
	
	private int skipWhiteSpace(String content, int index,final String token)
	{
		while(true)
		{
			if(index>=maxLength)
			{
				JSONUtility.handleFailure(heirarchyList,key,"Unexpected End Of File While looking for token '"+token+"' ...@Position::",String.valueOf(index));
			}
			
			if(!Character.isWhitespace(content.charAt(index)))
			{
				int tmpindex=index;
				index=processCommentsIfAny(content,index,token);
				
				if(index>tmpindex)
					continue;
					
				break;
			}
			
			index++;
		}
		
		return index;
	}
	
	private int processCommentsIfAny(String content,int index,String token)
	{
		if(content.charAt(index)==JSONConstants.SLASH)
		{
			int tmpindex=index;

			boolean isComments=false;
			boolean isMultiLineComment=false;
			
			for(;;)
			{
				if(++index>=maxLength)
					JSONUtility.handleFailure(heirarchyList,key,"Unexpected End Of File While looking for token '",token,"' ...@Position::",String.valueOf(tmpindex));
				
				if(isMultiLineComment)
				{
					if(content.charAt(index)==JSONConstants.SLASH)
					{
						if(backtraceToVerifyIfEscaped(content,index,JSONConstants.STAR))
							return index+1;
					}
					continue;
				}
				else if(isComments)
				{
					if(content.charAt(index)==JSONConstants.NEWLINE)
						return index+1;
					
					continue;
				}
				
				if(!Character.isWhitespace(content.charAt(index)))
				{
					if(content.charAt(index)==JSONConstants.SLASH)
						isComments=true;
					else if(content.charAt(index)==JSONConstants.STAR)
					{
						isMultiLineComment=true;
					}
					else
						JSONUtility.handleFailure(heirarchyList,key,"Invalid Token '/' while parsing JSON...@Position::",String.valueOf(tmpindex));
				}
			}
		}
		
		return index;
	}
	
	private int skipWhiteSpaceEOF(String content, int index)
	{
		while(true)
		{
			if(index>=maxLength)
			{	
				return -1;
			}
			
 			if(!Character.isWhitespace(content.charAt(index)))
					break;
			
			index++;
		}
		
		return index;
	}
}
