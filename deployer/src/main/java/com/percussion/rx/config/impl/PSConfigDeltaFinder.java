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
package com.percussion.rx.config.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Configuration delta finder class.
 * @author bjoginipally
 *
 */
public class PSConfigDeltaFinder
{
   /**
    * Returns the delta of new properties and old properties. If a property
    * exists in new property and not in old then it is considered as delta.
    * 
    * @param newProps must not be <code>null</code>.
    * @param oldProps may be <code>null</code> or empty, in which case
    * newProps are returned as delta.
    * @return delta of new and old properties. Never <code>null</code>, may
    * be empty.
    */
   public Map<String, Object> getConfigDelta(Map<String, Object> newProps,
         Map<String, Object> oldProps)
   {
      if (newProps == null)
         throw new IllegalArgumentException("newProps must not be null");
      if (oldProps == null || oldProps.isEmpty())
      {
         return newProps;
      }
      // Propcess delta and return changed props
      Map<String, Object> delta = new HashMap<String, Object>();
      Iterator<String> iter = newProps.keySet().iterator();
      while(iter.hasNext())
      {
         String key = iter.next();
         if(!oldProps.containsKey(key))
         {
            delta.put(key, newProps.get(key));
         }
         else
         {
            Object objN = newProps.get(key);
            Object objO = oldProps.get(key);
            if ((objN == null && objO != null)
                  || (objN != null && (!objN.equals(objO))))
            {
               delta.put(key, objN);
            }
         }
         
      }
      return delta;
   }
}
