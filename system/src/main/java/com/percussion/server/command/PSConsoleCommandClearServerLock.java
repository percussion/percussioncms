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

import com.percussion.server.IPSConsoleCommand;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServerLockManager;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class clears a server lock.
 */
public class PSConsoleCommandClearServerLock extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param cmdArgs the argument string to use when executing this command, 
    *    may be <code>null</code>.
    * 
    * @throws IllegalArgumentException if the <code>cmdArgs</code> are invalid.
    */
   public PSConsoleCommandClearServerLock(String cmdArgs)
   {
      super(cmdArgs);
      
      try 
      {
         m_lockId = Integer.parseInt(cmdArgs);
      }
      catch (NumberFormatException e) 
      {
         throw new IllegalArgumentException("Invalid lock id");
      }
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   
    * @see IPSConsoleCommand
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, 
         "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(doc, root, "command", ms_cmdName + 
         " " + m_cmdArgs);
      
      boolean cleared = PSServerLockManager.getInstance().releaseLock(m_lockId);
      String msg = cleared ? "Cleared lock with id: " : 
         "Lock not found.  Unable to clear lock with id: ";
      PSXmlDocumentBuilder.addElement(doc, root, "resultCode", "0");
      PSXmlDocumentBuilder.addElement(doc, root, "resultText", 
         msg + m_lockId);
      
      return doc;
   }
   
   /**
    * The command entered in the server console to perform the action executed
    * by this class.
    */
   final static String ms_cmdName = "clear server lock";
   
   /**
    * The lock id supplied in the command args to the ctor.
    */
   private int m_lockId;
}
