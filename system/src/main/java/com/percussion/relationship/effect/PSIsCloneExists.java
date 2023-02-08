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
package com.percussion.relationship.effect;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
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
