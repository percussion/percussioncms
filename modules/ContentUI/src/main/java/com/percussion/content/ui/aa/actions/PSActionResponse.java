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
