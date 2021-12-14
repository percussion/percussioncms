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

package com.percussion.services.workflow.data;


/**
 * The interface for aging transition
 */
public interface IPSAgingTransition extends IPSTransitionBase
{
   /**
    * Get the type of this aging transition.
    * 
    * @return the aging transition type, never <code>null</code>.
    */
   PSAgingTransition.PSAgingTypeEnum getType();
   
   /**
    * Set the type of this aging transition.
    * 
    * @param agingType the aging transition type, may not be <code>null</code>.
    */   
   void setType(PSAgingTransition.PSAgingTypeEnum agingType);
   
   /**
    * Get the aging interval in minutes. this is only useful for types
    * absolute and repeated.
    * 
    * @return the aging time in minutes.
    */
   long getInterval();
   
   /**
    * Set the aging interval in minutes. this is only useful for types
    * absolute and repeated.
    * 
    * @param agingInterval The interval in minutes
    */
   void setInterval(long agingInterval);
   
   /**
    * Get the system field from which to get the aging time. This is only 
    * useful for type systemField.
    * 
    * @return the name of the system field from which to get the aging time,
    *    may be <code>null</code> or empty.
    */
   String getSystemField();
   
   /**
    * Set the system field name
    * 
    * @param name The name, may be <code>null</code> or empty.
    */
   void setSystemField(String name);
}
