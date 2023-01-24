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
