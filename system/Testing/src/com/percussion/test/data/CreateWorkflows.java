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
package com.percussion.test.data;

import com.percussion.util.PSRemoteRequester;
import com.percussion.utils.xml.PSXmlUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CreateWorkflows
{
   /**
    * Usage:
    * java testdata.CreateWorkflows [server=localhost] [port=9992] [uid=admin1] [pw=demo] [basename=workflow] [count=1]
    * 
    * param names are case-insensitive, all are optional, order doesn't matter,
    * default values shown, params w/ no value are ignored (they get the default)
    * 
    * @param args
    */
   public static void main(String[] args)
      throws Exception
   {
      Map<String, String> userParams = extractArgs(args,
            "CreateWorkflows [server=localhost] [port=9992] [uid=admin1] [pw=demo] [basename=workflow] [count=1]");
      PSRemoteRequester req = new PSRemoteRequester(userParams.get("server"),
            Integer.parseInt(userParams.get("port")), -1);
      req.setCredentials(userParams.get("uid"), userParams.get("pw"));
      
      int count = Integer.parseInt(userParams.get("count"));
      for (int i=1; i <= count; i++)
      {
         String wfName = userParams.get("basename") + i;
         int wfId = createWorkflow(req, wfName);
         System.out.println("Created workflow: " + wfName + "(" + wfId + ")");
         int stateCount = 10;
         for (int j = 1; j <= stateCount; j++)
         {
            createState(req, wfId, j);
            System.out.println("  Created state: " + j);
         }
         
         //assume state ids are 1->n
         for (int j = 1; j < stateCount; j++)
         {
            createTransition(req, wfId, j, j+1);
            System.out.println("    Created transition: " + j);
         }
      }
      System.out.println("Finished");
   }
   
   static public int createWorkflow(PSRemoteRequester req, String wfName)
      throws Exception
   {
      String resource = "sys_wfEditor/NewWorkflow_comm.xml";
      String[] paramArray = 
      {
         "sys_componentname", "wf_all",
         "workflowid", "",
         "rxorigin", "newwf",
         "administrator", "Admin",
         "initialstate", "1",
         "description", "",
         "DBActionType", "INSERT",
         "requiredname", wfName
      };
      Document result = makeRequest(req, resource, paramArray);
      
      NodeList nl = result.getElementsByTagName("editstate");
      Element elem = ((Element) nl.item(0)); 
      String data = PSXmlUtils.getElementData(elem, "editstate", true);
      String key = "workflowid=";
      int pos = data.indexOf(key) + key.length();
      StringBuffer buf = new StringBuffer();
      for (int i = pos; i < data.length(); i++)
      {
         char c = data.charAt(i);
         if (c == '&')
            break;
         buf.append(c);
      }
      
      return Integer.parseInt(buf.toString());
   }
   
   static public void createState(PSRemoteRequester req, int wfId, int counter)
      throws Exception
   {
      String resource = "sys_wfEditor/UpdateState.xml";
      String[] paramArray = 
      {
         "workflowid", String.valueOf(wfId),
         "sys_componentname", "wf_all",
         "stateid", "",
         "rxorigin", "editstate",
         "requiredname", "state"+counter,
         "description", "", 
         "sortorder", String.valueOf(counter),
         "publishable", "n",
         "DBActionType", "Update" 
      };
      makeRequest(req, resource, paramArray);
   }
   
   static public void createTransition(PSRemoteRequester req, int wfId,
         int fromStateId, int toStateId) throws Exception
   {
      String resource = "sys_wfEditor/UpdateTransition.xml";
      String label = "transition" + fromStateId + "-" + toStateId;
      String[] paramArray = 
      {
         "sys_componentname", "wf_all",
         "workflowid", String.valueOf(wfId),
         "stateid", String.valueOf(fromStateId),
         "transitionid", "",
         "transitiontype", "0",
         "rxorigin", "edittrans",
         "requiredlabel", label,
         "description", "",
         "requiredtrigger", label,
         "tostate", String.valueOf(toStateId), 
         "approvaltype", "1",
         "requiredapprovals", "1",
         "commentrequired", "n",
         "defaulttransition", "n",
         "workflowactions", "",
         "transitionroles", "*ALL*",
         "DBActionType", "UPDATE"
      };
      makeRequest(req, resource, paramArray);
   }
   
   
   static Document makeRequest(PSRemoteRequester req, String resource,
         String[] paramArray)
      throws Exception
   {
      Map<String, String> params = new HashMap<String, String>();
      for (int i=0; i < paramArray.length; i+=2)
      {
         params.put(paramArray[i], paramArray[i+1]);
      }

      return req.getDocument(resource, params);
   }
   
   /**
    * Fills a map with all default values, then overwrites them with values
    * found in the supplied args. The rules outlined in the usage are followed.
    * 
    * @param args Assumed not <code>null</code>.
    * @param usage If any problems w/ args, an exception is thrown with this
    * text as the exception text.
    * @return Never <code>null</code>, always has all parameters as specified
    * in usage.
    */
   static Map<String, String> extractArgs(String[] args, String usage)
   {
      Map<String, String> result = new HashMap<String, String>();
      result.put("server", "localhost");
      result.put("port", "9992");
      result.put("uid", "admin1");
      result.put("pw", "demo");
      result.put("basename", "workflow");
      result.put("count", "1");
      
      for (String arg : args)
      {
         String[] param = StringUtils.split(arg, '=');
         if (param.length == 2)
         {
            String normalName = param[0].toLowerCase();
            if (result.get(normalName) == null)
            {
               throw new IllegalArgumentException(usage);                 
            }
            result.put(normalName, param[1]);
         }
         else if (param.length > 2)
         {
            throw new IllegalArgumentException("Param values cannot contain '='");  
         }
      }
      
      return result;
   }
}
