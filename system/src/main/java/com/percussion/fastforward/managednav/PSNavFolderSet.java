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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A set of parent Nav folders for a given page content item.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavFolderSet
{
   /**
    * Default Constructor.
    */
   public PSNavFolderSet()
   {

   }

   /**
    * Adds a new folder to the set.
    * 
    * @param navFolder
    */
   public void add(PSNavFolder navFolder)
   {
      m_folderSet.add(navFolder);
   }

   /**
    * Gets in iterator for this set. The iterator will return all Folders which
    * have been added to the set. This interator should not be used for
    * operations which may modify the set.
    * 
    * @return the iterator
    */
   public Iterator iterator()
   {
      return m_folderSet.iterator();
   }

   /**
    * Removes a folder from the set.
    * 
    * @param navFolder the folder to remove.
    */
   public void remove(PSNavFolder navFolder)
   {
      m_folderSet.remove(navFolder);
   }

   /**
    * Finds the first Nav Folder in the set alphabetically. This method cannot
    * use Comparators here because of the contract with the equals() method.
    * Because the set is expected to be small, a simple linear search is used.
    * 
    * @return the first Nav Folder alphabetically, or <code>null</code> if the
    *         set is EMPTY.
    */
   public PSNavFolder getFirst()
   {
      PSNavFolder first = null;
      Iterator it = this.iterator();
      while (it.hasNext())
      {
         PSNavFolder next = (PSNavFolder) it.next();
         if (first == null || next.getName().compareTo(first.getName()) < 0)
            ;
         {
            first = next;
         }
      }

      return first;
   }

   /**
    * Gets the current size of the Set.
    * 
    * @return the size of the Set.
    */
   public int size()
   {
      return m_folderSet.size();
   }

   /**
    * Determines if the set contains any folders.
    * 
    * @return <code>true</code> if the set is empty.
    */
   public boolean isEmpty()
   {
      return m_folderSet.isEmpty();
   }

   /**
    * The set of PSNavFolder objects.
    */
   private Set m_folderSet = new HashSet();
}
