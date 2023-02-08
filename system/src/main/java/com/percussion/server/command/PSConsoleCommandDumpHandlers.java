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

import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * This class dumps the currently used server resources to the server console.
 */
public class PSConsoleCommandDumpHandlers extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param cmdArgs the argument string to use when executing this command, 
    *    may be <code>null</code>.
    */
   public PSConsoleCommandDumpHandlers(String cmdArgs)
   {
      super(cmdArgs);
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   <P>
    * The execution of this command results in the following XML document
    * structure:
    *   
    * @see IPSConsolCommand
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, 
         "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(doc, root, "command", ms_cmdName + 
         " " + m_cmdArgs);

      Locale loc;
      if (request != null)
         loc = request.getPreferredLocale();
      else
         loc = Locale.getDefault();
      
      return doc;
   }
   
   /**
    * The command entered in the server console to get the information produced
    * in this class.
    */
   final static String ms_cmdName = "dump handlers";
}

