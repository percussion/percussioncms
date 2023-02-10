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
