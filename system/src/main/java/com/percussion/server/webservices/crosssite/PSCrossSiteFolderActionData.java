/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
