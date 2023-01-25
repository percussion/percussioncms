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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.data.PSIdGenerator;
import com.percussion.error.PSSqlException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.SQLException;

/**
 * This is a system exit used to allocate ids from the next number table.
 * See the description of processResultDocument for details of its functioning.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSIdGeneratorExit extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * See interface for description.
    *
    * @return Always <code>false</code>.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Makes a call to the id generator using a supplied lookup key. The
    * allocated ids are returned in a document that replaces the supplied one
    * w a new one of the form:
    * <code><pre>
    * &lt;rootelement key="lookup" firstId="100", count="count or less"&gt;
    * </pre></code>
    * Where 'lookup' is the lookup key used to find the next available ids,
    * firstId is the first of a series of 1 or more ids allocated for the
    * caller's use and count is the actual number of allocated ids.
    * <p>This exit expects 1 html parameter to be present and 1 to optionally
    * be present. A parameter called sys_lookupkey is required. It is passed
    * to the id generator. An optional parameter called sys_idcount specifies
    * how many ids to allocate. If not supplied, 1 is assumed. A maximum of
    * 1000 is allowed.
    *
    * @param params unused.
    *
    * @param request Guaranteed not <code>null</code> by interface.
    *
    * @param resultDoc The key, firstId and count attributes are added to the
    *    root element of this doc. If there is no root, PSXIdGenerator root
    *    is added. Guaranteed not <code>null</code> by interface.
    *
    * @return The supplied doc.
    *
    * @throws PSParameterMismatchException If the required html parameter
    *    is missing.
    *
    * @throws PSExtensionProcessingException If a sql error occurs while
    *    allocating the ids.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         String paramName = "sys_lookupkey";
         String key = request.getParameter(paramName, "").trim();
         if (key.length() == 0)
         {
            String[] args =
            {
               paramName,
               ""
            };
            throw new PSParameterMismatchException(
                  IPSCmsErrors.MISSING_HTML_PARAMETER, args);
         }

         String val = request.getParameter("sys_idcount", "");
         int count;
         try
         {
            if (null != val && val.trim().length() > 0)
               count = Integer.parseInt(val);
            else
               count = 1;
         }
         catch (NumberFormatException nfe)
         {
            count = 1;
         }

         int[] ids = PSIdGenerator.getNextIdBlock(key, count);
         Element root = resultDoc.getDocumentElement();
         if (null == root)
            root = PSXmlDocumentBuilder.createRoot(resultDoc, "PSXIdGenerator");
         root.setAttribute("key", key);
         root.setAttribute("firstId", ""+ids[0]);
         root.setAttribute("count", ""+ids.length);
         return resultDoc;
      }
      catch (SQLException se)
      {
         throw new PSExtensionProcessingException(
               IPSCmsErrors.SQL_EXCEPTION_WRAPPER,
               PSSqlException.getFormattedExceptionText(se));
      }
   }
}
