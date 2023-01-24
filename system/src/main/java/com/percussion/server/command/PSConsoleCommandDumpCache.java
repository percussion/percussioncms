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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the execution of 'dump cache' console command and 
 * dumps the results as an XML document to the console.
 */
public class PSConsoleCommandDumpCache extends PSConsoleCommandCache
{
   /**
    * The constructor for this class. The command arguments are ignored for this
    * command.
    *
    * @param cmdArgs   the argument string to use when executing   this command, may
    * be <code>null</code> or empty.
    */
   public PSConsoleCommandDumpCache(String cmdArgs)
   {
      super(cmdArgs);
   }
   
   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    *   <P>
    * The execution of this command results in the following XML document
    * structure:
    * <PRE><CODE>
    *    &lt;--
    *       The cache statistics element with each attribute referring to
    *       each kind of statistics. See {@link 
    *       PSConsoleCommandCache#getCacheStatistics getCacheStatistics} for
    *       description of <code>CacheStatistics</code> element.
    *      --&gt;
    *      &lt;ELEMENT PSXConsoleCommandResults   (command, CacheStatistics)&gt;
    *
    *      &lt;--
    *         the command that was executed 
    *      --&gt;
    *      &lt;ELEMENT command (#PCDATA)&gt;
    *            
    * </CODE></PRE>
    *
    * @param request the requestor object, may be <code>null</code>
    *
    * @return the result document, never <code>null</code>
    *
    * @throws PSConsoleCommandException   if an error occurs during execution
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {
      Document respDoc = getResultsDocument();
      Element root = respDoc.getDocumentElement(); 
      
      root.appendChild(getCacheStatistics( respDoc ));
      
      return respDoc;
   }
   
   //see super class method for description.
   @Override
   public String getCommandName()
   {
      return ms_cmdName;
   }
   
   /**
    * The command executed by this class.
    */
   public static final String ms_cmdName = "dump cache";
}
