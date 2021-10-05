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
package com.percussion.extension;

import com.percussion.cms.PSCmsException;
import com.percussion.data.PSConversionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.List;

/**
 * System extension used to validate the sys_title field for a Content 
 * Editor only. It determines that the value of the <code>sys_title</code> 
 * field will be unique if within a folder.
 */
public class PSValidateUniqueName implements IPSFieldValidator
{
   

   /**
    * Executes the UDF with the specified parameters and request context.
    *
    * @param params The parameter values of the exit, it is not used. It may be
    *    <code>null</code> or empty.
    *
    * @param request The current request context. It may not be 
    *    <code>null</code>.
    *
    * @return it returns <code>true</code> in the following conditions:
    *    <ul>
    *       <li>the item will not be updated or inserted.</li>
    *       <li>the item will not be in a folder.</li>
    *       <li>the value of sys_title field will be unique within a folder.</li>
    *       <li>the item of content type is unique within the system.
    *    </ul>
    * otherwise return <code>false</code>.
    */
   public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null.");
      boolean isProcessed = false;

        try
        {
            if (isValidScope(params, request))
            {

                String actionType = request.getParameter("DBActionType");
                if (actionType == null || !(actionType.equals("INSERT") || actionType.equals("UPDATE")))
                    return Boolean.TRUE; // skip validation if not insert or
                                         // update

                request.appendParameter("UniqueValidated", "yes");
                PSServerFolderProcessor.validateUniqueDepName(request, actionType.equals("INSERT"));

                isProcessed = true;
            }
        }
        catch (PSCmsException e)
        {
            return Boolean.FALSE;
        }
        return isProcessed;
   }   

   /**
    * Checks to see if  an item of its content type is unique (case insensitively) within the
    * system. If not then returns false.  
    * 
    * @param params The parameter values of the exit, it is not used. It may be
    *            <code>null</code> or empty.
    * @param request The current request context. It may not be
    *            <code>null</code>.
    * @return returns true if there is no duplicate item of its
    *         content type in the system.
    * @throws PSConversionException
    */
   private boolean isValidScope(Object[] params, IPSRequestContext request) throws PSConversionException
   {
       String title = request.getParameter(IPSHtmlParameters.SYS_TITLE);
       String contentTypeId = request.getParameter(IPSHtmlParameters.SYS_CONTENTTYPEID);
       String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
       boolean isValid = true;
       String scope = "";

       if ((params != null) && params.length == 1 && (params[0] != null))

       {
           scope = StringUtils.defaultString(params[0].toString());
       }

       try
       {
           if (scope.equalsIgnoreCase("ContentType"))
           {
              IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
                
               List<String> entries = mgr.findNodesByTitle(Long.parseLong(contentTypeId), title);
               if(entries.size() > 1)
               {
                  isValid = false;
               }
               else if(entries.size() == 1 && !entries.get(0).equals(contentid))
               {
                  isValid = false;
               }
           }
           else
           {
               isValid = true;
           }
       }
       catch (RepositoryException e)
       {
           isValid = false;
           String msg = "Could not get Content Type name for the  name the contentTypeId = " + contentTypeId;
           log.error(msg, e);

       }

       return isValid;
   }
   /* 
    * @see com.percussion.extension.IPSExtension#init(
    * com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   @SuppressWarnings("unused")
   public void init( IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      // no-op 
   }
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSValidateUniqueName.class);


}
