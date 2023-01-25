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
