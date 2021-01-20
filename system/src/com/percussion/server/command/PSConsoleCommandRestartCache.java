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

import com.percussion.error.PSErrorManager;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.cache.PSCacheException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the execution of 'restart cache' console command and
 * dumps the results as an XML document to the console.
 */
public class PSConsoleCommandRestartCache extends PSConsoleCommandCache
{
   /**
    * The constructor for this class. The command arguments are ignored for this
    * command.
    *
    * @param cmdArgs the argument string to use when executing this command, may
    * be <code>null</code> or empty.
    */
   public PSConsoleCommandRestartCache(String cmdArgs)
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
    *      &lt;ELEMENT PSXConsoleCommandResults   (command, CacheStatistics,
    *    resultCode, resultText)&gt;
    *
    *      &lt;--
    *    the command that was executed (includes the arguments)
    *      --&gt;
    *      &lt;ELEMENT command   (#PCDATA)&gt;
    *
    *      &lt;--
    *    See {@link PSConsoleCommandCache#getCacheStatistics getCacheStatistics}
    *    for description of <code>CacheStatistics</code> element. This
    *    represents the cache statistics before restarting.
    *      --&gt;
    *      &lt;ELEMENT CacheStatistics   (#PCDATA)&gt;
    *
    *      &lt;--
    *    the result code for the command execution
    *      --&gt;
    *      &lt;ELEMENT resultCode   (#PCDATA)&gt;
    *
    *      &lt;--
    *    the message text associated with the result code
    *      --&gt;
    *      &lt;ELEMENT resultText   (#PCDATA)&gt;
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

      Locale loc;
      if (request != null)
         loc = request.getPreferredLocale();
      else
         loc = Locale.getDefault();

      root.appendChild( getCacheStatistics( respDoc ) );

       try
       {
            restartCache();
       }
       catch(PSCacheException pscex)
       {
          PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode",
               Integer.toString(pscex.getErrorCode()));

            String termMsg = PSErrorManager.createMessage(
                IPSServerErrors.RCONSOLE_UNABLE_TO_EXECUTE_CACHE_COMMAND,
                   new Object[] {pscex.getLocalizedMessage( loc )}, loc);
            PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", termMsg);
            return respDoc;
       }

       PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode",
         String.valueOf(IPSServerErrors.RCONSOLE_CACHE_RESTARTED));

      String termMsg = PSErrorManager.getErrorText(
         IPSServerErrors.RCONSOLE_CACHE_RESTARTED, true, loc);
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", termMsg);

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
   public final static String ms_cmdName = "restart cache";
}
