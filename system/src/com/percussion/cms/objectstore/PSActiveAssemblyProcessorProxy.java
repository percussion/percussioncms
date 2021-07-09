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
package com.percussion.cms.objectstore;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSAuthTypes;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSInternalRequestCallException;

import com.percussion.design.objectstore.PSCollectionComponent;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class extends {@link PSProcessorProxy} and is to manipulate
 * all the active assembly relationships and properties and is a convenient
 * wrapper around the functionality of {@link PSRelationshipProcessorProxy} 
 * with methods that are meaningful for active assembly processing.
 * <p>
 * Active assembly involves manipultion of item (owner), variant, slot and
 * relationship objects based on some preconfigured rules.
 * <p>An implementor should be able to achieve everything he wants to do
 * around active assembly using this class. He can use the class
 * {@link PSRelationshipProcessorProxy} and do the same, however, it will be 
 * more work.
 *
 * @author RammohanVangapalli
 */
public class PSActiveAssemblyProcessorProxy extends PSProcessorProxy
{
   /**
    * Creates a proxy for a specific type of processor. Simply delegates to the
    * base  class.
    *
    * @param type  The type of processor for which this class is acting as a
    *    proxy. See {@link PSProcessorProxy version of the constructor} for more
    *    details
    *
    * @param ctx A context object appropriate for the processor type,
    * may be <code>null</code> if the processor does not require one.
    *
    * @throws PSCmsException If the xml document is not well-formed and
    *    conformant to its schema.
    */
   public PSActiveAssemblyProcessorProxy(String type, Object ctx)
      throws PSCmsException
   {
      super(type, ctx);
   }

   /**
    * Get the list of all slots for a given item locator including inline slots.
    * This is list a union (i.e. does not include duplicates) of all slots from
    * all variants of the item.
    * 
    * @param item Locator for the owner item, must not be <code>null</code>.
    * @return Set of slot objects associated with this item, never
    *         <code>null</code>, may be empty.
    * @throws IllegalArgumentException if the item locator supplied is
    *            <code>null</code>.
    * @throws PSCmsException if it cannot extract item slots for any reason.
    * @throws PSUnknownNodeTypeException if the slots are not loaded because XML
    *            parsing error.
    */
   public PSSlotTypeSet getItemSlots(PSLocator item)
      throws PSCmsException, PSUnknownNodeTypeException
   {
      PSContentTypeVariantSet variantSet = getItemVariants(item);
      Iterator iter = variantSet.iterator();
      Set slotKeys = new HashSet();
      while (iter.hasNext())
      {
         PSContentTypeVariant variant = (PSContentTypeVariant) iter.next();
         Iterator iter2 = variant.getVariantSlots().iterator();
         while (iter2.hasNext())
         {
            PSVariantSlotType entry = (PSVariantSlotType) iter2.next();
            int slotid = entry.getSlotId();
            slotKeys.add(PSSlotType.createKey(slotid));
         }
      }

      PSKey[] keys = new PSKey[slotKeys.size()];
      keys = (PSKey[]) slotKeys.toArray(keys);

      PSComponentDefProcessorProxy defProxy =
         new PSComponentDefProcessorProxy(
            PSComponentDefProcessorProxy.PROCTYPE_SERVERLOCAL,
            m_context);

      return new PSSlotTypeSet(
         defProxy.load(
            PSDbComponent.getComponentType(PSSlotTypeSet.class),
            keys));
   }

   /**
    * Get the list of all variants registered for the item specified by the
    * locator.
    * 
    * @param item Locator for the owner item, must not be <code>null</code>.
    * @return Set of variant objects associated with this item, never
    *         <code>null</code>, may be empty.
    * @throws IllegalArgumentException if the item locator supplied is
    *            <code>null</code>.
    * @throws PSCmsException if it cannot extract item variants for any reason.
    * @throws PSUnknownNodeTypeException if the variants are not loaded because
    *            XML parsing error.
    */
   public PSContentTypeVariantSet getItemVariants(PSLocator item)
      throws PSUnknownNodeTypeException, PSCmsException
   {
      if (item == null)
      {
         throw new IllegalArgumentException(
               "item must not be null for getItemSlots()");
      }
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = cms.loadComponentSummary(item.getId());
      if (summary == null)
         return new PSContentTypeVariantSet();
      
      long cTypeId = summary.getContentTypeId();

      PSComponentDefProcessorProxy defProxy =
         new PSComponentDefProcessorProxy(
            PSComponentDefProcessorProxy.PROCTYPE_SERVERLOCAL,
            m_context);

      PSKey cTypeKey = PSContentTypeVariant.createKey(cTypeId, -1);
      return new PSContentTypeVariantSet(
         defProxy.load(
            PSDbComponent.getComponentType(PSContentTypeVariantSet.class),
            new PSKey[] { cTypeKey }));
   }

   /**
    * Get the list of all relationships for the given slot of the item
    * specified by the locator. This is equivalent to getting all related
    * items in the specified slot for manipulation.
    * @param owner Locator for the owner item, must not be <code>null</code>.
    * @param slot item slot object for which the relationships are to be
    * fetched, must not be <code>null</code>.
    * @param authType authorization type value to filter the relationships, 
    * one of the valid authorization type valuess. This auth type must have 
    * been implemented in sys_casSupport application. It is an error if the 
    * requested auth type is not implemented. It is <code>null</code> if the
    * returned relationships are not filtered by authType. The template of the
    * relationship will be ignored if the authType value is <code>null</code>.
    *  
    * @return a sorted list of relationship objects associated with this slot 
    * of the item. The list is in the order of sort rank property of the
    * relationships. It is never <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if the item locator or the slot supplied
    * is <code>null</code> or slotid specified is invalid.
    * @throws PSCmsException if it cannot extract relationships for the item
    * slot for any reason.
    */
   public PSAaRelationshipList getSlotRelationships(
      PSLocator owner,
      PSSlotType slot,
      Integer authType)
      throws PSCmsException
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");
      
      if (slot == null)
         throw new IllegalArgumentException("slot cannot be null");
      
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, "" + slot.getSlotId());

      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      Iterator relationships = relProxy.getRelationships(filter).iterator();
      PSAaRelationshipList aaRels = new PSAaRelationshipList();
      if (!relationships.hasNext())
         return aaRels;
      
      List authLocatorList = null;
      try
      {
         if (authType != null)
         {
         authLocatorList = getDependentsByAuthType(owner, authType);
      }
      }
      catch (PSNotFoundException e1)
      {
         throw new PSCmsException(e1.getErrorCode(), e1.getErrorArguments());
      } 

      PSRelationship rel = null;
      while (relationships.hasNext())
      {
         rel = (PSRelationship) relationships.next();
         if (authType == null)
         {
            aaRels.add(new PSAaRelationship(rel));
            continue;
         }
         
         //Filter by authtype
         if (!authLocatorList.contains("" + rel.getDependent().getId()))
            continue;

         String variantstr = rel.getProperty(IPSHtmlParameters.SYS_VARIANTID);
         if(variantstr==null)
            variantstr = "";
         int variantid = -1;
         try
         {
            variantid = Integer.parseInt(variantstr);
         }
         catch(NumberFormatException nfe)
         {
            variantid = -1;
         }
         if(variantid==-1)
         {
            String[] args = {"" + rel.getId(), variantstr};
            throw new PSCmsException(IPSCmsErrors.INVALID_AA_RELATIONSHIP, args);
         }
         IPSAssemblyService assembly = 
            PSAssemblyServiceLocator.getAssemblyService();
         PSContentTypeVariant variant = null;
         try
         {
            variant = 
               new PSContentTypeVariant(assembly
                     .loadUnmodifiableTemplate(variantstr));
         }
         catch(PSAssemblyException e)
         {
            String[] args = {"" + variantid};
            throw new PSCmsException(IPSCmsErrors.VARIANT_LOOKUP_FAILED, args);
         }
         aaRels.add(new PSAaRelationship((PSRelationship) rel, slot, variant));
      }
      
      Collections.sort(aaRels, new RelationshipSorter());
      return aaRels;
   }

   /**
    * Get the summaries for the dependent items of all relationships for 
    * the given slot of the item specified by the locator. This is equivalent 
    * to getting all related items in the specified slot for manipulation.
    * @param owner Locator for the owner item, must not be <code>null</code>.
    * @param slot item slot object for which the dependent items are to be
    * fetched, must not be <code>null</code>.
    * @param authType authorization type value to filter the slot items, 
    * one of the valid authorization type values. This auth type must have 
    * been implemented in sys_casSupport application. It is an error if the 
    * requested auth type is not implemented. 
    * @return Component summaries for the dependent items related to parent item
    * via slot supplied, never <code>null</code>, may be empty.
    * @throws IllegalArgumentException if the item locator or the slot supplied
    * is <code>null</code> or slotid specified is invalid.
    * @throws PSCmsException if it cannot extract component summaries for the
    * dependent items for any reason.
    */
   public PSComponentSummaries getSlotItems(
      PSLocator owner,
      PSSlotType slot,
      int authType)
      throws PSCmsException
   {
      if (owner == null)
         throw new IllegalArgumentException("owner cannot be null");
      
      if (slot == null)
         throw new IllegalArgumentException("slot cannot be null");
      
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, "" + slot.getSlotId());

      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      PSComponentSummaries summaries = new PSComponentSummaries();
      Iterator iter = relProxy.getSummaries(filter, false).iterator();
      try
      {
         List list = getDependentsByAuthType(owner, authType);
         while (iter.hasNext())
         {
            PSComponentSummary summary = (PSComponentSummary) iter.next();
            //Filter by authtype
            if(!list.contains(""+((PSLocator)summary.getLocator()).getId()))
               continue;
            summaries.add(summary);
         }
      }
      catch (PSNotFoundException e)
      {
         throw new PSCmsException(e.getErrorCode(), e.getErrorArguments());
      }
      return summaries;
   }

   /**
    * Rearrange the related items specified via relationships and move to a
    * specified location. All the items from the relationship list will be
    * arranged together in the order they occur in the set and moved to the
    * specified location. The first item (relationship) in the list will get
    * the order specified by the new location. Specify 0 to move to top in
    * the slot and -1 (or a value larger than the items in the slot) to move
    * to bottom. All relationships in the list must be for the same slot and
    * have the same owner.
    * 
    * @param slotRelations list of relationships to rearrange and move, must
    *    not be <code>null</code> or empty.
    * @param index new location, -1 for bottom and any other value
    *    specified becomes the sort index of the first item in the relationship
    *    list to rearrange.
    * @throws PSCmsException if rearrange fails for any reason.
    */
   public void reArrangeSlotRelationships(PSAaRelationshipList slotRelations,
      int index) throws PSCmsException
   {
      if (slotRelations == null || slotRelations.isEmpty())
         throw new IllegalArgumentException(
            "slotRelations cannot be null or empty");

      // validate slotid and owner for supplied relationships
      Iterator relationships = slotRelations.iterator();
      int slotid = -1;
      PSLocator owner = null;
      while (relationships.hasNext())
      {
         PSAaRelationship relationship = 
            (PSAaRelationship) relationships.next();
         
         if (slotid == -1)
            slotid = relationship.getSlot().getSlotId();
         else if (slotid != relationship.getSlot().getSlotId())
            throw new IllegalArgumentException(
               "all relationhips must belong to the same slot");
         
         if (owner == null)
            owner = relationship.getOwner();
         else if (!owner.equals(relationship.getOwner()))
            throw new IllegalArgumentException(
               "all relationhips must belong to the same owner");
      }
      
      PSRelationshipProcessor proxy = PSRelationshipProcessor.getInstance();

      // load all existing relationships for the current owner and slot
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.limitToOwnerRevision(true);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, 
         Integer.toString(slotid));
      PSRelationshipSet currentSet = proxy.getRelationships(filter);
      
      // sort all existing relationships according to their sortrank property
      Collections.sort(currentSet, new RelationshipSorter());

      // prepare the result set with a list of all non-reordered relationships
      PSRelationshipSet resultSet = new PSRelationshipSet();
      for (int i=0; i<currentSet.size(); i++)
      {
         PSRelationship current = (PSRelationship) currentSet.get(i);
         
         boolean found = false;
         for (int j=0; !found && j<slotRelations.size(); j++)
         {
            PSRelationship test = (PSRelationship) slotRelations.get(j);
            if (current.getId() == test.getId())
               found = true;
         }
         
         if (!found)
            resultSet.add(current);
      }

      insertNormalize(resultSet, slotRelations, index, isZeroBased(currentSet));

      proxy.save(resultSet);
   }
   
   /**
    * Inserts the supplied source relationship collection into the target 
    * relationship collection at the given index. First the
    * supplied source relationships are inserted at the specified index.
    * Then all target relationships sortrank property is normalized
    * starting with 1, continiously incremented by 1.
    * 
    * @param target the relationship collection into which to insert the 
    *    supplied source collection, assumed not <code>null</code>.
    * @param source the relationship collection which will be inserted into
    *    the supplied target collection at the provided index, assumed not
    *    <code>null</code>.
    * @param index the index where to insert the supplied source relationships 
    *    into the target relationships. Supply -1 or a value greater than the 
    *    target relationship size to append the source to the target.
    * @param isZeroBased <code>true</code> if the ordering of the collection
    *    of relationships is zero-based, <code>false</code> otherwise. 
    */
   private void insertNormalize(PSRelationshipSet target, 
      PSAaRelationshipList source, int index, boolean isZeroBased)
   {
      // calculate the location index for new relationships
      if (index == -1 || index > target.size())
      {
         index = target.size();
      }
      else
      {
         if (!isZeroBased)
         {
         index = index-1;
         }
      }

      // insert all new relationships into the existing relationships
      for (int i=0; i<source.size(); i++)
         target.add(index++, (PSRelationship) source.get(i));

      // normalize the sortrank property as 1-based index
      for (int i=0; i<target.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) target.get(i);
         
         relationship.setProperty(IPSHtmlParameters.SYS_SORTRANK, 
            Integer.toString(i+1));
      }
   }

   /**
    * Add new items specified by the relationship list to a slot at a specified
    * location index. All relationships in the list must be for the same owner
    * and slot for this method to be successful. Also, all the relationships 
    * must have an id of -1. The sortrank properties of all saved relationships
    * will be normalized starting with 1, continiously incremented by 1.
    * 
    * @param newSlotRelations list of relationships to add at the location
    *    specified, must not be <code>null</code> or empty.
    * @param locIndex the location index where to insert the new supplied 
    *    relationships into existing relationships. Supply -1 or a value
    *    greater than the existing relationship size to append the ones to the
    *    end. It is <code>1</code> based number.
    * @throws PSCmsException if add fails for any reason.
    */
   public void addSlotRelationships(PSAaRelationshipList newSlotRelations,
      int locIndex) throws PSCmsException
   {
      if (newSlotRelations == null || newSlotRelations.isEmpty())
         throw new IllegalArgumentException(
            "newSlotRelations must not be null or empty");

      Iterator relationships = newSlotRelations.iterator();
      int slotid = -1;
      PSLocator owner = null;
      while (relationships.hasNext())
      {
         PSAaRelationship relationship = 
            (PSAaRelationship) relationships.next();
         
         // validate id of each relationship 
         int rid = relationship.getId();
         if (rid != -1)
            throw new IllegalArgumentException(
               "all relationhips must have an id of -1");

         // validate slotid of each relationship
         if (slotid == -1)
         {
            String id = relationship.getProperty(IPSHtmlParameters.SYS_SLOTID);
            slotid = Integer.parseInt(id);
         }
         else if (slotid != relationship.getSlot().getSlotId())
         {
            throw new IllegalArgumentException(
               "all relationhips must belong to the same slot");
         }
         
         // validate owner of each relationship
         if (owner == null)
            owner = relationship.getOwner();
         else if (!owner.equals(relationship.getOwner()))
            throw new IllegalArgumentException(
               "all relationhips must belong to the same owner");
      }
      
      PSRelationshipProcessor proxy = PSRelationshipProcessor.getInstance();
      
      // load all existing relationships for the current owner and slot
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.limitToOwnerRevision(true);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, 
         Integer.toString(slotid));
      PSRelationshipSet existing = proxy.getRelationships(filter);
      
      // sort all existing relationships according to their sortrank property
      Collections.sort(existing, new RelationshipSorter());
      
      insertNormalize(existing, newSlotRelations, locIndex, isZeroBased(existing));

      proxy.save(existing);
   }

   /**
    * Used to sort relationships based on the sort rank property.
    */
   public class RelationshipSorter implements Comparator
   {
      /* (non-Javadoc)
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(Object o1, Object o2)
      {
         PSRelationship rel1 = (PSRelationship) o1;
         PSRelationship rel2 = (PSRelationship) o2;

         int sortrank1 = 0;
         try
         {
            sortrank1 = Integer.parseInt(rel1.getProperty(
               IPSHtmlParameters.SYS_SORTRANK));
         }
         catch (Exception e)
         {
            //ignore and keep 0
         }

         int sortrank2 = 0;
         try
         {
            sortrank2 = Integer.parseInt(rel2.getProperty(
               IPSHtmlParameters.SYS_SORTRANK));
         }
         catch (Exception e)
         {
            //ignore and keep 0
         }
         
         return (sortrank1 - sortrank2);
      }
   }


   /**
    * Remove items specified by the relationship list from a slot. All
    * relationships in the list must have the same owner and for the same
    * slot for this method to be successful.
    * @param existingSlotRelations list of relationships to remove, must
    * not be <code>null</code> or empty.
    * @throws IllegalArgumentException if
    * <ol>
    * <li>the relationship list is <code>null</code> or empty</li>
    * <li>if all relationships are not for the same slot or do
    * not share the same owner</li>
    * </ol>
    * @throws PSCmsException if remove fails for any reason.
    */
   public void removeSlotRelations(PSAaRelationshipList existingSlotRelations)
      throws PSCmsException
   {
      if (existingSlotRelations == null || existingSlotRelations.isEmpty())
      {
         throw new IllegalArgumentException(
         "existingSlotRelations must not be empty in removeSlotRelations()");
      }

      Iterator iter = existingSlotRelations.iterator();
      PSRelationshipSet rels = new PSRelationshipSet();
      while(iter.hasNext())
         rels.add((PSRelationship)iter.next());

      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      relProxy.delete(rels);
   }

   /**
   * Helper method to get the relationship object given the relationship type 
   * name. Simply delegates to the same method of 
   * {@link PSRelationshipProcessorProxy}. 
   * @param relationshipName name of the relationship name. Must correspond 
   * to one of the relationships configured in the workbench. 
   * @return Relationship Configuration object, may be <code>null</code> if 
   * one with the name supplied is not found.
   * @throws PSCmsException if the method cannot extract the configuraion 
   * object for the given name for any reason.
   */
   public PSRelationshipConfig getRelationshipConfig(String relationshipName)
      throws PSCmsException
   {
      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      
      return relProxy.getConfig(relationshipName);
   }

   /**
    * Save the specified active assembly relationship list. Corrects the sort 
    * rank properties before saving.
    * @param relationships relationship list to ve saved, must not be 
    * <code>null</code> or empty. 
    * @throws PSCmsException
    */
   public void save(PSAaRelationshipList relationships) throws PSCmsException
   {
      if(relationships == null || relationships.isEmpty())
         throw new IllegalArgumentException("relationships must not be " +
            "empty for save() method");
         
      PSRelationshipSet relSet = new PSRelationshipSet();
      //reset the sort orders to correct the values keeping the same order. 
      //And validate the relationships
      PSAaRelationship aaRel = null;
      for (int i = 0; i < relationships.size(); i++)
      {
         aaRel = (PSAaRelationship)relationships.get(i);
         PSRelationship relationship = (PSRelationship) aaRel;
         relationship.setProperty(IPSHtmlParameters.SYS_SORTRANK, "" + i);
         validateAaRelationship(aaRel);
         relSet.add(relationship);
      }
      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      relProxy.save(relSet);
   }
   
   /**
    * Helper methid that validates the active assembly relationship. This has 
    * the following steps:
    * <ol>
    * <li>Check to make sure the relationship category is active assembly</li>
    * <li>The slot of the AA relationship must allow the variant of the 
    * relationship</li>
    * </ol>
    * @param aaRel Active Assembly relationship to validate, must not be 
    * <code>null</code>.
    * @throws PSCmsException if vaildation fails.
    */
   public void validateAaRelationship(PSAaRelationship aaRel)
      throws PSCmsException
   {
      if (aaRel == null)
         throw new IllegalArgumentException("aaRel cannot be null");
      
      if (!aaRel
         .getConfig()
         .getCategory()
         .equals(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
      {
         String[] args ={"" + aaRel.getId(), aaRel.getConfig().getCategory(),
               PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY};
         throw new PSCmsException(IPSCmsErrors.INVALID_AA_RELATIONSHIP_TYPE,
               args);
      }
      PSSlotType slot = null;
      PSContentTypeVariant variant = null;
      slot = aaRel.getSlot();
      variant = aaRel.getVariant();
      PSSlotTypeContentTypeVariantSet slotVariants = slot.getSlotVariants();
      if (!slotVariants.isVariantAllowed(variant))
      {
         String[] args ={"" + aaRel.getId(), "" + slot.getSlotId(),
               "" + variant.getVariantId()};
         throw new PSCmsException(IPSCmsErrors.INVALID_AA_RELATIONSHIP_SLOT_VARIANT, args);
      }
   }
   
   /**
    * Get a list of dependent contentids given the parent locator and the
    * authorization type value. The list is obtained by executing the internal
    * request to "sys_casSupport/casSupport_x" Rhythmyx resource, where x is the
    * authorization type value.
    * 
    * @param parent Locator for the patent item.
    * @param authType authorization type value one of the valid authorization
    *           type valuess. This auth type must have been implemented in
    *           sys_casSupport application. It is an error if the requested auth
    *           type is not implemented.
    * @return A list of all contentids (as strings) for the dependent items
    *         filtered for supplied authorization type.
    * @throws PSNotFoundException if the authtype implementation resource is not
    *            found.
    * @throws PSCmsException if any other error occurs while filtering by
    *            authtype.
    */
   private List getDependentsByAuthType(PSLocator parent, int authType)
      throws PSNotFoundException, PSCmsException
   {
      String resource = PSAuthTypes.getInstance().getResourceForAuthtype(
            "" + authType);
      if (resource == null || resource.length() == 0)
      {
         String[] args =
         {"" + authType,
               PSAuthTypes.getInstance().getConfigFile().getAbsolutePath()};
         throw new PSCmsException(IPSCmsErrors.INVALID_AUTHTYPE, args);
      }
      IPSRequestContext cxt = null;
      if (m_context instanceof PSRequest)
      {
         cxt = new PSRequestContext((PSRequest) m_context);
      }
      else if (m_context instanceof IPSRequestContext)
      {
         cxt = (IPSRequestContext) m_context;
      }
      if (cxt == null)
      {
         String[] args = {};
         throw new PSCmsException(IPSCmsErrors.INVALID_CONTEXT_FOR_AA_PROXY,
               args);
      }
      List result = new ArrayList();

      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_CONTENTID, "" + parent.getId());
      params.put(IPSHtmlParameters.SYS_REVISION, "" + parent.getRevision());
      IPSInternalRequest ir = cxt.getInternalRequest(resource, params, true);
      if (ir == null)
      {
         Object[] args = { resource, "No request handler found." };
         throw new PSNotFoundException(
            IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE,
            args);
      }
      try
      {
         Document doc = ir.getResultDoc();
         NodeList nl = doc.getElementsByTagName("linkurl");
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            Element elem = (Element)nl.item(i);
            String contentid = elem.getAttribute("contentid");
            if (contentid!=null)
            {
               contentid = contentid.trim();
               if(contentid.length()>0)
                  result.add(contentid);
            }
         }
      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSCmsException(e.getErrorCode(), e.getErrorArguments());
      }
      return result;
   }
   
   /**
    * Determines if the ordering of a collection of relationships
    * is zero-based.
    * 
    * @param collection of {@link PSRelationship} objects, may not
    * be <code>null</code>.
    * 
    * @return <code>true</code> if the sort rank properties for the
    *  collection of relationships are zero-based, <code>false</code>
    *  otherwise. 
    */
   public boolean isZeroBased(PSCollectionComponent collection)
   {
      if (collection == null)
      {
         throw new IllegalArgumentException("collection may not be null");
      }
      
      for (int i=0; i<collection.size(); i++)
      {
         PSRelationship relationship = (PSRelationship) collection.get(i);
         
         if (relationship.getProperty(IPSHtmlParameters.SYS_SORTRANK).
               equals("0"))
         {
            return true;
         }
      }
      
      return false;
   }
}
