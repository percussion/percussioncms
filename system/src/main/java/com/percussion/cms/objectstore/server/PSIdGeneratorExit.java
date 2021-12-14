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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.data.PSIdGenerator;
import com.percussion.data.PSSqlException;
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
