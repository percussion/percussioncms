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
