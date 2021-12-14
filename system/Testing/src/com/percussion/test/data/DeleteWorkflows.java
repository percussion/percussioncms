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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.test.data;

import com.percussion.util.PSRemoteRequester;

import java.util.Map;

public class DeleteWorkflows
{
   /**
    * Usage: DeleteWorkflows lowerId upperId [server=localhost] [port=9992] [uid=admin1] [pw=demo]
    * 
    * param names are case-insensitive, [] params are optional, order doesn't
    * matter for optional, except they must appear after the ids, default values
    * shown, [params] w/ no value are ignored (they get the default)
    * 
    * @param args
    */
   public static void main(String[] args)
      throws Exception
   {
      final String usage = 
         "Usage: DeleteWorkflows lowerId upperId [server=localhost] [port=9992] [uid=admin1] [pw=demo]";
      if (args.length < 2)
      {
         throw new IllegalArgumentException(usage);
      }
      int lowerId = Integer.parseInt(args[0]);
      int upperId = Integer.parseInt(args[1]);
      
      if (lowerId <= 0 || upperId <= 0 || upperId < lowerId)
      {
         throw new IllegalArgumentException(
               "lowerId > 0, upperId > 0, upperId >= lowerId: lowerId=" 
               + lowerId + ", upperId=" + upperId);
      }

      String[] optionalArgs = new String[args.length > 2 ? args.length - 2 : 0];
      System.arraycopy(args, 2, optionalArgs, 0, args.length-2);
      Map<String, String> userParams = CreateWorkflows.extractArgs(optionalArgs, 
            usage);
      
      PSRemoteRequester req = new PSRemoteRequester(userParams.get("server"),
            Integer.parseInt(userParams.get("port")), -1);
      req.setCredentials(userParams.get("uid"), userParams.get("pw"));

      for (int id = lowerId; id <= upperId; id++)
      {
         System.out.println("Deleting workflow " + id);
         deleteWorkflow(req, id);
      }
      System.out.println("Finished");
   }

   private static void deleteWorkflow(PSRemoteRequester req, int id)
      throws Exception
   {
      String resource = "sys_wfEditorDelete/workflowdelete.xml";
      String[] paramArray =
      {
         "sys_componentname", "wf_all",
         "sys_pagename", "wf_all",
         "workflowid", String.valueOf(id),
         "DBActionType", "DELETE",
         "rxorigin", "wfhome"
      };
      CreateWorkflows.makeRequest(req, resource, paramArray);
   }
}
