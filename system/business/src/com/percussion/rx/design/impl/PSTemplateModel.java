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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.rx.design.IPSAssociationSet.AssociationType;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManagerInternal;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;
import org.hibernate.AnnotationException;

public class PSTemplateModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      return loadTemplate(guid, true);
   }

   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return loadTemplate(guid, false);
   }

   @Override
   public Collection<String> findAllNames()
   {
      IPSTemplateService service = (IPSTemplateService) getService();
      try {
         return service.findAllTemplates().stream().map(t -> t.getName()).collect(Collectors.toList());
      } catch (PSAssemblyException e) {
         ms_logger.error("Cannot load template names",e);
         return Collections.emptyList();
      }
   }
   
   /**
    * Loads the readonly or modifiable template from the template service for
    * the supplied guid based on the readonly flag.
    * 
    * @param guid Must not be <code>null</code> and must be a template guid.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * template.
    * @return Object template object never <code>null</code>, throws
    * {@link RuntimeException} in case of an error.
    */
   private Object loadTemplate(IPSGuid guid, boolean readonly)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSTemplateService service = (IPSTemplateService) getService();
      IPSAssemblyTemplate obj = null;
      try
      {
         if (readonly)
         {
            obj = service.loadUnmodifiableTemplate(guid);
         }
         else
         {
            obj = service.loadTemplate(guid, true);
         }
      }
      catch (PSAssemblyException e)
      {
         throw new RuntimeException(e);
      }
      if (obj == null)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return obj;
   }

   @Override
   public void save(Object obj)
   {
      save(obj, null);
   }

   /**
    * Saves the template along with its associations. If the associationSets or
    * <code>null</code> or empty or associations of these sets are
    * <code>null</code> or empty then the original associations are not
    * touched. The association setting is replacement only and does not merge
    * the supplied list with the original association list. The objects of
    * associations are expected to be of type String. If any object in the list
    * of associations is not a String or no slot object exists with that name
    * then it is not added to the template.
    * 
    * @param obj Object
    * 
    */
   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj must not be null");

      if (!(obj instanceof IPSAssemblyTemplate))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      IPSTemplateService service = (IPSTemplateService) getService();
      try
      {
         IPSAssemblyTemplate template = (IPSAssemblyTemplate) obj;
         if (associationSets != null)
         {
            for (IPSAssociationSet set : associationSets)
            {
               if (set.getType() == AssociationType.TEMPLATE_SLOT)
               {
                  if (set.getAssociations() != null)
                     setSlotAssociations(template, set.getAssociations());
               }
            }
         }
         service.saveTemplate(template);
      }
      catch (PSAssemblyException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Sets the supplied slot associations on the supplied template. See
    * {@link #save(Object, List)} for details.
    * 
    * @param template assumed not <code>null</code>.
    * @param associations assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void setSlotAssociations(IPSAssemblyTemplate template,
         List associations)
   {
      List<String> slotNames = PSDesignModelUtils.getStringList(associations);
      // Log all the ones in the list that are not instances of String
      if (slotNames.size() != associations.size())
      {
         associations.removeAll(slotNames);
         for (Object obj : associations)
         {
            String msg = "Skipping the template ({0}), slot ({1}) association "
                  + "as the type of the association is not String.";
            Object[] args = { template.getName(), obj.toString() };
            ms_logger.warn(MessageFormat.format(msg, args));
         }
      }
      IPSTemplateService service = (IPSTemplateService) getService();

      List<IPSTemplateSlot> slots = new ArrayList<IPSTemplateSlot>();
      if (!slotNames.isEmpty())
      {
         slots = service.findSlotsByNames(slotNames);
         // Log all names that do not have slot objects
         if (slots.size() != slotNames.size())
         {
            List<String> temp = new ArrayList<String>();
            for (IPSTemplateSlot slot : slots)
            {
               temp.add(slot.getName());
            }
            for (String sn : temp)
            {
               String msg = "Failed to load the slot with the given name {0}, "
                     + "skipping the template {1} and slot {2} association.";
               Object[] args = { sn, template.getName(), sn };
               ms_logger.warn(MessageFormat.format(msg, args));
            }
         }
      }
      Set<IPSTemplateSlot> tempSlots = new HashSet<IPSTemplateSlot>(slots);
      template.setSlots(tempSlots);
   }

   @Override
   public void delete(IPSGuid guid)
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      
      IPSAssemblyTemplate template = service.findTemplate(guid);
      if(template == null)
      {
         String msg = "Failed to find the template with the given id ({0}) "
               + " skipping the deletion.";
         Object[] args = { guid };
         ms_logger.info(MessageFormat.format(msg, args));
         return;
      }
      
      String depTypes = PSDesignModelUtils.checkDependencies(guid);
      if(depTypes != null)
      {
         String msg = "Skipped deletion of template ({0}) as it is " +
               "currently being used by ({1})";
         Object[] args = { template.getName(), depTypes };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }

      try
      {
         removeSiteAssociations(guid);
         removeContentTypeAssociations(guid);
         removeSlotAssocations(guid);
         service.deleteTemplate(guid);
      }
      catch (Exception e)
      {
         String msg = "Failed to delete the template with the given id ({0}) "
               + "and name ({1})";
         Object[] args = { guid, template.getName() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
   }

   /**
    * Helper method to remove the content type template associations if exists.
    * 
    * @param guid, Template guid assumed not <code>null</code>.
    * @throws RepositoryException
    */
   private void removeContentTypeAssociations(IPSGuid guid)
      throws RepositoryException
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      List<IPSNodeDefinition> nodeDefs = mgr.findAllItemNodeDefinitions();
      List<IPSNodeDefinition> nodeDefs2Save = new ArrayList<IPSNodeDefinition>();
      for (IPSNodeDefinition nodeDef : nodeDefs)
      {
         Set<IPSGuid> ctemps = nodeDef.getVariantGuids();
         if (ctemps.contains(guid))
         {
            nodeDef.removeVariantGuid(guid);
            nodeDefs2Save.add(nodeDef);
         }
      }
      if (!nodeDefs2Save.isEmpty())
      {
         mgr.saveNodeDefinitions(nodeDefs2Save);
      }
   }

   /**
    * Helper method to remove the site template associations.
    * 
    * @param guid, Template guid assumed not <code>null</code>.
    * @throws PSAssemblyException
    */
   private void removeSiteAssociations(IPSGuid guid)
      throws PSAssemblyException
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSAssemblyTemplate template;
      template = service.loadTemplate(guid, true);

      // get site / templates associations
      IPSSiteManagerInternal sitemgr = (IPSSiteManagerInternal) PSSiteManagerLocator
            .getSiteManager();
      Map<PSPair<IPSGuid, String>, Collection<IPSGuid>> siteToTemplates = sitemgr
            .findSiteTemplatesAssociations();

      // save template / sites associations
      for (Map.Entry<PSPair<IPSGuid, String>, Collection<IPSGuid>> entry : siteToTemplates
            .entrySet())
      {
         IPSGuid siteId = entry.getKey().getFirst();
         Collection<IPSGuid> templateIds = entry.getValue();

         // remove template / sites associations
         if (templateIds.contains(template.getGUID()))
         {
            IPSSite s = sitemgr.loadSite(siteId);
            s.getAssociatedTemplates().remove(template);
            sitemgr.saveSite(s);
         }
      }
   }

   /**
    * 
    * Helper method to handle the slot association removals for the supplied
    * template.
    * 
    * @param guid, Template guid assumed not <code>null</code>.
    * @throws PSAssemblyException
    */
   private void removeSlotAssocations(IPSGuid guid) throws PSAssemblyException
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      List<IPSTemplateSlot> allSlots = service.findSlotsByName(null);
      List<IPSTemplateSlot> modSlots = new ArrayList<IPSTemplateSlot>();
      for (IPSTemplateSlot slot : allSlots)
      {
         Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations = slot
               .getSlotAssociations();
         Iterator<PSPair<IPSGuid, IPSGuid>> iter = slotAssociations.iterator();
         boolean modified = false;
         while (iter.hasNext())
         {
            PSPair<IPSGuid, IPSGuid> assoc = iter.next();
            if (guid.equals(assoc.getFirst())
                  || guid.equals(assoc.getSecond()))
            {
               iter.remove();
               modified = true;
            }
         }
         if (modified)
         {
            slot.setSlotAssociations(slotAssociations);
            modSlots.add(slot);
         }
      }
      if (!modSlots.isEmpty())
      {
         // now save the slots
         for (IPSTemplateSlot slot : modSlots)
         {
            service.saveSlot(slot);
         }
      }

   }

   @Override
   public List<IPSAssociationSet> getAssociationSets()
   {
      List<IPSAssociationSet> asets = new ArrayList<IPSAssociationSet>();
      asets.add(new PSAssociationSet(AssociationType.TEMPLATE_SLOT));
      return asets;
   }

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSTemplateModel");

}
