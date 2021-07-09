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

package com.percussion.server.webservices.crosssite;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.webservices.crosssite.PSCrossSiteFolderActionProcessor.ProcessorStatusEnum;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable data object for PSCrossSite Processors.
 * This object holds the current state of the cross site processor and
 * does not contain any behavior.
 * 
 * This object is sent to the queue if the cross site move is to
 * big (see {@link #getDependentItems()}.
 * 
 * @author adamgent
 *
 */
public class PSCrossSiteFolderActionData implements Serializable
{
   /**
    * Safe to serialize
    */
   private static final long serialVersionUID = -8344627688437557286L;

   public PSLocator getSourceFolderId()
   {
      return m_sourceFolderId;
   }

   public void setSourceFolderId(PSLocator sourceFolderId)
   {
      m_sourceFolderId = sourceFolderId;
   }

   public Integer getSourceSiteId()
   {
      return m_sourceSiteId;
   }

   public void setSourceSiteId(Integer sourceSiteId)
   {
      m_sourceSiteId = sourceSiteId;
   }
   
   public List<Integer> getSourceSiteIds()
   {
      return m_sourceSiteIds;
   }

   public void setSourceSiteIds(List<Integer> sourceSiteIds)
   {
      m_sourceSiteIds = sourceSiteIds;
   }

   public List<PSLocator> getChildren()
   {
      return m_children;
   }

   public void setChildren(List<PSLocator> children)
   {
      m_children = children;
   }

   public boolean isHasCrossSiteLinks()
   {
      return m_hasCrossSiteLinks;
   }

   public void setHasCrossSiteLinks(boolean hasCrossSiteLinks)
   {
      m_hasCrossSiteLinks = hasCrossSiteLinks;
   }

   public List<PSAaFolderDependent> getDependentItems()
   {
      return m_dependentItems;
   }

   public void setDependentItems(List<PSAaFolderDependent> dependentItems)
   {
      m_dependentItems = dependentItems;
   }

   public ProcessorStatusEnum getProcessorStatus()
   {
      return m_processorStatus;
   }

   public void setProcessorStatus(ProcessorStatusEnum processorStatus)
   {
      m_processorStatus = processorStatus;
   }

   /**
    * Source folder locator for the action being processed, set in the ctors.
    * Never <code>null</code> after that.
    */
   protected PSLocator m_sourceFolderId = null;

   /**
    * Source siteid for the action being processed, computed in the stor.
    * Corresponds tho the source folder id. May be <code>null</code> if the
    * source folder does not part of any site.
    */
   protected Integer m_sourceSiteId = null;
   
   private List<Integer> m_sourceSiteIds = null;

   /**
    * Target child locators for the action being processed, set in the ctor,
    * never <code>null</code> or empty after that.
    */
   protected List<PSLocator> m_children = null;

   /**
    * @see #hasCrossSiteLinks()
    */
   private boolean m_hasCrossSiteLinks;
   
   /**
    * @see #getDependentItems()
    */
   protected List<PSAaFolderDependent> m_dependentItems = null;

   /**
    * Status of this processor, initialized to
    * {@link ProcessorStatusEnum#PROCESSOR_STATUS_NONE}.
    */
   protected ProcessorStatusEnum m_processorStatus = ProcessorStatusEnum.PROCESSOR_STATUS_NONE;
   
   
}
