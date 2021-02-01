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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.relationship.effect;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSProcessorProxy;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This effect typically is attached to {@link com.percussion.design.objectstore.PSRelationshipConfig#CATEGORY_TRANSLATION translation}
 * category of relationships.
 * <p>
 * The newly translated object (item or folder) is attached to all parent 
 * folders. Parent folders are determined as follows:
 * <ol>
 *    <li>
 *    Collect all parent folders of the translated object where the folder 
 *    is the owner of the translation relationship and the translation 
 *    relationship name matches the name of the currently processed translation 
 *    relationship.
 *    </li>
 *    <li>
 *    Collect all parent folders of the translated object where the folder 
 *    is the dependent of the translation relationship and the translation 
 *    relationship name matches the name of the currently processed translation 
 *    relationship.
 *    </li>
 *    <li>
 *    Determine the locale through the dependent of the current translation 
 *    relationship.
 *    </li>
 *    <li>
 *    From all collected parent folders weed out the ones which do not match 
 *    the determined locale.
 *    </li>
 * </ol>
 * <p>
 * The translated object will be attached to all translated parent folders if
 * found or to all original parent folders for which no translated parent 
 * folder was found. If the original object is not attached to any folder, the 
 * translated object will not be attached to any folder.
 * <p>
 * This effect will be active for he following situations only:
 * <ol>
 *    <li>
 *    The execution context must be {@link com.percussion.relationship.IPSExecutionContext#RS_PRE_CONSTRUCTION}
 *    </li>
 *    <li>
 *    The originating relationship and the current relationship both must be 
 *    of {@link PSRelationshipConfig#CATEGORY_TRANSLATION translation}
 *    </li>
 *    <li>
 *    The originating relationshipship's owner must be a Folder
 *    </li>
 * </ol>
 * 
 * @author RammohanVangapalli
 */
public class PSAttachTranslatedFolder extends PSEffect
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.relationship.IPSEffect#test(java.lang.Object[],
    * com.percussion.server.IPSRequestContext,
    * com.percussion.relationship.IPSExecutionContext,
    * com.percussion.relationship.PSEffectResult)
    */
   @SuppressWarnings("unused")
   @Override
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      if(!context.isPreConstruction())
      {
         result.setWarning(
            "This effect is active only during relationship construction");
         return;
      }
      
      PSRelationship originatingRel = context.getOriginatingRelationship();
      if (!originatingRel.getConfig().getCategory().equals(
         PSRelationshipConfig.CATEGORY_TRANSLATION))
      {
         result.setWarning(
            "This effect is active only if the originating relationship is " +
            "of category '" + PSRelationshipConfig.CATEGORY_TRANSLATION + "'");
         return;
      }
      
      PSRelationship currentRel = context.getCurrentRelationship();
      if (!currentRel.getConfig().getCategory().equals(
         PSRelationshipConfig.CATEGORY_TRANSLATION))
      {
         result.setWarning(
            "This effect is active only if the current relationship is " +
            "of category '" + PSRelationshipConfig.CATEGORY_TRANSLATION + "'");
         return;
      }
      
      try
      {
         PSLocator origOwner = originatingRel.getOwner();
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary summary = cms.loadComponentSummary(
            origOwner.getId());

         if (summary.getType() == PSCmsObject.TYPE_FOLDER && 
            (originatingRel.getOwner().getId() != 
               currentRel.getOwner().getId()))
         {
            result.setWarning("This effect is active only if the originating " +
               "relationship's parent is a Folder and the owners of current " +
               "and originating relationships have he same contentid");
            return;
         }
         
         PSLocator depLocator = currentRel.getDependent();
         PSServerFolderProcessor relProxy = PSServerFolderProcessor.getInstance();
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setDependent(depLocator);
         filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         PSComponentSummaries folderParents = relProxy.getSummaries(filter,
            true);
         if (folderParents.size() > 0)
         {
            result.setWarning("Folder is already attached");
            return;
         }
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         result.setError(e);
      }
      
      result.setSuccess();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[],
    * com.percussion.server.IPSRequestContext,
    * com.percussion.relationship.IPSExecutionContext,
    * com.percussion.relationship.PSEffectResult)
    */
   @SuppressWarnings({"unused","unchecked"})
   @Override
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      PSRelationship currentRel = context.getCurrentRelationship();
      PSLocator owner = currentRel.getOwner();
      try
      {
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         /*
          * Locate the containing folders for the owner of the current 
          * relationship.
          */
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setDependent(owner);
         filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         PSComponentSummaries folderParents = relProxy.getSummaries(filter,
            true);
         
         /*
          * Do nothing if none exists. This is legal, for example one is 
          * creating a translation of an item that is not attached to any 
          * folder and hence is visible in a view.
          */
         if (folderParents.size() < 1)
         {
            /*
             * The original item is not related to any folder, do nothing, set 
             * status to success and return. 
             */
            result.setSuccess();
            return;
         }
         
         /*
          * Load the component summary for the current relationship's 
          * dependent to get the required locale.
          */
         PSLocator depLocator = currentRel.getDependent();
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary depSummary = cms.loadComponentSummary(
            depLocator.getId());
         String locale = depSummary.getLocale();

         /*
          * The current relationship's owner may belong to one or more folders. 
          * Find the folder's translation counterpart with the current 
          * dependent's locale.
          * First follow the translation relationship on the folder parent 
          * down, then follow the relationship up to find all translated 
          * parent folders.
          */
         List<PSLocator> originalParents = new ArrayList<PSLocator>();
         List<PSLocator> newParents = new ArrayList<PSLocator>();
         Iterator walker = folderParents.iterator();
         while (walker.hasNext())
         {
            PSComponentSummary summary = (PSComponentSummary) walker.next();
            PSLocator originaFolderLocator = (PSLocator) summary.getLocator();
            
            boolean foundTranslatedParent = false;
            
            // follow relationship down
            filter = new PSRelationshipFilter();
            filter.setOwner(originaFolderLocator);
            filter.setName(currentRel.getConfig().getName());
            if (updateNewParents(
               relProxy.getSummaries(filter, false).iterator(), locale, 
               newParents))
               foundTranslatedParent = true;

            // follow relationship up
            filter = new PSRelationshipFilter();
            filter.setDependent(originaFolderLocator);
            filter.setName(currentRel.getConfig().getName());
            if (updateNewParents(
               relProxy.getSummaries(filter, true).iterator(), locale, 
               newParents))
               foundTranslatedParent = true;
            
            if (!foundTranslatedParent)
               originalParents.add((PSLocator) summary.getLocator());
         }

         /*
          * Add all original parents for which we did not find a translated
          * counterpart.
          */
         newParents.addAll(originalParents);

         // create the new relationships
         List<PSLocator> list = new ArrayList<PSLocator>();
         list.add(depLocator);
         PSServerFolderProcessor folderProc = PSServerFolderProcessor.getInstance();
         for (int i=0; i<newParents.size(); i++)
         {
            PSLocator newParent = newParents.get(i);
            folderProc.addChildren(list, 
               newParent);
         }
         
         result.setSuccess();
      }
      catch (PSException e)
      {
         e.printStackTrace();
         result.setError(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.relationship.IPSEffect#recover(java.lang.Object[],
    * com.percussion.server.IPSRequestContext,
    * com.percussion.relationship.IPSExecutionContext,
    * com.percussion.extension.PSExtensionProcessingException,
    * com.percussion.relationship.PSEffectResult)
    */
   @SuppressWarnings("unused")
   @Override
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result) throws PSExtensionProcessingException
   {
      // noop
   }
   
   /**
    * Walks all supplied summaries and weeds out the ones which do not match
    * the supplied locale. The new parents locator list will be updated with 
    * all summaries that match the supplied locale.
    * 
    * @param summaries the summaries to walk, assumed not <code>null</code>, 
    *    may be empty.
    * @param locale the locale to filter by, assumed not <code>null</code>, 
    *    may be empty.
    * @param newParents the list of new parent locators which will be updated
    *    for all summaries that match the supplied locale, assumed not
    *    <code>null</code>, may be empty.
    * @return <code>true</code> if the supplied list of new parents was 
    *    updated, <code>false</code> otherwise.
    */
   private boolean updateNewParents(Iterator<PSComponentSummary> summaries, 
      String locale, List<PSLocator> newParents)
   {
      boolean foundTranslatedParent = false;

      while (summaries.hasNext())
      {
         PSComponentSummary summary = summaries.next();
         if (summary.getLocale().equals(locale))
         {
            newParents.add((PSLocator) summary.getLocator());
            foundTranslatedParent = true;
         }
      }
      
      return foundTranslatedParent;
   }
}
