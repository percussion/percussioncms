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

package com.percussion.validation;

/** 
 * Extends to support detail error message with the label of the component that
 * fails on validation.
 */
public interface ComponentValidationConstraint extends ValidationConstraint
{
   /** 
    * Gets the error message to be posted by the warning dialog when the 
    * validating component contains an invalid value. Should be called by the
    * <code>ValidationFramework</code> when the validation fails on a component.
    * If <code>compLabel</code> is supplied, the error message includes the 
    * label to identify the component.
    * 
    * @param compLabel the label of the component, may be <code>null</code> or
    * empty.
    * 
    * @return the message, never <code>null</code>, may be empty.
    */
   public String getErrorText(String compLabel);
}

 
