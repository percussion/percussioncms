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
package com.percussion.relationship.annotation;

import com.percussion.design.objectstore.PSRelationshipConfig;


/**
 * Endpoint defines which relationships the current item is part of
 * should be processed by the effect.
 * <p>
 * Endpoint is ignored if not one of the following contexts
 * {@link PSEffectContext#PRE_WORKFLOW}, {@link PSEffectContext#POST_WORKFLOW}
 * {@link PSEffectContext#PRE_CHECKIN} and {@link PSEffectContext#POST_CHECKOUT}.
 * <p>
 * For the current item being processed the effect will fire for
 * each relationship of the type the effect is assigned to where
 * the item is the owner, dependent or both for the relationship.
 * <p>
 * For a folder relationship OWNER will never fire because folder items
 * are not involved in workflow.  
 * <p>
 * For an item in a folder, Dependent endpoint effects 
 * will fire for each folder relationship, e.g. each folder the item is in.
 * <p>
 * For active assembly relationships owner effects will fire for each item in 
 * an AA slot on the current item, and dependent will fire for each slot the current
 * item has been added to in other relationships.
 * 
 */
public enum PSEndpoint 
{
   /**
    * See {@link PSRelationshipConfig#ACTIVATION_ENDPOINT_OWNER}
    */
   OWNER(PSRelationshipConfig.ACTIVATION_ENDPOINT_OWNER), // Old UI calls this Direction - Down
   
   /**
    * See {@link PSRelationshipConfig#ACTIVATION_ENDPOINT_DEPENDENT}
    */
   DEPENDENT(PSRelationshipConfig.ACTIVATION_ENDPOINT_DEPENDENT), // Old UI calls this Direction - Up
   
   /**
    * See {@link PSRelationshipConfig#ACTIVATION_ENDPOINT_EITHER}
    */
   BOTH(PSRelationshipConfig.ACTIVATION_ENDPOINT_EITHER),

   /**
    * User defined endpoint, which can be {@link #OWNER}, {@link #DEPENDENT} or {@link #BOTH}.
    */
   USER("user");
   
   /**
    * Gets the name of the endpoint.
    * @return the name of the endpoint. It can never be <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   private PSEndpoint(String name)
   {
      m_name = name;
   }

   private String m_name;
}
