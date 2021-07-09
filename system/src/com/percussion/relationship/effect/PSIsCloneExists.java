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
package com.percussion.relationship.effect;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSCloneAlreadyExistsException;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.util.Iterator;

/**
 * This effect runs in the context of
 * {@link com.percussion.relationship.IPSExecutionContext#RS_PRE_CLONE pre-clone}.
 * The purpose of this is to check if the item to be cloned already has clones
 * and the configuration allows creation of another clone. In this context we
 * have no current relationship or the originating relationship. The attempt()
 * and recover() methods are not used at all. Note that the test() method throws
 * a special {@link PSCloneAlreadyExistsException runtime exception} which
 * allows to set the details of an existing relationship that prevents the
 * cloning process.
 * 
 * @author RammohanVangapalli
 */
public class PSIsCloneExists extends PSEffect
{

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSEffect#test(java.lang.Object[], 
    * com.percussion.server.IPSRequestContext, com.percussion.relationship.
    * IPSExecutionContext, com.percussion.relationship.PSEffectResult)
    */
   public void test(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if (!context.isPreClone())
      {
         String[] args = {m_name, "pre clone"};
         result.setWarning(request.getUserLocale(),
            IPSExtensionErrors.ILLEGAL_EXECUTION_CONTEXT, args);
         return;
      }

      String contentid =
         request.getParameter(IPSHtmlParameters.SYS_CONTENTID, "");
      if (contentid.length() < 1)
      {
         Object[] args = { contentid, "null or empty" };
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.EXT_MISSING_HTML_PARAMETER_ERROR,
            args);
      }
      String relationshipType =
         request.getParameter(IPSHtmlParameters.SYS_RELATIONSHIPTYPE, "");
      if (relationshipType.length() < 1)
      {
         Object[] args = { relationshipType, "null or empty" };
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.EXT_MISSING_HTML_PARAMETER_ERROR,
            args);
      }
      PSRelationshipProcessor relProxy;
      PSRelationshipConfig config = null;
      try
      {
         relProxy = PSRelationshipProcessor.getInstance();
         config = relProxy.getConfig(relationshipType);
         if (config == null)
         {
            //TODO I18n
            throw new PSExtensionProcessingException(
               1000,
               "Relationship type '"
                  + relationshipType
                  + "' does not exist in the system.");
         }

         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setOwner(new PSLocator(contentid));
         filter.setName(relationshipType);

         if (config
            .getCategory()
            .equals(PSRelationshipConfig.CATEGORY_TRANSLATION))
         {
            String locale =
               request.getParameter(IPSHtmlParameters.SYS_LANG, "");
            if (locale.length() < 1)
            {
               Object[] args = { locale, "null or empty" };
               throw new PSExtensionProcessingException(
                  IPSExtensionErrors.EXT_MISSING_HTML_PARAMETER_ERROR,
                  args);
            }
            PSComponentSummaries summaries =
               relProxy.getSummaries(filter, false);
            Iterator iter = summaries.iterator();
            while (iter.hasNext())
            {
               PSComponentSummary element = (PSComponentSummary) iter.next();
               if (element.getLocale().equalsIgnoreCase(locale))
               {
                  throw new PSCloneAlreadyExistsException(
                     filter.getOwner(),
                     (PSLocator) element.getCurrentLocator(),
                     relationshipType);
               }
            }
            filter.setOwner(null);
            filter.setDependent(new PSLocator(contentid));
            summaries =
               relProxy.getSummaries(filter, false);
            iter = summaries.iterator();
            while (iter.hasNext())
            {
               PSComponentSummary element = (PSComponentSummary) iter.next();
               if (element.getLocale().equalsIgnoreCase(locale))
               {
                  PSComponentSummary depSumm =
                     PSPromote.getItemSummary(request, filter.getDependent());
                  throw new PSCloneAlreadyExistsException(
                     (PSLocator) element.getCurrentLocator(),
                     depSumm.getCurrentLocator(),
                     relationshipType);
               }
            }
         }
      }
      catch (PSCmsException e)
      {
         throw new PSExtensionProcessingException(
            e.getErrorCode(),
            e.getErrorArguments());
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[], 
    * com.percussion.server.IPSRequestContext, com.percussion.relationship.
    * IPSExecutionContext, com.percussion.relationship.PSEffectResult)
    */
   public void attempt(
      Object[] prams,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      //attempt() method is not applicable for this effect
   }

   /* (non-Javadoc)
    * @see com.percussion.relationship.IPSEffect#recover(java.lang.Object[], 
    * com.percussion.server.IPSRequestContext, com.percussion.relationship.
    * IPSExecutionContext, com.percussion.extension.
    * PSExtensionProcessingException, com.percussion.relationship.
    * PSEffectResult)
    */
   public void recover(
      Object[] params,
      IPSRequestContext request,
      IPSExecutionContext context,
      PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      //recover() method is not applicable for this effect
   }

}
