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
package com.percussion.services.guidmgr;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for various guid operations.
 */
public class PSGuidUtils
{
   /**
    * Convenience method that loads the Guid manager and calls its method with
    * the same signature. See that
    * {@link IPSGuidManager#makeGuid(long, PSTypeEnum) method} for a
    * description.
    */
   public static IPSGuid makeGuid(long id, PSTypeEnum type)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return mgr.makeGuid(id, type);
   }
   
   /**
    * Convenience method that loads the Guid manager and calls its method with
    * the same signature. See that
    * {@link IPSGuidManager#makeGuid(String, PSTypeEnum) method} for a
    * description.
    */
   public static IPSGuid makeGuid(String id, PSTypeEnum type)
   {
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return mgr.makeGuid(id, type);
   }
   
   /**
    * Get an list of ids from the supplied summaries in the same order.
    * 
    * @param summaries the summaries for which to create a list of ids,
    *    not <code>null</code>, may be empty.
    * @return a list of ids in the same order as the supplied summaries,
    *    never <code>null</code>, may be empty.
    */
   public static List<IPSGuid> getIds(List summaries)
   {
      if (summaries == null)
         throw new IllegalArgumentException("summaries cannot be null");
      
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      
      Iterator walker = summaries.iterator();
      while (walker.hasNext())
      {
         Object summary = walker.next();
         if (!(summary instanceof IPSCatalogSummary))
            throw new IllegalArgumentException(
               "all summary elements must be of type IPSCatalogSummary");
         
         ids.add(((IPSCatalogSummary) summary).getGUID());
      }
        
      return ids;
   }

   /**
    * Transform the supplied guid array into a long array.
    * 
    * @param ids the guids to be transformed, not <code>null</code>, may be
    *    empty.
    * @return an array of long values for the supplied guids in the same order,
    *    never <code>null</code>, may be empty.
    */
   public static Long[] toLongArray(IPSGuid[] ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      Long[] longArray = new Long[ids.length];
      for (int i=0; i<ids.length; i++)
         longArray[i] = new Long(ids[i].longValue()); 
      
      return longArray;
   }
   
   /**
    * Transform the supplied guid list into a long array.
    * 
    * @param ids the list of guids to be transformed, not <code>null</code>, 
    *    may be empty.
    * @return an array of long values for the supplied ids in the same order,
    *    never <code>null</code>, may be empty. All long values include the
    *    complete guid including UUID, type ID and host ID.
    */
   public static long[] toLongArray(List<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      long[] longArray = new long[ids.size()];
      for (int i=0; i<ids.size(); i++)
      {
         PSDesignGuid id = new PSDesignGuid(ids.get(i));
         longArray[i] = id.getValue();
      }
      
      return longArray;
   }

   /**
    * Transform the supplied guid list into a long list of guid long values.
    * 
    * @param ids the guids to be transformed, not <code>null</code>, may be
    *    empty.
    * @return a list of long values for the supplied guids in the same 
    *    order, never <code>null</code>, may be empty.
    */
   public static List<Long> toLongList(List<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      List<Long> longList = new ArrayList<Long>();
      for (int i=0; i<ids.size(); i++)
         longList.add(new Long(ids.get(i).longValue())); 
      
      return longList;
   }

   /**
    * Transform the supplied guid list into a long list of full guid values, 
    * including UUID, type and hostid.
    * 
    * @param ids the guids to be transformed, not <code>null</code>, may be
    *    empty.
    * @return a list of long values for the supplied guids in the same 
    *    order, never <code>null</code>, may be empty.
    */
   public static List<Long> toFullLongList(List<IPSGuid> ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      List<Long> longList = new ArrayList<Long>();
      for (int i=0; i<ids.size(); i++)
         longList.add(new Long(new PSDesignGuid(ids.get(i)).getValue())); 
      
      return longList;
   }
   
   /**
    * Convert the supplied array of ids into a list of guid's.
    * 
    * @param ids the array of ids to convert, not <code>null</code>, may be
    *    empty. All ids must specify the object type.
    * @return a list of guid's, never <code>null</code>, may be empty.
    */
   public static List<IPSGuid> toGuidList(long[] ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      List<IPSGuid> guidList = new ArrayList<IPSGuid>();
      for (int i=0; i<ids.length; i++)
         guidList.add(new PSDesignGuid(ids[i]));
      
      return guidList;
   }
   
   /**
    * Convert the supplied array of ids into a list of guid's of the specified
    * type.
    * 
    * @param ids the array of ids to convert, not <code>null</code>, may be
    *    empty.
    * @param type the type of guid's to convert to, not <code>null</code>.
    * @return a list of guid's of the specified type, never <code>null</code>,
    *    may be empty.
    */
   public static List<IPSGuid> toGuidList(long[] ids, PSTypeEnum type)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids cannot be null");
      
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");
      
      List<IPSGuid> guidList = new ArrayList<IPSGuid>();
      for (int i=0; i<ids.length; i++)
         guidList.add(new PSGuid(type, ids[i]));
      
      return guidList;
   }
   
   /**
    * Test if the supplied ids are 'blank', meaning <code>null</code> or empty.
    * 
    * @param ids the ids array to test for blank, may be <code>null</code> or
    *    empty.
    * @return <code>true</code> if the supplied ids are blank, 
    *    <code>false</code> otherwise.
    */
   public static boolean isBlank(IPSGuid[] ids)
   {
      return (ids == null || ids.length == 0);
   }
   
   /**
    * Test if the supplied ids are 'blank', meaning <code>null</code> or empty.
    * 
    * @param ids the ids list to test for blank, may be <code>null</code> or
    *    empty.
    * @return <code>true</code> if the supplied ids are blank, 
    *    <code>false</code> otherwise.
    */
   public static boolean isBlank(List<IPSGuid> ids)
   {
      return (ids == null || ids.isEmpty());
   }
   
   /**
    * Get a list of guids for the supplied list of catalog items.
    * 
    * @param items the list of catalog items from which to get a list of
    *    guids, not <code>null</code>, may be empty, all elements must
    *    implement the <code>IPSCatalogItem</code> interface.
    * @return a list of guids in the same order as the supplied catalog items,
    *    never <code>null</code>, may be empty.
    */
   public static List<IPSGuid> toGuidList(List items)
   {
      if (items == null)
         throw new IllegalArgumentException("items cannot be null");
      
      List<IPSGuid> guidList = new ArrayList<IPSGuid>();

      Iterator walker = items.iterator();
      while (walker.hasNext())
      {
         Object item = walker.next();
         if (!(item instanceof IPSCatalogItem))
            throw new IllegalArgumentException(
               "all item elements must be of type IPSCatalogItem");
         
         guidList.add(((IPSCatalogItem) item).getGUID());
      }
      
      return guidList;
   }

   /**
    * Get a list of guids from the supplied guid.
    * 
    * @param guid The guid, may not be <code>null</code>.
    * 
    * @return A list containing the supplied guid, never <code>null</code>.
    */
   public static List<IPSGuid> toGuidList(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      guids.add(guid);
      
      return guids;
   }

   /**
    * Convert the supplied array of ids into a list of legacy guid's.
    * 
    * @param ids the array of ids to convert, not <code>null</code>, may be
    *    empty. All ids must specify a legacy guid.
    *    
    * @return a list of legacy guid's, never <code>null</code>, may be empty.
    */
   public static List<IPSGuid> toLegacyGuidList(long[] ids)
   {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");
      
      List<IPSGuid> guidList = new ArrayList<IPSGuid>(ids.length);
      for (long id : ids)
      {
         guidList.add(new PSLegacyGuid(id));
      }
      
      return guidList;
   }
}

