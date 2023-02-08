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
