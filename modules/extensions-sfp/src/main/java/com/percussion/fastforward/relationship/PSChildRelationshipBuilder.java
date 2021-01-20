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
package com.percussion.fastforward.relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

/**
 * Creates and deletes auto relationships.
 * 
 * @author James Schultz
 * @since 6.0
 */
public class PSChildRelationshipBuilder extends PSChildRelationshipBase
{
   /**
    * Constructs an instance of <code>PSChildRelationshipBuilder</code> that
    * will use the specified relationship processor for managing relationships.
    * 
    * @param relProcessor used to query, add, and delete relationships, not
    *           <code>null</code>
    * @throws IllegalArgumentException if relProcessor is <code>null</code>
    */
   public PSChildRelationshipBuilder(IPSRelationshipProcessor relProcessor)
   {
      super(relProcessor);
   }

   /**
    * Synchronizes relationships in a slot with the specified dependent item to
    * match the supplied owner item array. Relationships are created for items
    * in the owner array without them, using the specified template. Any
    * existing relationships owned by items not in the owner array are removed.
    * 
    * @param dependentId item that is the dependent of all relationships
    * @param ownerIds only relationships owned by these ids should have the
    *           dependentId
    * @param slotName the name of the slot whose relationships between
    *           <code>ownerIds</code> and <code>dependentId</code> will be
    *           synchronized.
    * @param templateName template to assign to any created relationships
    * @throws PSCmsException propagated from relationship api errors
    * @throws PSAssemblyException if the slot or template cannot be found by
    * assembly service.
    */
   public void build(int dependentId, Object[] ownerIds, String slotName,
         String templateName) throws PSCmsException, PSAssemblyException
   {
      if (StringUtils.isBlank(slotName))
         throw new IllegalArgumentException("slotName may not be blank");
      
      List<Integer> desiredOwnerIds = convert(ownerIds);
      m_log.debug("desired ids: " + desiredOwnerIds);

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary item = cms.loadComponentSummary(dependentId);
      PSLocator child = item.getHeadLocator();

      IPSAssemblyTemplate template = findTemplate(templateName, item
            .getContentTypeGUID());
      IPSTemplateSlot slot = findSlot(slotName);

      PSRelationshipSet currentRelations = getRelationships(dependentId, slot);
      if (!currentRelations.isEmpty())
      {
         List<Integer> currentOwnerIds = extractOwnerIds(currentRelations);
         m_log.debug("current ids: " + currentOwnerIds);

         // desired - current = add
         List<Integer> idsToAdd = createComplement(desiredOwnerIds,
               currentOwnerIds);
         addRelationships(idsToAdd, child, slot, template);

         // current - desired = remove
         List<Integer> idsToRemove = createComplement(currentOwnerIds,
               desiredOwnerIds);
         filterForRelationshipsToRemove(currentRelations, idsToRemove);
         deleteRelationships(currentRelations);
      }
      else
      {
         m_log.debug("no current ids");
         addRelationships(desiredOwnerIds, child, slot, template);
      }
   }

   /**
    * Creates an active assembly relationship between the "active" revision
    * (current or edit) of each item in <code>ownerIds</code> and the
    * dependent item, using the specified slot and template.
    * 
    * @param ownerIds content ids of the items to own the relationships, assumed
    *           not <code>null</code>
    * @param dependent the item that will be dependent of the relationship,
    *           assumed not <code>null</code>
    * @param slot the slot that will be assigned to the relationship, assumed
    *           not <code>null</code>
    * @param template the template that will be assigned to the relationship,
    *           assumed not <code>null</code>
    * @throws PSCmsException propagated if an error occurs saving the
    *            relationships
    */
   private void addRelationships(Collection<Integer> ownerIds,
         PSLocator dependent, IPSTemplateSlot slot, IPSAssemblyTemplate template)
         throws PSCmsException
   {
      m_log.debug("to be added: " + ownerIds);

      // make locators from ids
      Collection<PSLocator> owners = buildOwnerLocators(ownerIds);

      // create relationships
      PSRelationshipSet newRelationships = new PSRelationshipSet();
      for (PSLocator owner : owners)
      {
         PSAaRelationship newRelationship = new PSAaRelationship(owner,
               dependent, slot, template);
         newRelationships.add(newRelationship);
      }
      saveRelationships(newRelationships);
   }

   /**
    * Builds a collection of <code>PSLocator</code> using the edit locator for
    * each content id in the <code>idsToAdd</code> parameter.
    * 
    * @param ownerIds content ids to be converted to edit revision locators,
    *           assumed not <code>null</code>, may be empty.
    * @return collection of edit <code>PSLocator</code>s for the content ids
    *         provided. never <code>null</code>, will be empty if
    *         <code>ownerIds</code> parameter is empty.
    */
   private Collection<PSLocator> buildOwnerLocators(Collection<Integer> ownerIds)
   {
      Collection<PSLocator> owners = new ArrayList<PSLocator>(ownerIds.size());
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      for (Integer contentid : ownerIds)
      {
         PSComponentSummary sum = cms.loadComponentSummary(contentid);
         PSLocator loc = sum.getHeadLocator();
         owners.add(loc);
      }
      return owners;
   }

   /**
    * Removes any relationships from the set that are not owned by an item id in
    * the <code>idsToRemove</code> collection. This leaves the set with only
    * those relationships that should be deleted.
    * 
    * @param relationships modified
    * @param idsToRemove list of owner ids whose relationships should remain in
    *           the set, because those relationships should be deleted. Assumed
    *           not <code>null</code>, may be empty.
    */
   private void filterForRelationshipsToRemove(PSRelationshipSet relationships,
         Collection<Integer> idsToRemove)
   {
      m_log.debug("to be removed: " + idsToRemove);

      // remove any relationships from the set that are not being removed
      for (Iterator iter = relationships.iterator(); iter.hasNext();)
      {
         PSRelationship r = (PSRelationship) iter.next();
         Integer ownerId = new Integer(r.getOwner().getId());
         if (!idsToRemove.contains(ownerId))
         {
            iter.remove();
         }
      }
   }

   /**
    * Creates a new list of the elements in <code>retain</code> that are not
    * in <code>suppress</code>.
    * 
    * @param retain all integers except those also in <code>suppress</code>
    *           are copied to returned list. Assumed not <code>null</code>
    * @param suppress integers to be suppressed from being copied from
    *           <code>retain</code>. Assumed not <code>null</code>
    * @return a new list of the elements in <code>retain</code> that are not
    *         in <code>suppress</code>, never <code>null</code>
    */
   protected List<Integer> createComplement(final List<Integer> retain,
         final List<Integer> suppress)
   {
      List<Integer> complement = new ArrayList<Integer>();
      for (Integer id : retain)
      {
         if (!suppress.contains(id))
         {
            complement.add(id);
         }
      }
      return complement;
   }

   /**
    * Converts an array of objects to a list of integers. Non-parsable elements
    * are skipped.
    * 
    * @param ids array of ids to be converted. if <code>null</code> returned
    *           list will be empty.
    * @return the <code>ids</code> array converted to integer list; never
    *         <code>null</code>
    */
   private List<Integer> convert(Object[] ids)
   {
      if (ids == null)
         return new ArrayList<Integer>();

      List<Integer> convertedList = new ArrayList<Integer>(ids.length);
      for (int i = 0; i < ids.length; i++)
      {
         Object id = ids[i];
         if (id instanceof Integer)
         {
            convertedList.add((Integer) id);
         }
         else
         {
            try
            {
               convertedList.add(new Integer(id.toString()));
            }
            catch (NumberFormatException e)
            {
               // skip any non-parsables
               m_log
                     .warn("skipping non-parsable element in array <" + id
                           + ">");
            }
         }
      }
      return convertedList;
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   protected Log m_log = LogFactory.getLog(PSChildRelationshipBuilder.class);
}
