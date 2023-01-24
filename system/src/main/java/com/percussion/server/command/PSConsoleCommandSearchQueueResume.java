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
package com.percussion.server.command;

import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import org.w3c.dom.Document;

/**
 * Command to resume the queue processing if it is paused.  Noop if
 * queue is not paused.
 * 
 */
public class PSConsoleCommandSearchQueueResume extends PSConsoleCommand
{

   /**
    * The constructor for this class. The command arguments are ignored for this
    * command.
    * 
    * @param cmdArgs the argument string to use when executing this command, may
    * be <code>null</code> or empty.
    */
   public PSConsoleCommandSearchQueueResume(String cmdArgs)
   {
      super(cmdArgs);
   }

   //see base class   
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document doc;
      doc = PSConsoleCommandShowStatusSearch.getDisabledDoc(request, 
         getCommandName());
      if (null != doc)
         return doc;
      String result = PSSearchIndexEventQueue.getInstance().resume() ? "Indexer was resumed" : "Indexer was not resumed";
      doc = getResultsDocument(request, getCommandName(), 
            IPSServerErrors.RAW_DUMP, 
            new Object[] {result});
      return doc;
   }
   
   // see base class
   protected String getCommandName()
   {
      return COMMAND_NAME;
   }
   
   /**
    * The full command that launched this handler. 
    */
   private final static String COMMAND_NAME = "search queue resume";
   

}
