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
