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
