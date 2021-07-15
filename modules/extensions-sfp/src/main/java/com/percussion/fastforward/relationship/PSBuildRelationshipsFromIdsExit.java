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
package com.percussion.fastforward.relationship;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSQueryCommandHandler;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.util.IPSHtmlParameters;

/**
 * This class is intended to be used as a post-exit on a content editor
 * resource, to create relationships in a specific slot with the request's
 * current item as the dependent and owners determined by a field's value.
 * 
 * @author James Schultz
 * @since 6.0
 */
public class PSBuildRelationshipsFromIdsExit extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   /**
    * @return <code>false</code> always.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Maintains active-assembly-style relationships between the request's
    * content item and a list of content items -- missing relationships will be
    * created, existing relationships with items not in the list will be
    * deleted. The request's content item is the dependent, and the list of
    * content items become its parents. The details of the relationship are
    * provided as parameters (relationship type, slot id, and variant id).
    * 
    * @param params three expected parameters, all required:
    * <ul>
    * <li>
    * <dt>fieldname</dt>
    * <dd>name of content editor field that contains desired parent ids</dd>
    * </li>
    * <li>
    * <dt>slotname</dt>
    * <dd>name of slot whose parents will be synchronized to match field value</dd>
    * </li>
    * <li>
    * <dt>templatename</dt>
    * <dd>name of template that will be assigned to created relationships</dd>
    * </li>
    * </ul>
    * @param request the current request context, not <code>null</code>.
    * @param resultDoc the request's result XML document. not modified by this
    *           exit. may be <code>null</code>.
    * @return the supplied <code>resultDoc</code>, without modification
    * @throws PSParameterMismatchException if any required parameter is blank.
    * @throws PSExtensionProcessingException if the assembly or relationship
    *            APIs report an error.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, final Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      /*
       * This exit should only act when editing an item, not when editing the
       * child, or performing other content editor operations like inline link
       * updates.
       */
      String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      String page = request.getParameter(
         PSContentEditorHandler.PAGE_ID_PARAM_NAME);
      String processInlineLink = request.getParameter(
         IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);
      
      if (page != null && command.equals(PSModifyCommandHandler.COMMAND_NAME) && 
         page.equals(String.valueOf(PSQueryCommandHandler.ROOT_PARENT_PAGE_ID)) &&
         (processInlineLink == null || !processInlineLink.equals("yes")))
      {
         String contentId = request
               .getParameter(IPSHtmlParameters.SYS_CONTENTID);
         if (StringUtils.isNumeric(contentId))
         {
            int cid = Integer.parseInt(contentId);

            Map<String, String> paramMap = getParameters(params);

            String fieldName = paramMap.get("fieldname");
            if (StringUtils.isBlank(fieldName))
            {
               throw new PSParameterMismatchException(
                     IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                     new Object[]
                     {"fieldname", "is a required parameter"});
            }

            String slotName = paramMap.get("slotname");
            if (StringUtils.isBlank(slotName))
            {
               throw new PSParameterMismatchException(
                     IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                     new Object[]
                     {"slotname", "is a required parameter"});
            }

            String templateName = paramMap.get("templatename");
            if (StringUtils.isBlank(templateName))
            {
               throw new PSParameterMismatchException(
                     IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                     new Object[]
                     {"templatename", "is a required parameter"});
            }

            try
            {
               Object[] fieldValues = request.getParameterList(fieldName);
               PSChildRelationshipBuilder builder = new PSChildRelationshipBuilder(
                     PSRelationshipProcessor.getInstance());
               builder.build(cid, fieldValues, slotName, templateName);
            }
            catch (PSAssemblyException e)
            {
               ms_log.error("Failure in assembly API", e);
               throw new PSExtensionProcessingException(0, e);
            }
            catch (PSCmsException e)
            {
               ms_log.error("Failure in relationship API", e);
               throw new PSExtensionProcessingException(0, e);
            }
         }
      }
      return resultDoc;
   }

   
   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Logger ms_log = LogManager.getLogger(PSBuildRelationshipsFromIdsExit.class);

}
