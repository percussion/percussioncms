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
 * 3 html parameters are expected: sys_originalSiteName, sys_newSiteName and
 * sys_newFolderPath. A search is done looking for a site whose name matches the
 * original supplied (case-insensitive). Once found, this site is copied and the
 * site name and folder path are set from the supplied values before saving the
 * copy. The variables(Assembler Properties) of the original site will be copied
 * to the new site. The result doc indicates success or failure and includes
 * error info on failure (if success is false). If successful the result
 * includes the source site id and the new copied site id in the result. The
 * result doc has the following DTD:
 * 
 * <pre>
 * 
 *  
 *     &lt;!ELEMENT CopySiteDefResults (Error?)&gt; 
 *     &lt;!ATTLIST CopySiteDefResults 
 *        success (true|false) #REQUIRED
 *        sourceSiteId CDATA #IMPLIED 
 *        copySiteId CDATA #IMPLIED 
 *        &gt; 
 *     &lt;!ELEMENT Error (Message, StackTrace)&gt; 
 *     &lt;!ELEMENT Message (#PCDATA)&gt; 
 *     &lt;!ELEMENT StackTrace (#PCDATA)&gt; 
 *   
 *  
 * </pre>
 * 
 * @author paulhoward
 */
public class PSCopySiteDef extends PSDefaultExtension
      implements
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
    * @param params none expected.
    * @param request Never <code>null</code>.
    * @param resultDoc Discarded and replaced with document generated in this
    *    exit.
    */
   public Document processResultDocument(
      @SuppressWarnings("unused") Object[] params,
      IPSRequestContext request, @SuppressWarnings("unused") Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "CopySiteDefResults");
      boolean success = false;
      String errorMsg = null;
      StackTraceElement[] stack = null;
      String newSiteName = null;
      try
      {
         String PARAM_NAME = "sys_originalSiteName";
         String origSiteName = request.getParameter(PARAM_NAME);
         if (origSiteName == null || origSiteName.trim().length() == 0)
         {
            //used just to end normal processing and pass error text
            throw new Exception("The original site name is missing. "
                  + "It must be supplied via the " + PARAM_NAME + " parameter.");
         }

         PARAM_NAME = "sys_newSiteName";
         newSiteName = request.getParameter("sys_newSiteName");
         if (newSiteName == null || newSiteName.trim().length() == 0)
         {
            //used just to end normal processing and pass error text
            throw new Exception("The new site name is missing. "
                  + "It must be supplied via the " + PARAM_NAME + " parameter.");
         }

         PARAM_NAME = "sys_newFolderPath";
         String newFolderPath = request.getParameter("sys_newFolderPath");
         if (newFolderPath == null || newFolderPath.trim().length() == 0)
         {
            //used just to end normal processing and pass error text
            throw new Exception("The new folder path is missing. "
                  + "It must be supplied via the " + PARAM_NAME + " parameter.");
         }

         final IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
         IPSSite srcSite = smgr.loadSite(origSiteName);
         IPSSite newSite = smgr.createSite();
         newSite.copy(srcSite);
         newSite.setName(newSiteName);
         newSite.setFolderRoot(newFolderPath);
         smgr.saveSite(newSite);

         root.setAttribute("sourceSiteId", srcSite.getGUID().getUUID() + "");
         root.setAttribute("copySiteId", newSite.getGUID().getUUID() + "");
         success = true;
      }
      catch (Exception e)
      {
         errorMsg = e.getLocalizedMessage();
         stack = e.getStackTrace();
      }

      root.setAttribute("success", success ? "true" : "false");
      if (errorMsg != null)
         createErrorResult(doc, root, errorMsg, stack);

      return doc;
   }

   /**
    * Builds a doc fragment and adds it to the supplied document. The fragment
    * follows the &lt;Error&gt; element in the dtd as specified in the class
    * description.
    * 
    * @param doc Assumed not <code>null</code>.
    * @param parent The node to which the generated fragment will be added.
    *           Assumed not <code>null</code>.
    * @param message Will be set as the content of the &lt;Message&gt; element.
    *           May be <code>null</code> or empty, in which case the message
    *           will say "None available".
    * @param stack Will be set as the content of the &lt;StackTrace&gt; element.
    *           May be <code>null</code> or empty.
    */
   static void createErrorResult(Document doc, Element parent, String message,
         StackTraceElement[] stack)
   {
      Element error = PSXmlDocumentBuilder
            .addEmptyElement(doc, parent, "Error");
      PSXmlDocumentBuilder.addElement(doc, error, "Message", message == null
            ? "None available"
            : message);

      String stackText = "";
      if (stack != null)
      {
         for (int i = 0; i < stack.length; i++)
         {
            StackTraceElement element = stack[i];
            stackText += element.toString();
            stackText += "\r\n";
         }
      }
      PSXmlDocumentBuilder.addElement(doc, error, "StackTrace", stackText);
   }
}
