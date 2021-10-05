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

package com.percussion.cms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Container for a set of plans used to process different modify requests for
 * a particular display mapper.
 */
public class PSModifyPlanSet
{
   /**
    * Constructor for this class.
    */
   public PSModifyPlanSet()
   {
   }

   /**
    * Adds the plan to this set.
    *
    * @param plan The plan to add.  A plan with the same type must not have
    * already been added.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if plan is <code>null</code> or if a
    * plan with the same type has already been added.
    */
   public void addPlan(PSModifyPlan plan)
   {
      if (plan == null)
         throw new IllegalArgumentException("plan may not be null");

      Integer key = new Integer(plan.getType());

      if (m_plans.containsKey(key))
         throw new IllegalArgumentException("plan type already added");

      m_plans.put(key, plan);
   }

   /**
    * Returns the plan stored for the given type.
    *
    * @param type The type of plan.  See {@link PSModifyPlan} for info on types.
    *
    * @return The plan matching that type.  May be <code>null</code> if a plan
    * for that type has not been added.
    */
   public PSModifyPlan getPlan(int type)
   {
      return (PSModifyPlan)m_plans.get(new Integer(type));
   }

   /**
    * Returns an Iterator over <code>zero</code> or more PSModifyPlan objects,
    * which is all the plans that have been added to this set.
    *
    * @return The Iterator, never <code>null</code>, may be empty.
    */
   public Iterator getAllPlans()
   {
      return m_plans.values().iterator();
   }

   /**
    * Map of plans where key is the type and value is a PSModifyPlan object,
    * never <code>null</code>.
    */
   private Map m_plans = new HashMap();

}
