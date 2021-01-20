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
