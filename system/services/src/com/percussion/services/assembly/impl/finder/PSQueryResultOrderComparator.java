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
package com.percussion.services.assembly.impl.finder;

import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItem;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.guidmgr.data.PSLegacyGuid;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Value;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

/**
 * Record and use query result information in sorting the slot items. The
 * content id of the slot item is used to lookup the position of the given
 * content id in the results.
 * 
 * @author dougrand
 */
public class PSQueryResultOrderComparator implements Comparator<ContentItem>
{
   /**
    * Store the ordering information
    */
   private Map<Integer, Integer> m_order = new HashMap<>();

   /**
    * Ctor - extract the ordering information from the results
    * 
    * @param riter the rows from the query, never <code>null</code> and must
    *           contain the field rx:sys_contentid
    */
   public PSQueryResultOrderComparator(RowIterator riter) {
      if (riter == null)
      {
         throw new IllegalArgumentException("riter may not be null");
      }
      int index = 0;
      while (riter.hasNext())
      {
         Row row = riter.nextRow();
         try
         {
            Value v = row.getValue(IPSContentPropertyConstants.RX_SYS_CONTENTID);
            m_order.put((int) v.getLong(), index++);
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException(
                  "error retrieving sys_contentid from results");
         }
      }
   }

   public int compare(ContentItem o1, ContentItem o2)
   {
      PSLegacyGuid first = (PSLegacyGuid) o1.getItemId();
      PSLegacyGuid second = (PSLegacyGuid) o2.getItemId();

      Integer firstpos =  m_order.get(first.getContentId());
      Integer secondpos = m_order.get(second.getContentId());
      
      if (firstpos == null) firstpos = 0;
      if (secondpos == null) secondpos = 0;

      return firstpos - secondpos;
   }

}
