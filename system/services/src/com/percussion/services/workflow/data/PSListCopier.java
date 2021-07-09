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

package com.percussion.services.workflow.data;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The list copier is used to copy a list of one type of objects to another list of different type of objects.
 * This is typically used to copy a list of non-persisted objects to a list of persisted objects,
 * where the "persisted" objects are hibernate entity beans and "non-persisted" objects are non-hibernate 
 * entity beans. 
 * See detail for {@link #copyList(List, List)}
 *  
 * @author YuBingChen
 *
 * @param <S> the source object type, which may be non-persisted object type.
 * @param <T> the target object type, which may be persisted object type.
 */
abstract class PSListCopier<S extends IPSCatalogIdentifier, T extends IPSCatalogIdentifier>
{
   /**
    * Copy the properties from the source element to the target element.
    * @param srcElem the source element, not <code>null</code>.
    * @param tgtElem the target element, not <code>null</code>.
    */
   abstract protected void copy(S srcElem, T tgtElem);
   
   /**
    * Converts a list of objects with "source" type to a list of objects with  
    * "target" type.
    * 
    * @param srcList the list of objects with "source" type, not <code>null</code>.
    * 
    * @return the list of converted objects with "target" type, 
    * never <code>null</code>, but may be empty.
    */
   abstract protected List<T> convertList(List<S> srcList);
   
   /**
    * Copy the source list to target list. The copy operation does the following:
    * <ul>
    * <li>Remove objects from the target list where the objects do not exist in the source</li>
    * <li>Copy objects from the source to target list where the objects exist in both source and target lists</li>
    * <li>Add objects from the source to target list where the objects do not exist in the target</li>
    * </ul>
    * 
    * @param srcList the source list, not <code>null</code>.
    * @param tgtList the target list, not <code>null</code>.
    */
   void copyList(List<S> srcList, List<T> tgtList)
   {
      notNull(srcList);
      notNull(tgtList);
      
      List<S> src = new ArrayList<>();
      src.addAll(srcList);
      
      Iterator<T> itTgt = tgtList.iterator();
      while (itTgt.hasNext())
      {
         T tgtElem = itTgt.next();
         if (! isTargetElement(tgtElem))
            continue;
         
         S srcElem = lookUp(tgtElem.getGUID(), src);
         if (srcElem == null)
         {
            itTgt.remove();
         }
         else
         {
            copy(srcElem, tgtElem);
            src.remove(srcElem);
         }
      }
      
      tgtList.addAll(convertList(src));
      
   }

   /**
    * Determines if the specified element is the target element that need to be processed.
    * This is used to filter out the unwanted element from the target list.
    * @param t the possible target element in question.
    * @return <code>true</code> if the element needs to be processed. 
    */
   protected boolean isTargetElement(T t)
   {
      return true;
   }
   
   /**
    * Look up an element from the "source" list.
    * 
    * @param id the ID of the element in question, assumed not <code>null</code>.
    * @param src the "source" list, assumed not <code>null</code>.
    * 
    * @return the object with the specified ID. It may be <code>null</code> if cannot find it in the "source" list.
    */
   private S lookUp(IPSGuid id, List<S> src)
   {
      for (S srcElem : src)
      {
         if (srcElem.getGUID().equals(id))
            return srcElem;
      }
      return null;
   }

}
