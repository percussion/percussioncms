/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.error.PSErrorManager;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.cache.IPSCacheHandler;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class implements the execution of 'flush cache' console command and
 * dumps the results as an XML document to the console.
 */
public class PSConsoleCommandFlushCache extends PSConsoleCommandCache
{
   /**
    * The constructor for this class. If the <code>cmdArgs</code> is
    * <code>null
    * </code> or empty it flushes the entire cache when excuting
    * this command, otherwise flushes the cache based on the cache keys
    * supplied.
    * 
    * @param cmdArgs the argument string to use when executing this command, may
    *           be <code>null</code> or empty. The cache keys must be
    *           separated by ';'.
    */
   public PSConsoleCommandFlushCache(String cmdArgs) {
      super(cmdArgs);
   }

   /**
    * Execute the command specified by this object. The results are returned as
    * an XML document of the appropriate structure for the command.
    * <P>
    * The execution of this command results in the following XML document
    * structure:
    * 
    * <PRE><CODE> &lt;ELEMENT PSXConsoleCommandResults (command, resultCode,
    * resultText)&gt;
    * 
    * &lt;-- the command that was executed (includes the arguments) --&gt;
    * &lt;ELEMENT command (#PCDATA)&gt;
    * 
    * &lt;-- the result code for the command execution --&gt; &lt;ELEMENT
    * resultCode (#PCDATA)&gt;
    * 
    * &lt;-- the message text associated with the result code --&gt; &lt;ELEMENT
    * resultText (#PCDATA)&gt; </CODE></PRE>
    * 
    * @param request the requestor object, may be <code>null</code>
    * 
    * @return the result document, never <code>null</code>
    * 
    * @throws PSConsoleCommandException if an error occurs during execution
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document respDoc = getResultsDocument();
      Element root = respDoc.getDocumentElement();
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      boolean skip_others = false;
      
      Locale loc;
      if (request != null)
         loc = request.getPreferredLocale();
      else
         loc = Locale.getDefault();

      PSCacheManager mgr = PSCacheManager.getInstance();
      if (!mgr.hasCacheStarted())
      {
         throw new PSConsoleCommandException(
               IPSServerErrors.RCONSOLE_CACHE_ALREADY_STOPPED);
      }

      Map<String,String> keys = null;
      if (m_cmdArgs != null && m_cmdArgs.length() != 0)
      {
         // determine type and get handler
         String handlerType = null;
         String cmdArgs = "";
         StringTokenizer st = new StringTokenizer(m_cmdArgs,
               CACHE_ARG_SEPARATOR, false);

         IPSCacheHandler cacheHandler = null;
         if (st.hasMoreTokens())
         {
            // first arg must be the type
            handlerType = st.nextToken().trim();
            if (handlerType.equalsIgnoreCase("hibernate"))
            {
               cms.flushSecondLevelCache();
               skip_others = true;
            }
            else
            {
               cacheHandler = mgr.getCacheHandler(handlerType);
               if (cacheHandler == null)
               {
                  String typeList = "";
                  String delim = "";
                  Iterator types = mgr.getCacheTypes();
                  while (types.hasNext())
                  {
                     typeList += (delim + (String) types.next());
                     delim = ", ";
                  }

                  Object[] args =
                  {ms_cmdName, handlerType, typeList};
                  throw new PSConsoleCommandException(
                        IPSServerErrors.RCONSOLE_INVALID_SUBCMD, args);
               }
               else if (st.hasMoreTokens())
               {
                  cmdArgs = st.nextToken().trim();
               }

               // determine keys
               keys = new HashMap<>();

               // ask for delimiters since ";;;" is valid and won't return any
               // tokens
               // at all if we don't ask for delimiters
               st = new StringTokenizer(cmdArgs, CACHE_KEY_SEPARATOR, true);

               String[] keyNames = cacheHandler.getKeyNames();
               int requiredCacheKeys = keyNames.length;

               // first add null entry for all possible values
               for (int i = 0; i < requiredCacheKeys; i++)
                  keys.put(keyNames[i], null);

               // now overlay the supplied key values
               int iKey = 0;
               while (st.hasMoreTokens() && iKey < requiredCacheKeys)
               {
                  String token = st.nextToken().trim();

                  if (token.equals(CACHE_KEY_SEPARATOR))
                     iKey++; // will skip over empty values as well
                  else
                     keys.put(keyNames[iKey], token);
               }

               try
               {
                  cacheHandler.validateKeys(keys);
               }
               catch (PSSystemValidationException e)
               {
                  throw new PSConsoleCommandException(e.getErrorCode(), e
                        .getErrorArguments());
               }
            }
         }
      }
      else
      {
         cms.flushSecondLevelCache();
      }

      if (! skip_others)
      {
         flushCache(keys);
      }

      PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode", String
            .valueOf(IPSServerErrors.RCONSOLE_CACHE_FLUSHED));

      String termMsg = PSErrorManager.getErrorText(
            IPSServerErrors.RCONSOLE_CACHE_FLUSHED, true, loc);
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", termMsg);

      return respDoc;
   }

   // see super class method for description.
   public String getCommandName()
   {
      return ms_cmdName;
   }

   /**
    * The seperator used to delimit cache keys that are supplied as arguments to
    * the 'flush cache' command.
    */
   public final static String CACHE_KEY_SEPARATOR = ";";

   /**
    * The seperator used to separate the type from the keys when supplied as
    * arguments to the 'flush cache' command.
    */
   public final static String CACHE_ARG_SEPARATOR = " ";

   /**
    * The command executed by this class.
    */
   private final static String ms_cmdName = "flush cache";
}
