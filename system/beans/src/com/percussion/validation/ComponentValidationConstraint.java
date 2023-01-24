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

 
