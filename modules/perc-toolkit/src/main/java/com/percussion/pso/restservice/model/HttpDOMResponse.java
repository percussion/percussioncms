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
import org.dom4j.Document;

/***
 * Represents a Response from an request for the DOM of a remote resource.
 * @author natechadwick
 *
 */
public class HttpDOMResponse extends BaseHttpResponse{

	
	private Document document;
	
	/***
	 * Sets the DOM Document for the content returned in this response.
	 * @param document
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	/***
	 * Gets the DOM document for this response. 
	 * @return
	 */
	public Document getDocument() {
		return document;
	}
		
	/***
	 * Default constructor
	 */
	public HttpDOMResponse(){}
	

	/***
	 * Single Shot Constructor
	 * @param doc
	 * @param head
	 */
	public HttpDOMResponse(Document doc, Header[] head){
		this.setHeaders(head);
		this.document = doc;
	}


}
