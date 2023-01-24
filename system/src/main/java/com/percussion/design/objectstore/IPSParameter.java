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

package com.percussion.design.objectstore;

/**
 * This interface represents a parameter whose value is represented by a
 * <code>IPSReplacementValue</code>.
 */
public interface IPSParameter extends Cloneable
{
   /**
    * Gets the value associated with this parameter.
    *
    * @return this parameter's value, never <code>null</code>.
    */
   public IPSReplacementValue getValue();
   
 
   /**
    * Sets the value associated with this parameter.
    *
    * @param value the new parameter value, not <code>null</code>.
    * @throws IllegalArgumentException if the value is <code>null</code>.
    */
   public void setValue(IPSReplacementValue value);
   
   
   /**
    * Creates a new instance of this object, deep copying all member variables.
    * @return a clone of this instance.
    */
   @Deprecated
   public Object clone();
}
