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

import com.json.parsers.JSONParser;

public interface ConfigHandler {
	
	void setStream(InputStream is);
	
	void setEncoding(String encoding);
	
	void setParserSelfInstance(JSONParser parser);
	
	void parse();

	HashMap<String, String> getPatternMap(String substring);
	
}
