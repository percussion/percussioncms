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
