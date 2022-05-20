/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.content.ui.aa.actions;

/**
 * A little container class to bundle up the information needed
 * for an action response.
 */
public class PSActionResponse
{

   public PSActionResponse(String responseData, int responseType)
   {
      m_responseData = responseData;
      m_responseType = responseType;
   }
   
   /**
    * Returns the response mime type string for the specified 
    * response type integer. If the response type integer
    * passed in is invalid then the response type of string
    * will be returned.
    * @return never <code>null</code> or empty.
    */
   public String getResponseTypeString()
   {
      if(m_responseType >= ms_responseStrings.length || m_responseType < 0)
         m_responseType = RESPONSE_TYPE_PLAIN;
      return ms_responseStrings[m_responseType];
   }
   
   /**
    * see {@link #m_responseData} for more detail.
    * @return Returns the responseData.
    */
   public String getResponseData()
   {
      return m_responseData;
   }
     
   /**
    * The response data string. May be <code>null</code> or
    * empty. Initialized in the ctor
    */
   private String m_responseData;
   
   /**
    * The response type that will be returned to the
    * caller. USe the RESPONSE_TYPE_XXX constants.
    * Defaults to the RESPONSE_TYPE_PLAIN type.
    */
   private int m_responseType = RESPONSE_TYPE_PLAIN;
      
   /**
    * Response type constant that represents the &quot;text/html&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_HTML = 0;
   
   /**
    * Response type constant that represents the &quot;text/plain&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_PLAIN = 1;
   
   /**
    * Response type constant that represents the &quot;text/xml&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_XML = 2;
   
   /**
    * Response type constant that represents the &quot;text/json&quot;
    * mime type.
    */
   public static final int RESPONSE_TYPE_JSON = 3;
   
   /**
    * Response string array.
    */
   private static String[] ms_responseStrings = new String[]
                                                           {
      "text/html",
      "text/plain",
      "text/xml",
      "text/json"
                                                           };
  
   

}
