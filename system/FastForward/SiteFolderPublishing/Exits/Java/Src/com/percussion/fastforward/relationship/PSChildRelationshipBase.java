/******************************************************************************
 *
 * [ PSChildRelationshipBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.relationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

/**
 * Base class for querying and updating "category"-style auto relationships.
 * 
 * @author James Schultz
 * @since 6.0
 */
public abstract class PSChildRelationshipBase
{
   /**
    * Creates an instance of this class that will use the specified processor
    * for querying and updating relationships.
    * 
    * @param relProcessor processor for querying and updating relationships, not
    *           <code>null</code>
    */
   protected PSChildRelationshipBase(IPSRelationshipProcessor relProcessor)
   {
      if (relProcessor == null)
         throw new IllegalArgumentException(
               "IPSRelationshipProcessor may not be null");
      setRelationshipProcessor(relProcessor);
   }

   /**
    * Finds the definition for a slot given its name, using the assembly
    * service.
    * 
    * @param slotname name of the slot to find. not <code>null</code>, must
    *           exist.
    * @return the slot definition for the specified name
    * @throws PSAssemblyException propagated from assembly service if the slot
    *            is not found
    */
   protected IPSTemplateSlot findSlot(String slotname)
         throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSTemplateSlot slot = asm.findSlotByName(slotname);
      return slot;
   }

   /**
    * Finds the definition for a slot given its name, using the assembly
    * service.
    * 
    * @param templateName name of the template to find. not <code>null</code>,
    *           must exist.
    * @return the template definition for the specified name, never
    *         <code>null</code>
    * @throws PSAssemblyException if the template is not found
    */
   protected IPSAssemblyTemplate findTemplate(String templateName, IPSGuid type)
         throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSAssemblyTemplate template = asm.findTemplateByNameAndType(
            templateName, type);
      return template;
   }

   /**
    * Gets all relationships in the specified slot whose dependent item matches
    * the specified content item, limited to only the current/edit revision of
    * the owner items.
    * 
    * @param dependentContentId content item that is the dependent of all
    *           relationships, assumed not <code>null</code>
    * @param slot slot whose relationships will be queried, assumed not
    *           <code>null</code>
    * @return the set of relationships for the specified slot with the specified
    *         dependent item. never <code>null</code>, may be emptty.
    */
   protected PSRelationshipSet getRelationships(int dependentContentId,
         IPSTemplateSlot slot) throws PSCmsException
   {
      // setup the relationship filter
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependent(new PSLocator(dependentContentId));
      filter.limitToEditOrCurrentOwnerRevision(true);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slot
            .getGUID().longValue()));

      // get the relationships
      PSRelationshipSet relationships = m_relationshipProcessor
            .getRelationships(filter);
      return relationships;
   }

   /**
    * Deletes the specified relationships using the
    * <code>PSRelationshipProcessor</code>.
    * 
    * @param toBeDeleted set of relationships to be deleted. assumed not
    *           <code>null</code>, may be empty.
    * @throws PSCmsException propagated from
    *            <code>PSRelationshipProcessor</code>
    */
   protected void deleteRelationships(PSRelationshipSet toBeDeleted)
         throws PSCmsException
   {
      if (toBeDeleted.size() > 0)
         m_relationshipProcessor.delete(toBeDeleted);
   }

   /**
    * Saves the specified relationships using the
    * <code>PSRelationshipProcessor</code>.
    * 
    * @param toBeSaved set of relationships to be saved. assumed not
    *           <code>null</code>, may be empty.
    * @throws PSCmsException propagated from
    *            <code>PSRelationshipProcessor</code>
    */
   protected void saveRelationships(PSRelationshipSet toBeSaved)
         throws PSCmsException
   {
      if (toBeSaved.size() > 0)
         m_relationshipProcessor.save(toBeSaved);
   }

   /**
    * Extracts the owner content ids from the specified relationships.
    * 
    * @param relationships set whose owner content ids will be extracted.
    *           Assumed not <code>null</code>, may be empty.
    * @return a list of the relationship set's owner content ids. never
    *         <code>null</code> but will be empty if the relationship set is
    *         empty.
    */
   protected List<Integer> extractOwnerIds(PSRelationshipSet relationships)
   {
      // extract owner content ids from the relationship set
      @SuppressWarnings("unchecked")
      Iterator<PSRelationship> iter = relationships.iterator();
      List<Integer> cids = new ArrayList<>();
      while (iter.hasNext())
      {
         PSRelationship rel = iter.next();
         PSLocator owner = rel.getOwner();
         cids.add(owner.getId());
      }
      return cids;
   }

   /**
    * Sets the processor that will be used to query, save, and delete
    * relationships.
    * 
    * @param processor processor for querying and updating relationships, not
    *           <code>null</code>
    */
   private void setRelationshipProcessor(IPSRelationshipProcessor processor)
   {
      if (processor == null)
         throw new IllegalArgumentException(
               "relationship processor may not be null");

      m_relationshipProcessor = processor;
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   protected static final Logger m_log = LogManager.getLogger(PSChildRelationshipBase.class);

   /**
    * Processor for querying and updating relationships. Assigned in ctor, never
    * <code>null</code>.
    */
   private IPSRelationshipProcessor m_relationshipProcessor;

}
