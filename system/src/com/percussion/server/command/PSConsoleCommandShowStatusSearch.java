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

import com.percussion.error.PSErrorManager;
import com.percussion.search.PSSearchEngine;
import com.percussion.search.PSSearchException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements the "show status search" command.
 *
 * @author paulhoward
 */
public class PSConsoleCommandShowStatusSearch extends PSConsoleCommand
{
   /**
    * Ctor required by framework.
    */
   public PSConsoleCommandShowStatusSearch(String args)
   {
      super(args);
   }

   /**
    * Queries the search engine for its status and appends the results to
    * the standard doc. The modified dtd is as follows:
    * <pre><code>
    *    &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, 
    *       resultText, SearchStatus)&gt;
    * </code></pre>
    * See {@link PSSearchEngine#getStatus()} for more details on the 
    * <code>SearchStatus</code> element.
    * 
    * @return A document conforming to and extending the format recommended 
    * in the interface. 
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      try
      {
         Document doc = getDisabledDoc(request, COMMAND_NAME);
         if (null != doc)
         {
            return doc;
         }
         doc = getResultsDocument(request, COMMAND_NAME, 0, null);
         Element root = doc.getDocumentElement();
         Element status = PSSearchEngine.getInstance().getStatus(doc);
         root.appendChild(status);
         return doc;
      }
      catch (PSSearchException e)
      {
         throw new PSConsoleCommandException(e.getErrorCode(), 
               e.getErrorArguments());
      }
   }

   /**
    * Checks if the full text search engine is enabled and if it isn't,
    * builds a document w/ appropriate messages.
    * <p>Note: this is made static so it is available to other search console
    * command handlers.
    * 
    * @param The request supplied to the <code>execute</code> method. May be
    * <code>null</code>.
    * 
    * @param command The full command, with or without args, never <code>
    * null</code> or empty.
    * 
    * @return <code>null</code> if full text search is enabled, otherwise
    * a document suitable for return from an <code>execute</code> method.
    */
   static Document getDisabledDoc(PSRequest req, String commandName)
   {
      if (PSServer.getServerConfiguration().getSearchConfig().isFtsEnabled())
      {
         return null;
      }
      if (null == commandName)
      {
         throw new IllegalArgumentException(
               "commandName cannot be null or empty");
      }
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(doc, root, "command", commandName);
  
      int resultCode = IPSServerErrors.RCONSOLE_FTS_DISABLED;
      PSXmlDocumentBuilder.addElement(doc, root, "resultCode", 
            String.valueOf(resultCode));

      Locale loc;
      if (req != null)
         loc = req.getPreferredLocale();
      else
         loc = Locale.getDefault();

      String termMsg = PSErrorManager.createMessage(
            resultCode, null, loc);
      PSXmlDocumentBuilder.addElement(doc, root, "resultText", termMsg);
      return doc;
   }

   /**
    * The full command that launched this handler. 
    * <p>Note: this should be passed in rather than having it in two places,
    * but the whole framework needs reworking so I'm not going to take that on
    * now. 
    */
   private final static String COMMAND_NAME = "status search";
}
