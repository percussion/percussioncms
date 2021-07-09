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
   public String getCommandName()
   {
      return ms_cmdName;
   }
   
   /**
    * The command executed by this class.
    */
   public final static String ms_cmdName = "dump cache";
}
