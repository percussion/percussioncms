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
package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSComponent;

/**
 * Interface implemented by a class that handles the updating of a type of
 * objectstore component where the old object needs to be updated to the new
 * model. The new component class ctor that takes an element should get the
 * updater by passing the class name.
 */
public interface IPSComponentUpdater
{
   /**
    * This should be called fromXml methods of the component class by passing
    * the just created object. The implementation of this method should check
    * the object and update if needed.
    * 
    */
   public void updateComponent(PSComponent comp);

   /**
    * Determines if this updater supports updating elements to the specified
    * class. Each updater should handle a single class, and it is expected that
    * it will know the legacy XML format of that class.
    * 
    * @param type The type for which the serialized XML requires conversion, may
    *           not be <code>null</code>.
    * 
    * @return <code>true</code> if this converter supports the supplied type,
    *         <code>false</code> if not.
    */
   public boolean canUpdateComponent(Class type);

}
