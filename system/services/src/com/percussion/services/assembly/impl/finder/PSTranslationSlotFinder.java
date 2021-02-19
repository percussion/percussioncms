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
package com.percussion.services.assembly.impl.finder;

import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getLocale;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.reorderItems;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.setSiteFolderId;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Find the items related to the source by a translation relationship
 * 
 * @author dougrand
 */
public class PSTranslationSlotFinder extends PSSlotContentFinderBase
{
   private static Log ms_log = LogFactory.getLog(PSTranslationSlotFinder.class);

   @Override
   protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
   {
      Set<ContentItem> rval = null;
      try
      {
         rval = assumeParent(sourceItem);
         if (rval == null)
         {
            rval = assumeChild(sourceItem);
         }
      }
      catch (Exception e)
      {
         String errMsg = "Problem retrieving items for slot id=" + slot.getGUID();
         ms_log.error(errMsg, e);
         throw new RuntimeException(errMsg, e);         
      }

      String orderby = getValue(selectors, ORDER_BY, null);
      String locale = getLocale(sourceItem, selectors);
      if (StringUtils.isNotBlank(orderby))
      {
         rval = reorderItems(rval, orderby, locale);
      }
      return rval;
   }

   /**
    * Get the translation parent for the given child, then get the children of
    * the parent and build a relationship set.
    * 
    * @param sourceItem
    * @return a set of zero or more slot items corresponding to the translations
    *         related to the given child relationship item
    * @throws PSAssemblyException if there's a problem retrieving the
    *            relationship information
    */
   private Set<ContentItem> assumeChild(IPSAssemblyItem sourceItem)
         throws PSAssemblyException
   {
      Set<ContentItem> rval = new HashSet<ContentItem>();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      try
      {
         PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();

         PSRelationshipFilter filter = new PSRelationshipFilter();
         PSLegacyGuid guid = (PSLegacyGuid) sourceItem.getId();
         PSLocator dep = new PSLocator(guid.getContentId(), guid.getRevision());
         filter.setDependent(dep);
         filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
         PSRelationshipSet relationships = proc.getRelationships(filter);
         if (relationships.size() > 0)
         {
            PSRelationship parentrel = (PSRelationship) relationships.get(0);
            PSLocator owner = parentrel.getOwner();
            IPSGuid parentguid = gmgr.makeGuid(owner);
            ContentItem sitem = new ContentItem(parentguid, null, 0);
            setSiteFolderId(sitem, false);
            rval.add(sitem);

            // Get translation children and combine parent and child information
            // into one set
            filter = new PSRelationshipFilter();
            filter.setOwner(owner);
            filter.limitToOwnerRevision(true);
            filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
            relationships = proc.getRelationships(filter);
            for (int i = 0; i < relationships.size(); i++)
            {
               PSRelationship rel = (PSRelationship) relationships.get(i);
               // Skip original item
               PSLocator rdep = rel.getDependent();
               if (rdep.getId() == dep.getId())
                  continue;
               IPSGuid childguid = gmgr.makeGuid(rel.getDependent());
               ContentItem slotItem = new ContentItem(childguid, null, 0);
               setSiteFolderId(slotItem, false);
               rval.add(slotItem);
            }
         }
      }
      catch (PSCmsException | PSNotFoundException e)
      {
         ms_log.error("Problem retrieving relationship information", e);
         throw new PSAssemblyException(IPSAssemblyErrors.FINDER_ERROR,
               "sys_TranslationContentFinder", e.getLocalizedMessage());
      }
      catch (PSSiteManagerException e)
      {
         ms_log.error("Problem retrieving site info", e);
         throw new PSAssemblyException(IPSAssemblyErrors.FINDER_ERROR,
               "sys_TranslationContentFinder", e.getLocalizedMessage());
      }

      return rval;
   }

   /**
    * Find child translation items assuming that the source item refers to the
    * parent.
    * 
    * @param sourceItem the source assembly item, never <code>null</code>
    * @return a set of slot items, or <code>null</code> if there are no
    *         related dependents
    * @throws PSAssemblyException
    */
   private Set<ContentItem> assumeParent(IPSAssemblyItem sourceItem)
         throws PSAssemblyException
   {
      Set<ContentItem> rval = new HashSet<ContentItem>();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      try
      {
         PSRelationshipProcessor proc = PSRelationshipProcessor.getInstance();

         PSLegacyGuid ownerguid = (PSLegacyGuid) sourceItem.getId();
         PSLocator owner = new PSLocator(ownerguid.getContentId(), ownerguid
               .getRevision());
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setOwner(owner);
         filter.limitToOwnerRevision(true);
         filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
         PSRelationshipSet relationships = proc.getRelationships(filter);
         if (relationships.size() == 0)
         {
            return null;
         }
         for (int i = 0; i < relationships.size(); i++)
         {
            PSRelationship rel = (PSRelationship) relationships.get(i);
            IPSGuid childguid = gmgr.makeGuid(rel.getDependent());
            ContentItem slotItem = new ContentItem(childguid, null, 0);

            try {
               setSiteFolderId(slotItem, false);
            } catch (PSNotFoundException e) {
               ms_log.warn(e.getMessage());
               //continue processing
            }
            rval.add(slotItem);
         }

      }
      catch (PSCmsException e)
      {
         ms_log.error("Problem retrieving relationship information", e);
         throw new PSAssemblyException(IPSAssemblyErrors.FINDER_ERROR,
               "sys_TranslationContentFinder", e.getLocalizedMessage());
      }
      catch (PSSiteManagerException e)
      {
         ms_log.error("Problem retrieving site info", e);
         throw new PSAssemblyException(IPSAssemblyErrors.FINDER_ERROR,
               "sys_TranslationContentFinder", e.getLocalizedMessage());
      }

      return rval;
   }

   /**
    * Even though this is based on the relationships engine, it is an autoslot
    */
   public Type getType()
   {
      return Type.AUTOSLOT;
   }

}
