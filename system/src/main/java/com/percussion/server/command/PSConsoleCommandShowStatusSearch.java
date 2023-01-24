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

import com.percussion.error.PSErrorManager;
import com.percussion.search.PSSearchEngine;
import com.percussion.search.PSSearchException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;

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
