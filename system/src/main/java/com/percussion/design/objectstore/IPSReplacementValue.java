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
 * The IPSReplacementValue interface must be implemented by any class which
 * can be used as a replacement value for conditionals or exit parameters
 * at run-time.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSReplacementValue extends Cloneable {
   /**
    * Get the type of replacement value this object represents.
    */
   public String getValueType();

   /**
    * Get the text which can be displayed to represent this value.
    */
   public String getValueDisplayText();

   /**
    * Get the implementation specific text which for this value.
    */
   public String getValueText();
     
   /**
    * Creates a new instance of this object, deep copying all member variables.
    * If an implementing class has mutable member variables, it must override 
    * this method and clone() each of those variables.  This method will create
    * a shallow copy if it is not overridden.
    * 
    * @return a deep-copy clone of this instance, never <code>null</code>.
    */
   public Object clone();
}

