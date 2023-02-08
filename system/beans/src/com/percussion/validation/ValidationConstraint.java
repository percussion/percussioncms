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

 
