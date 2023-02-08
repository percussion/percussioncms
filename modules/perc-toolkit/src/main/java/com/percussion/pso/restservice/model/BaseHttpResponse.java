/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.pso.restservice.model;

import org.apache.commons.httpclient.Header;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseHttpResponse {

	private Item existingItem;
	private Header[] headers;
		
	/***
	 * Sets the HTTP Headers collection for this response.
	 * @param headers
	 */
	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	/***
	 * Gets the HTTP Headers collection for this response.
	 * @return
	 */
	public Header[] getHeaders() {
		return headers;
	}
	
	/***
	 * Will return the ETag header if it is set or the empty string.
	 * @return
	 */
	public String getETag(){
		String ret = "";
		
		for(int i = 0;i<headers.length;i++){
			if(headers[i].getName().equals("ETag")){
				ret = headers[i].getValue(); 
				break;
			}
		}
		
		return ret;
	}
	
	/***
	 * Will return the last modified header if it is set or the empty string.
	 * @return
	 */
	public String getLastModified(){
		String ret = "";
		
		for(int i = 0;i<headers.length;i++){
			if(headers[i].getName().equals("Last-Modified")){
				ret = headers[i].getValue(); 
				break;
			}
		}
		
		//If the last modified header isn't set, we'll use the date of the response.
		if(StringUtils.isEmpty(ret)){
			for(int i = 0;i<headers.length;i++){
				if(headers[i].getName().equals("Date")){
					ret = headers[i].getValue(); 
					break;
				}
			}
			
		}

		return ret;
	}

	public void setExistingItem(Item existingItem) {
		this.existingItem = existingItem;
	}

	public Item getExistingItem() {
		return existingItem;
	}	
}
