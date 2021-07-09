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
package com.percussion.services.publisher.impl;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStopwatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * This handler follows the ancestors of an item that has been updated or
 * otherwise marked. (From David Benua) The "Owner" of any AA relationship
 * should be touched if (and only if) one of the following conditions is true:
 * <ul>
 * <li> The Dependent item is the page that was edited (or transitioned) and not
 * a parent or other ancestor (this is the first level)
 * <li> The template of the relationship is a snippet which contains one or more
 * slots. This may occur where an image or other related field is included in a
 * snippet.
 * <li> The dependent item was reached by following a relationship based on an
 * Inline Slot. This can be determined from the Slot Type field.
 * </ul>
 */
public class PSTouchParentItemsHandler
{
   /**
    * Logger for this class
    */
    private static final Logger ms_log = LogManager.getLogger(PSTouchParentItemsHandler.class);

   /**
    * Load the relationship service on class init
    */
   IPSRelationshipService ms_rel = PSRelationshipServiceLocator
         .getRelationshipService();

   /**
    * Load the assembly service on class init
    */
   IPSAssemblyService ms_asm = PSAssemblyServiceLocator.getAssemblyService();

   /**
    * The hibernate session, initialized on construction
    */
   Session m_session = null;

   /**
    * Inline slots
    */
   Set<Long> m_inlineSlots = new HashSet<>();

   /**
    * Templates with slots
    */
   Set<Long> m_templatesWithSlots = new HashSet<>();

   /**
    * Items to be touched.
    */
   Set<Integer> m_items = new HashSet<>();

   /**
    * Create a handler
    * 
    * @param session hibernate session, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public PSTouchParentItemsHandler(Session session) {
      if (session == null)
      {
         throw new IllegalArgumentException("session may not be null");
      }
      m_session = session;

      Criteria c = m_session.createCriteria(PSAssemblyTemplate.class);
      c.add(Restrictions.isNotEmpty("slots"));
      c.setProjection(Projections.property("id"));
      m_templatesWithSlots.addAll(c.list());

      c = m_session.createCriteria(PSTemplateSlot.class);
      c.add(Restrictions.eq("slottype", 1));
      c.setProjection(Projections.property("id"));
      m_inlineSlots.addAll(c.list());
   }

   /**
    * Add the specific passed ids to the items to be touched
    * 
    * @param ids a collection of ids, may be empty but not <code>null</code>
    */
   public void addSpecificIds(Collection<Integer> ids)
   {
      if (ids == null)
      {
         throw new IllegalArgumentException("ids may not be null");
      }
      m_items.addAll(ids);
   }

   /**
    * Add the owners of the passed ids to the items to be touched. The rules
    * discussed in the class description are used to decide how far to pass up
    * the owner hierarchy. This method adds all the direct owner parents with AA
    * relationships
    * 
    * @param ids the ids to touch parents of, never <code>null</code> but
    *           could be empty
    */
   public void addParents(Collection<Integer> ids)
   {
      PSStopwatch watch = new PSStopwatch();
      watch.start();

      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");
      
      if (ids.isEmpty())
         return;
      
      try
      {
         List<PSRelationship> rels = getParentRelationships(ids);
         Set<Integer> parents = new HashSet<>();
         for (PSRelationship rel : rels)
         {
            Integer parentid = rel.getOwner().getId();
            parents.add(parentid);
         }
         addGrandParents(parents);

         if (ms_log.isDebugEnabled()) 
         {
            watch.stop();
            ms_log.debug("[addParents] elapse = " + watch.toString()
                  + ". ids: " + ids.size() + ". m_items: " + m_items.size());
         }
      }
      catch (PSException e)
      {
         ms_log.error("Problem finding parents", e);
      }
   }

   /**
    * Gets the Active Assembly relationships where the dependents are the 
    * specified items.
    * 
    * @param ids the ids of the specified items; assumed not <code>null</code>
    *    or empty.
    *    
    * @return the relationships; may be empty, but never <code>null</code>. 
    * 
    * @throws PSException if an error occurs.
    */
   private List<PSRelationship> getParentRelationships(Collection<Integer> ids) 
      throws PSException
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependentIds(ids);
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
      filter.limitToEditOrCurrentOwnerRevision(true);
      
      return ms_rel.findByFilter(filter);
   }
   
   /**
    * Determines whether the slot of the specified relationship is inline slot.
    *  
    * @param rel the relationship in question; assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the slot is a inline slot; otherwise return 
    *    <code>false</code>.
    */
   private boolean isInlineSlot(PSRelationship rel)
   {
      String sidProperty = rel.getProperty(IPSHtmlParameters.SYS_SLOTID);
      if (!StringUtils.isBlank(sidProperty))
      {
         Long sid = null;
         try
         {
            sid = new Long(sidProperty);
         }
         catch (NumberFormatException e)
         {
            // ignore bad data
         }
         if (sid != null)
         {
            return m_inlineSlots.contains(sid);
         }
      }
      
      return false;
   }
   
   /**
    * Determines whether the template of the specified relationship contains
    * slots.
    *  
    * @param rel the relationship in question; assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the template of the specified relationship
    *    contains slots; otherwise return <code>false</code>.
    */
   private boolean isTemplateWithSlots(PSRelationship rel)
   {
      String templateProperty = rel
            .getProperty(IPSHtmlParameters.SYS_VARIANTID);
      if (!StringUtils.isBlank(templateProperty))
      {
         Long templateId = null;
         try
         {
            templateId = new Long(templateProperty);
         }
         catch (NumberFormatException e)
         {
            // ignore bad data
         }
         if (templateId != null)
         {
            return m_templatesWithSlots.contains(templateId);
         }
      }
      
      return false;
   }
   

   /**
    * Look at the grandparents (or higher ancestors) of an item for possible
    * inclusion. Grandparents are included if:
    * <ul>
    * <li> The template of the relationship is a snippet which contains one or
    * more slots. This may occur where an image or other related field is
    * included in a snippet.
    * <li> The dependent item was reached by following a relationship based on
    * an Inline Slot. This can be determined from the Slot Type field.
    * </ul>
    * 
    * @param parentids the specified parent ids; assumed not <code>null</code>,
    *    but may be empty. Do nothing if it is empty.
    */
   private void addGrandParents(Set<Integer> parentids)
   {
      if (parentids.isEmpty())
         return;
      
      // Get the relationships
      Set<Integer> grandperes = new HashSet<>();
      Integer cparent = null;

      try
      {
         // collecting grand parents 
         List<PSRelationship> rels = getParentRelationships(parentids);
         for (PSRelationship rel : rels)
         {
            Integer grandpere = rel.getOwner().getId();
            if (isInlineSlot(rel) || isTemplateWithSlots(rel))
            {
               grandperes.add(grandpere);
            }
         }
         m_items.addAll(parentids);

         // Removes the items which have already processed from the grand 
         // parents. This is also to avoid infinit recursive calls.
         grandperes.removeAll(m_items);

         if (!grandperes.isEmpty())
         {
            addGrandParents(grandperes);
         }
      }
      catch (PSException e)
      {
         ms_log.error("Problem finding parents for id " + cparent, e);
      }
   }

   /**
    * Performs update operation to the given content id list. It sets the
    * CONTENTLASTMODIFIEDDATE column contain the current date & time.
    * 
    * Note, caller must call {@link #addSpecificIds(Collection)} 
    * and/or {@link #addParents(Collection)} before call this method; otherwise
    * this method does nothing.
    * 
    * @return the IDs of the affected items, never <code>null</code>, but may
    *    be empty.
    */
   public Collection<Integer> touchContentItems()
   {
      IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
      cmsMgr.touchItems(m_items);

      return m_items;
   }
   
   /**
    * Get the items that have been (or will be) touched
    * 
    * @return the collection, never <code>null</code>
    */
   public Collection<Integer> getItemIds()
   {
      return m_items;
   }

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder();

      b.append("<Update: ");
      b.append(m_items.toString());
      b.append(">");

      return b.toString();
   }
}
