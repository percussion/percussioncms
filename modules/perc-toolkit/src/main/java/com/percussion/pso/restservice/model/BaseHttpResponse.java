/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
