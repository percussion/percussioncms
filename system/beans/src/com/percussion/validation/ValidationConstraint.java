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

package com.percussion.validation;

/** 
 * Defines the 2 required methods for all constraint subclasses.
 *
 * @see ValidationFramework
 * @see IntegerConstraint
 * @see StringConstraint
 */
public interface ValidationConstraint
{
   /** 
    * Gets the error message to be posted by the warning dialog when the 
    * validating component contains an invalid value. Should be called by the
    * <code>ValidationFramework</code> when the validation fails on a component.
    * 
    * @return the message, never <code>null</code>, may be empty.
    */
   public String getErrorText();

   /** 
    * Validates the value of the component passed in. 
    *
    * @param comp the component to check, may not be <code>null</code> and must
    * be an instance of supported component of the implementor.
    * 
    * @throws IllegalArgumentException if the component is not a excepted 
    * component by the implementor.
    * 
    * @throws ValidationException if the component value does not pass the
    * validation.
    */
   public void checkComponent(Object comp) throws ValidationException;

}

 
