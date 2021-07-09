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
package com.percussion.extensions.cms;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 2 parameters are expected: sys_originalPath and sys_newPath. All sites
 * (RXSITES) are scanned and any folder path that matches original
 * (case-insensitive) is updated to match the new path. The result doc indicates
 * success or failure and includes error info on failure (if success is false).
 * The result doc has the following DTD:
 * 
 * <pre>
 *  
 *  &lt;!ELEMENT ModifySiteDefsResults (Error?)&gt; 
 *  &lt;!ATTLIST ModifySiteDefsResults 
 *     success (true|false) #REQUIRED 
 *     &gt; 
 *  &lt;!ELEMENT Error (Message, StackTrace)&gt; 
 *  &lt;!ELEMENT Message (#PCDATA)&gt; 
 *  &lt;!ELEMENT StackTrace (#PCDATA)&gt; 
 *  
 * </pre>
 * 
 * @author paulhoward
 */
public class PSModifySiteDefs extends PSDefaultExtension implements
      IPSResultDocumentProcessor
{
   //see base class method for details
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * See class description for details.
    * 
    * @param None expected.
    * @param request Never <code>null</code>.
    * @param resultDoc Discarded and replaced with document generated in this
    *           exit.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
            "ModifySiteDefsResults");
      String errorMsg = null;
      StackTraceElement[] stack = null;
      try
      {
         String PARAM_NAME = "sys_originalPath";
         String origPath = request.getParameter(PARAM_NAME);
         if (origPath == null || origPath.trim().length() == 0)
         {
            //used just to end normal processing and pass error text
            throw new Exception("The original folder path is missing. "
                  + "It must be supplied via the " + PARAM_NAME + " parameter.");
         }

         PARAM_NAME = "sys_newPath";
         String newPath = request.getParameter(PARAM_NAME);
         if (newPath == null || newPath.trim().length() == 0)
         {
            //used just to end normal processing and pass error text
            throw new Exception("The new folder path is missing. "
                  + "It must be supplied via the " + PARAM_NAME + " parameter.");
         }

         final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         for (IPSSite site : smgr.loadSitesModifiable())
         {
            String existingRoot = site.getFolderRoot();
            if (existingRoot != null)
               existingRoot = existingRoot.replace('\\', '/');
            if (existingRoot == null || existingRoot.trim().length() == 0
                  || !existingRoot.equalsIgnoreCase(origPath))
            {
               continue;
            }
            
            site.setFolderRoot(newPath);
            smgr.saveSite(site);
         }
      }
      catch (Exception e)
      {
         errorMsg = e.getLocalizedMessage();
         stack = e.getStackTrace();
      }

      root.setAttribute("success", errorMsg == null ? "true" : "false");
      if (errorMsg != null)
      {
         PSCopySiteDef.createErrorResult(doc, root, errorMsg, stack);
      }

      return doc;
   }
}
