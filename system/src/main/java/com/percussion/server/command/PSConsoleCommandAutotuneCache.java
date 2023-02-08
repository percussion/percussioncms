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

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.cache.PSAutotuneCache;
import com.percussion.server.cache.PSAutotuneCacheLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * Autotunes the ehcache.xml file. Currently not configurable with the current
 * ehcache version so must be read in as an XML file.
 * 
 * <br/>
 * 
 * @author chriswright
 *
 */
public class PSConsoleCommandAutotuneCache extends PSConsoleCommand
{

   /**
    * The constructor for this class.
    *
    * @param cmdArgs the argument string to use when executing this command
    *
    */
   public PSConsoleCommandAutotuneCache(String cmdArgs) throws PSIllegalArgumentException
   {
      super(cmdArgs);
   }

   /**
    * Execute the command specified by this object. The results are returned as
    * an XML document of the appropriate structure for the command.
    * <P>
    * The execution of this command results in the following XML document
    * structure:
    * 
    * <PRE>
    * <CODE>
    *      &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, resultText)&gt;
    * 
    *      &lt;--
    *         the command that was executed
    *      --&gt;
    *      &lt;ELEMENT command                     (#PCDATA)&gt;
    * 
    *      &lt;--
    *         the result code for the command execution
    *      --&gt;
    *      &lt;ELEMENT resultCode                  (#PCDATA)&gt;
    * 
    *      &lt;--
    *         the message text associated with the result code
    *      --&gt;
    *      &lt;ELEMENT resultText                  (#PCDATA)&gt;
    * </CODE>
    * </PRE>
    * 
    * @param request the requestor object
    *
    * @return the result document
    *
    * @exception PSConsoleCommandException if an error occurs during execution
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      try
      {
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();

         Element root = PSXmlDocumentBuilder.createRoot(respDoc, "PSXConsoleCommandResults");
         PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName + " " + m_cmdArgs);

         PSAutotuneCache cache = PSAutotuneCacheLocator.getAutotuneCache();
         cache.updateEhcache();

         PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode", "0");
         PSXmlDocumentBuilder.addElement(respDoc, root, "resultText",
               "The cache has been updated.  Please restart the server to apply the changes.");

         return respDoc;
      }
      catch (Exception e)
      {
         Locale loc;
         if (request != null)
            loc = request.getPreferredLocale();
         else
            loc = Locale.getDefault();

         String msg;
         if (e instanceof PSException)
            msg = ((PSException) e).getLocalizedMessage(loc);
         else
            msg = e.getMessage();

         Object[] args = {(ms_cmdName + " " + m_cmdArgs), msg};
         throw new PSConsoleCommandException(
               IPSServerErrors.RCONSOLE_EXEC_EXCEPTION, args);
      }
   }

   /**
    * Allow package members to see our command name
    */
   private static final String ms_cmdName = "autotune cache";
   
   static String getPropName() {
      return PSConsoleCommandAutotuneCache.class.getSimpleName();
  }
}
