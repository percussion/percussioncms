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

import java.util.Collection;

/**
 * Defines a component that can be used as a content editor dependency object.
 * This component may be associated with a collection of parameters 
 * (<code>IPSParameter</code> objects).
 */ 
public interface IPSDependentObject extends IPSComponent, IPSReplacementValue
{
   /**
    * Gets the name of this component, unique within a given type. 
    * @return the name, never <code>null</code> or empty.
    */ 
   public String getName();
   
   
   /**
    * Gets a string representation of the type (class) of this component.
    * @return the type, never <code>null</code> or empty.
    */ 
   public String getType();
   
   
   /**
    * Gets the parameters associated with this component.
    * 
    * @return the parameters as a collection of <code>IPSParameter</code> 
    * objects, never <code>null</code>, may be empty.
    */ 
   public Collection getParameters();
}
