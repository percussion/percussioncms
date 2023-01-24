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
 * The IPSResults interface must be implemented by classes which can be
 * used for defining the results a data set will return. There are no
 * methods associated with the object store implementation. It is merely
 * a placeholder to enforce objects of the appropriate type are used.
 *
 * @see PSDataSet
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSResults {
   /**
    * Performs a deep copy. 
    * Implementing classes must override this method if the class contains 
    * objects.
    * clone Interface for IPSResults this method ideally has to be overiden by
    * classes that implements IPSResults otherwise it will do a shallow copy.
    */  
   public Object clone();
}
