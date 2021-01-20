/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
		if(msg == null)
			msg = "";
		
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
		
		if(msg == null)
			msg = "";
	
		message = msg;
	}
}
