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
