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

package com.percussion.redirect.data;

/***
 * Generic status object for returning responses from redirect services.
 * 
 * @author natechadwick
 *
 */
public class PSRedirectStatus {

	public final static String  SERVICE_OK = "Ok";
	public final static String  SERVICE_ERROR = "Error";
	public final static String  SERVICE_UNLICENSED = "Unlicensed";
	
	private String statusCode;
	
	/***
	 * Get the status code
	 * @return The redirect status code
	 */
	public String getStatusCode(){
		return statusCode;
	}
	
	/***
	 * Sets the Redirect Status Code
	 * @param code A valid status constant value 
	 */
	public void setStatusCode(String code){
		statusCode = code;
	}
	
	private String message;
	
	/***
	 * Get the message that goes with the status
	 * @return A message if any is available for the status
	 */
	public String getMessage(){
		return message;
	}
	
	/***
	 * Sets the message if available.  
	 * @param msg  The status message.  If null will be coverted to  an empty string.
	 */
	public void setMessage(String msg){
		if(msg == null) {
			msg = "";
		}
		
		message = msg;
	}
	
	/***
	 * Default constructor
	 */
	public PSRedirectStatus(){}
	
	/***
	 * Convenience constructor.
	 * @param code  A valid Status code
	 * @param msg A valid message, null values will be converted to empty string
	 */
	public PSRedirectStatus(String code, String msg){
		statusCode = code;
		
		if(msg == null) {
			msg = "";
		}
	
		message = msg;
	}
}
