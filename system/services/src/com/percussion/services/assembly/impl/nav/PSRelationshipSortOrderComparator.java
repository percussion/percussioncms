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
package com.percussion.services.assembly.impl.nav;

import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;

import java.util.Comparator;

/**
 * Order two {@link com.percussion.design.objectstore.PSRelationship} objects
 * according to the sort order property
 * 
 * @author dougrand
 */
public class PSRelationshipSortOrderComparator implements Comparator
{
   public int compare(Object o1, Object o2)
   {
      PSRelationship rel1 = (PSRelationship) o1;
      PSRelationship rel2 = (PSRelationship) o2;

      String rank1 = rel1.getProperty(PSRelationshipConfig.PDU_SORTRANK);
      String rank2 = rel2.getProperty(PSRelationshipConfig.PDU_SORTRANK);

      int r1 = Integer.parseInt(rank1);
      int r2 = Integer.parseInt(rank2);

      if (r1 != r2)
      {
         return r1 - r2;
      }
      else
      {
         long d = rel1.getGuid().longValue() - rel2.getGuid().longValue();
         if (d > 0)
            return 1;
         else if (d < 0)
            return -1;
         else
            return 0;
      }
   }
}
