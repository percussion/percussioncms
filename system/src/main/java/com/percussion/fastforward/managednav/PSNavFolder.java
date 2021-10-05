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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;

/**
 * This class represents a parent folder that has a Navon (or Navtree) object in
 * it.
 * 
 * 
 * @author DavidBenua
 *  
 */
public class PSNavFolder
{
   /**
    * construct a nav folder from the folder and navon.
    * @param folder
    * @param navon
    * @throws PSNavException
    */
   public PSNavFolder(PSComponentSummary folder, PSComponentSummary navon)
         throws PSNavException
   {
      if (folder == null)
      {
         throw new PSNavException("null folders are not permitted here.");
      }
      m_folderSummary = folder;

      if (navon == null)
      {
         throw new PSNavException("a Navon must be specified here. ");
      }
      m_navonSummary = navon;
   }

   /**
    * Gets the summary for the folder item.
    * 
    * @return the folder summary. Never <code>null</code>
    */
   public PSComponentSummary getFolderSummary()
   {
      return m_folderSummary;
   }

   /**
    * Gets the summary for the navon item.
    * 
    * @return the navon summary. Never <code>null</code>
    */
   public PSComponentSummary getNavonSummary()
   {
      return m_navonSummary;
   }

   /**
    * Gets the name of the folder.
    * 
    * @return the folder name.
    */
   public String getName()
   {
      return m_folderSummary.getName();
   }

   /**
    * NavFolders are equal if they represent the same folder. This implies that
    * they have the same content id and path. The equals method only checks that
    * the content ids are equal, since the id is guaranteed to be unique.
    * @param o
    * @return
    */
   public boolean equals(Object o)
   {
      if (o instanceof PSNavFolder)
      {
         PSNavFolder other = (PSNavFolder) o;
         if (this.m_folderSummary.getCurrentLocator().getId() == other.m_folderSummary
               .getCurrentLocator().getId())
         {
            return true;
         }
      }
      return false;
   }

   /**
    * this method is over-ridden here to keep the contract between hashCode()
    * and equals().
    * @return
    */
   public int hashCode()
   {
      String hString = this.getName() + "||"
            + String.valueOf(this.m_folderSummary.getCurrentLocator().getId());
      return hString.hashCode();
   }

   /**
    * the folder summary.
    */
   private PSComponentSummary m_folderSummary;

   /**
    * The navon summary.
    */
   private PSComponentSummary m_navonSummary;
}
