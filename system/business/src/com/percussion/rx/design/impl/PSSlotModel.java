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

import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.rx.design.IPSAssociationSet.AssociationAction;
import com.percussion.rx.design.IPSAssociationSet.AssociationType;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class PSSlotModel extends PSDesignModel
{

   /**
    * Saves the supplied slot object and if present the associations also. If
    * the associationSets are <code>null</code> or the associations on each
    * set are <code>null</code> then the slot template associations are not
    * touched. Expects the associations to be the list of PSPair<String,String>
    * objects and the first one is expected to be the content type name and the
    * second one is expected to be the template name. Any association that is
    * not in the expected form or either content type or template does not exist
    * with the supplied name then the association is not added for that pair.
    * 
    * @param obj must not be <code>null</code> and must be an instance of
    * {@link IPSTemplateSlot}.
    * @param associationSets may be <code>null</code> or empty.
    */
   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if(obj == null)
               throw new IllegalArgumentException("obj must not be null");
            
      if (!(obj instanceof IPSTemplateSlot))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      try
      {
         IPSTemplateSlot slot = (IPSTemplateSlot) obj;
         IPSTemplateService service = (IPSTemplateService) getService();
         Collection<PSPair<IPSGuid, IPSGuid>> existAssoc = slot
               .getSlotAssociations();
         if (associationSets != null)
         {
            for (IPSAssociationSet set : associationSets)
            {
               if (set.getType().equals(
                     AssociationType.SLOT_CONTENTTYPE_TEMPLATE))
               {
                  setSlotAssociations(slot, existAssoc, set.getAssociations(),
                        set.getAction());
               }
            }
         }
         slot.setSlotAssociations(existAssoc);
         service.saveSlot(slot);
      }
      catch (PSAssemblyException e)
      {
         throw new RuntimeException(e);
      }

   }

   /**
    * Helper method to set the slot template associations on the supplied slot.
    * Expects the association list to consist of PSPair objects, if not skips
    * the object. The first and second object of PSPair are expected to be
    * String objects, the first one is expected to be the name of the content
    * type and the second one is expected to be the name of template.
    * 
    * @param slotObj assumed not <code>null</code>.
    * @param existAssoc the existing associations, may be <code>null</code> or
    * empty.
    * @param assoc the associations, may be <code>null</code> or empty.
    * @param action the action on how to apply the association, assumed not
    * <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private void setSlotAssociations(IPSTemplateSlot slotObj,
         Collection<PSPair<IPSGuid, IPSGuid>> existAssoc,
         Collection assoc, AssociationAction action)
   {
      if (assoc == null || assoc.isEmpty())
         return;
      
      IPSTemplateService service = (IPSTemplateService) getService();
      for (Object pairObj : assoc)
      {
         PSPair<String, String> pairNames = validatePairStrs(slotObj, pairObj);
         if (pairNames == null)
            continue;
         
         PSPair<IPSGuid, IPSGuid> pairIds = getPairIDs(slotObj, pairNames
               .getFirst(), pairNames.getSecond(), service);
         if (pairIds == null)
            continue;
         
         if (action.equals(AssociationAction.DELETE))
         {
            existAssoc.remove(pairIds);
         }
         else
         {
            if (!existAssoc.contains(pairIds))
               existAssoc.add(pairIds);
         }
      }
   }

   /**
    * Validates the supplied object as a {@link PSPair PSPair&lt;String,String>}
    * type.
    *  
    * @param slotObj the slot object, assumed not <code>null</code>.
    * @param pairObject the pair object in question, assumed not 
    * <code>null</code>.
    * 
    * @return the pair string in proper type. It may be <code>null</code> if
    * the pair object is invalid.
    */
   @SuppressWarnings("unchecked")
   private PSPair<String, String> validatePairStrs(IPSTemplateSlot slotObj,
         Object pairObject)
   {
      if (!(pairObject instanceof PSPair))
      {
         String msg = "Expected a PSPair object for slot ({0}) "
               + "association but found ({1}) skipping this object.";
         Object[] args = { slotObj.getName(), pairObject.getClass().getName() };
         ms_logger.warn(MessageFormat.format(msg, args));
         return null;
      }
      PSPair pair = (PSPair) pairObject;
      Object obj1 = pair.getFirst();
      Object obj2 = pair.getSecond();
      if (!(obj1 instanceof String) || !(obj2 instanceof String))
      {
         String msg = "Expected a PSPair<String, String> object for " +
               "slot ({0}) association but found PSPair<{1},{2}> skipping " +
               "this object."; 
         Object[] args = { slotObj.getName(), obj1.getClass().getName(),
               obj2.getClass().getName() };
         ms_logger.warn(MessageFormat.format(msg, args));
         return null;
      }
      
      return (PSPair<String, String>) pairObject;
   }

   /**
    * Gets the Content Type and Template IDs from their names.
    * 
    * @param slot the slot object, assumed not <code>null</code>.
    * @param ctName the Content Type name, assumed not <code>null</code> or 
    * empty.
    * @param tmplateName the Template name, assumed not <code>null</code> or 
    * empty. 
    * @param service the service, assumed not <code>null</code>.
    * 
    * @return the pair IDs, it may be <code>null</code> if failed to find
    * the specified Content Type or Template from their names or the specified
    * Content Type and Template association is invalid.
    */
   private PSPair<IPSGuid, IPSGuid> getPairIDs(IPSTemplateSlot slot,
         String ctName, String tmplateName, IPSTemplateService service)
   {
      try
      {
         List<IPSNodeDefinition> nodeDefs = PSContentTypeHelper
               .loadNodeDefs(ctName);
         if (nodeDefs.isEmpty())
         {
            String msg = "Failed to find the content type with name {0}, "
                  + "skipping the slot ({1}) association for pair ({2},{3}).";
            Object[] args = { ctName, slot.getName(),
                  ctName, tmplateName };
            ms_logger.warn(MessageFormat.format(msg, args));
            return null;
         }
         IPSAssemblyTemplate template = service
               .findTemplateByName(tmplateName);
         
         // validate the association
         IPSNodeDefinition nodeDef = nodeDefs.get(0);
         for (IPSGuid id : nodeDef.getVariantGuids())
         {
            if (id.equals(template.getGUID()))
               return new PSPair<>(nodeDef.getGUID(), template
                     .getGUID());
         }
         
         String msg = "Skipping the slot \"{0}\" association for pair ({1},{2})."
            + " This is because the content type \"{1}\" is not associate with"
            + " Template \"{2}\".";
         Object[] args = {slot.getName(), ctName, tmplateName };
         ms_logger.warn(MessageFormat.format(msg, args));
         
         return null;
      }
      catch (PSAssemblyException e)
      {
         String msg = "Failed to find the template with name {0}, "
               + "skipping the slot ({1}) association for pair ({2},{3}).";
         Object[] args = { ctName, slot.getName(), ctName, tmplateName };
         ms_logger.warn(MessageFormat.format(msg, args), e);
         return null;
      }
   }

   @Override
   public List<IPSAssociationSet> getAssociationSets()
   {
      List<IPSAssociationSet> asets = new ArrayList<>();
      
      // create 2 associations as the place-holder, one for DELETE, one for MERGE
      IPSAssociationSet assoc = new PSAssociationSet(AssociationType.SLOT_CONTENTTYPE_TEMPLATE);
      assoc.setAction(AssociationAction.DELETE);
      asets.add(assoc);
      assoc = new PSAssociationSet(AssociationType.SLOT_CONTENTTYPE_TEMPLATE);
      assoc.setAction(AssociationAction.MERGE);
      asets.add(assoc);
      
      return asets;
   }

   @Override
   public void delete(IPSGuid guid)
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = service.findSlot(guid);
      if(slot == null)
      {
         String msg = "Failed to find the slot with the given id ({0}) "
               + " skipping the deletion.";
         Object[] args = { guid };
         ms_logger.info(MessageFormat.format(msg, args));
         return;
      }
      String depTypes = PSDesignModelUtils.checkDependencies(guid);
      if(depTypes != null)
      {
         String msg = "Skipped deletion of slot ({0}) as it is " +
               "currently being used by ({1})";
         Object[] args = { slot.getName(), depTypes };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      try
      {
         removeTemmplateAssociations(slot);
         service.deleteSlot(guid);
      }
      catch (Exception e) 
      {
         String msg = "Failed to delete the slot with the given id ({0}) "
               + "and name ({1})";
         Object[] args = { guid, slot.getName() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
   }
   
   /**
    * Helper method to remove the template associations.
    * 
    * @param guid, The slot guid assumed not <code>null</code>.
    * @throws PSAssemblyException
    */
   private void removeTemmplateAssociations(IPSTemplateSlot slot)
      throws PSAssemblyException
   {
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      Map<IPSGuid, IPSAssemblyTemplate> templateMap = 
         new HashMap<>();
      List<IPSAssemblyTemplate> templates = service.findTemplatesBySlot(slot);
      for (IPSAssemblyTemplate template : templates)
      {
         IPSGuid templateGuid = template.getGUID();
         if (!templateMap.containsKey(templateGuid))
         {
            templateMap.put(templateGuid, template);
         }
      }

      // delete the associations to the slots being deleted and
      // save
      for (IPSAssemblyTemplate template : templateMap.values())
      {
         template.removeSlot(slot);
         service.saveTemplate(template);
      }

   }
   
   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSSlotModel");

}
