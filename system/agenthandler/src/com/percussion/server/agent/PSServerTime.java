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

package com.percussion.server.agent;

import com.percussion.server.PSConsole;

import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * This is a test implementation of the interface <code>IPSAgent</code>.
 */
public class PSServerTime implements IPSAgent
{
   /*
    * Implementation of the method from <code>IPSAgent</code>
    */
   public void init(Element configData) throws PSAgentException
   {
      PSConsole.printMsg(PSAgentRequestHandler.HANDLER,
         "Server time agent is initialized...");
   }

   /*
    * Implementation of the method from <code>IPSAgent</code>
    */
   public void terminate()
   {
      PSConsole.printMsg(PSAgentRequestHandler.HANDLER,
         "Closing server time agent...");
   }

   /*
    * Implementation of the method from <code>IPSAgent</code>
    */
   public void executeAction(String action, Map params,
      IPSAgentHandlerResponse response)
   {
      String result = "";
      if(action.equalsIgnoreCase("servertime"))
      {
         result = new Date().toString();
      }
      else if(action.equalsIgnoreCase("systemprops"))
      {
         Enumeration names = System.getProperties().propertyNames();
         while( names.hasMoreElements() )
         {
            String pname = (String) names.nextElement();
            result += pname+": "+System.getProperty(pname) + "\r\n";
         }
      }
      else
      {
         result = "Unsupported action requested";
         response.setResponse(response.RESPONSE_TYPE_ERROR, result);
         return;
      }
      response.setResponse(response.RESPONSE_TYPE_RESULT, result);
   }
}
