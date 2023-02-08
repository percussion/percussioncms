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

import java.util.List;

/**
 * Serializable data object for PSCrossSiteFolderMoveProcessor.
 * See Parent Class.
 * 
 * @author adamgent
 * @see PSCrossSiteFolderMoveActionProcessor
 *
 */
public class PSCrossSiteFolderMoveActionData extends PSCrossSiteFolderActionData
{
   
   private static final long serialVersionUID = 1L;

   public PSLocator getTargetFolderId()
   {
      return m_targetFolderId;
   }

   public void setTargetFolderId(PSLocator targetFolderId)
   {
      m_targetFolderId = targetFolderId;
   }

   public Integer getTargetSiteId()
   {
      return m_targetSiteId;
   }

   public void setTargetSiteId(Integer targetSiteId)
   {
      m_targetSiteId = targetSiteId;
   }

   public List<Integer> getTargetSiteIds()
   {
      return m_targetSiteIds;
   }

   public void setTargetSiteIds(List<Integer> targetSiteIds)
   {
      m_targetSiteIds = targetSiteIds;
   }

   public PSMoveActionCategoryEnum getActionCategory()
   {
      return m_actionCategory;
   }

   public void setActionCategory(PSMoveActionCategoryEnum actionCategory)
   {
      m_actionCategory = actionCategory;
   }
   
   

   @Override
   public String toString()
   {
      return "Move [sourceFolderId="
            + m_sourceFolderId + ", sourceSiteId=" + m_sourceSiteId
            + ", targetFolderId=" + m_targetFolderId + ", targetSiteId="
            + m_targetSiteId + ", actionCategory=" + m_actionCategory + "]";
   }



   /**
    * Target folder locator for the move action being processed, set in the
    * ctor. Never <code>null</code>.
    */
   private PSLocator m_targetFolderId = null;

   /**
    * Target siteid for the action being processed, computed in the ctor.
    * Corresponds to the target folderid. May be <code>null</code> if the
    * target folder is not part of any site.
    */
   private Integer m_targetSiteId = null;
   
   
   private List<Integer> m_targetSiteIds = null;
   

   /**
    * Action category evaluated in the ctor.
    */
   private PSMoveActionCategoryEnum m_actionCategory = null;
}
