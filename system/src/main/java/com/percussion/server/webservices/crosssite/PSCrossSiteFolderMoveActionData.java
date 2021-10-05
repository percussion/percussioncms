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
