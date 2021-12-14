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
package com.percussion.services.filter;

import com.percussion.utils.guid.IPSGuid;

import java.util.Map;

/**
 * A rule def associates a specific filter rule (stored by the extensions
 * manager) with a set of parameters to be used with it. Rules are used
 * to remove items that do not meet one or more criteria, and to adjust
 * what revisions of items are used.
 * 
 * @see IPSItemFilterRule
 * @author dougrand
 */
public interface IPSItemFilterRuleDef extends Comparable
{
   /**
    * Get the guid representation of this item filter rule def.
    * @return the guid, never <code>null</code>
    */
   public IPSGuid getGUID();

   /**
    * Set the guid representation of the rule def. 
    * @param newguid the new guid, never <code>null</code>
    */
   public void setGUID(IPSGuid newguid); 
   
   /**
    * Get the rule
    * 
    * @return the rule, never <code>null</code>
    * @throws PSFilterException if the rule can't be found
    */
   IPSItemFilterRule getRule() throws PSFilterException;

   /**
    * Get the rule name
    * 
    * @return the name, never <code>null</code>
    * @throws PSFilterException if the rule can't be found
    */
   String getRuleName() throws PSFilterException;

   
   
   /**
    * Set the rule on this def
    * 
    * @param rulename rule name, never <code>null</code> or empty.
    */
   void setRule(String rulename);
   
   /**
    * Get the named param to use with the rule
    * 
    * @param name the name of the param to get
    * @return the param may be empty or <code>null</code>
    */
   String getParam(String name);
   
   /**
    * Get the parameters for this rule. The parameters are returned
    * as a read-only map.
    * @return a read only map of the parameters
    */
   Map<String, String> getParams();

   /**
    * Add or change a parameter for the rule
    * 
    * @param name name of the parameter, never <code>null</code> or empty
    * @param value value, never <code>null</code> or empty
    */
   void setParam(String name, String value);

   /**
    * Remove the parameter for the rule
    * 
    * @param name name of the parameter to remove, never <code>null</code> or
    *           empty
    */
   void removeParam(String name);
   
   /**
    * Get the filter that this rule definition belongs to
    * @return the filter, may be <code>null</code>.
    */
   IPSItemFilter getFilter();
   
   /**
    * Set the associated item filter. Normally this is only 
    * called on new instances of the object.
    * @param filter the filter, may be <code>null</code>
    */
   void setFilter(IPSItemFilter filter);
}
