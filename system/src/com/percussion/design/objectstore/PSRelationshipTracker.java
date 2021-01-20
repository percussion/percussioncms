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
package com.percussion.design.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSActiveAssemblerProcessor;
import com.percussion.server.PSRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is used to track relationships while copying a site or site
 * subfolder. The collected information will be used for post processes.
 */
public class PSRelationshipTracker
{
   /**
    * Adds a source to target item mapping. This also looks up all 
    * relationships of related content and stores it in its own map.
    * 
    * @param request the request used to do the lookups, not <code>null</code>.
    * @param source the source item that was copied, not <code>null</code>.
    * @param target the target item copied from the source, not 
    *    <code>null</code>.
    * @throws PSCmsException for any error.
    */
   public void addItemMapping(PSRequest request, PSLocator source, 
      PSLocator target) throws PSCmsException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
         
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (target == null)
         throw new IllegalArgumentException("target cannot be null");
      
      Integer sourceId = new Integer(source.getId());
      Integer targetId = new Integer(target.getId());
      Object test = m_sourceTargetItemMap.put(sourceId, targetId);
      if (test == null)
      {
         PSActiveAssemblerProcessor processor = PSActiveAssemblerProcessor.getInstance();
         m_relatedContent.put(sourceId, processor.getRelatedContent(source));
      }
   }
   
   /**
    * Adds a source to target folder mapping.
    * 
    * @param source the source folder that was copied, not <code>null</code>.
    * @param target the target folder copied from the source, not 
    *    <code>null</code>.
    */
   public void addFolderMapping(PSLocator source, PSLocator target)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (target == null)
         throw new IllegalArgumentException("target cannot be null");
      
      m_sourceTargetFolderMap.put(new Integer(source.getId()), target);
   }
   
   /**
    * Convenience method that calls {@link #getFolderTarget(PSLocator) 
    * getFolderTarget(new PSLocator(source)}. 
    */
   public PSLocator getFolderTarget(String source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      return getFolderTarget(new PSLocator(source));
   }
   
   /**
    * Get the locator of the copy for the supplied source.
    * 
    * @param source the source locator for which to get the locator of the 
    *    copied folder, not <code>null</code>.
    * @return the value supplied as the target in the {@link #addFolderMapping(
    *    PSLocator, PSLocator) addFolderMapping(source, target)} method in 
    *    which the locator supplied to this method was the locator supplied 
    *    as the <code>source</code> of the {@link #addFolderMapping(PSLocator, 
    *    PSLocator)} method, may be <code>null</code> if not found.
    */
   public PSLocator getFolderTarget(PSLocator source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      return (PSLocator) m_sourceTargetFolderMap.get(
         new Integer(source.getId()));
   }
   
   /**
    * Get all tracked source id's.
    * 
    * @return an iterator over all source id's tracked as <code>Integer</code>
    *    objects, never <code>null</code>, may be empty.
    */
   public Iterator getItemSources()
   {
      return m_sourceTargetItemMap.keySet().iterator();
   }
   
   /**
    * Get the target id for the supplied source.
    * 
    * @param sourceId the id of the source for which we want the target,
    *    not <code>null</code>.
    * @return
    */
   public Integer getItemTargetId(Integer sourceId)
   {
      if (sourceId == null)
         throw new IllegalArgumentException("sourceId cannot be null");
      
      return (Integer) m_sourceTargetItemMap.get(sourceId);
   }
   
   /**
    * Get an iterator over all relationships tracked for the supplied source id.
    * 
    * @param sourceId the source id for which we want the relationships, not
    *    <code>null</code>.
    * @return an iterator over all relationships as <code>PSRelationship</code>
    *    objects for the supplied source id, never <code>null</code>, may be
    *    empty.
    */
   public Iterator getItemRelationships(Integer sourceId)
   {
      if (sourceId == null)
         throw new IllegalArgumentException("sourceId cannot be null");
      
      return ((PSRelationshipSet) m_relatedContent.get(sourceId)).iterator();
   }
   
   /**
    * Maps source item id's as <code>Integer</code> to target item id's as 
    * <code>Integer</code>. Initialized during construction, never 
    * <code>null</code> after that, updated through 
    * {@link #addItemMapping(PSRequest, PSLocator, PSLocator)}.
    */
   private Map m_sourceTargetItemMap = new HashMap();
   
   /**
    * Maps source folder id as <code>Integer</code> to target folder as 
    * <code>PSLocator</code>. Initialized during construction, never 
    * <code>null</code> after that, updated through 
    * {@link #addFolderMapping(PSLocator, PSLocator)}.
    */
   private Map m_sourceTargetFolderMap = new HashMap();
   
   /**
    * Maps source item id's as <code>Integer</code> to source related content
    * relationships as <code>PSRelationshipSet</code>. Initialized during 
    * construction, never <code>null</code> after that, updated through 
    * {@link #addItemMapping(PSRequest, PSLocator, PSLocator)}.
    */
   private Map m_relatedContent = new HashMap();
}
