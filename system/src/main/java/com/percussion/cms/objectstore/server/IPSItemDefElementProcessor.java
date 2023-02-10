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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemDefinition;

/**
 * Content editors have a number of components. There is always an 
 * item definition, and then there may be one or more child elements.
 * <p>A number of classes process the parent and children into some
 * product. This interface defines methods that a client can define
 * to process parent and child elements.
 * <p>This is called from one of a couple of methods that traverse
 * the item definition and first call
 * method {@link #processParentElement(PSItemDefinition, Object[])}
 * and then call the method
 * {@link #processChildElement(PSItemDefinition, PSItemChild, Object[])} 
 * zero or more times. 
 */
public interface IPSItemDefElementProcessor
{
   /**
    * Process the parent content item. The method accepts an
    * item def with a set of arguments. 
    * 
    * @param def Is the item, must never be <code>null</code>
    * @param args An arg array, may be <code>null</code>
    * @return an appropriate object for the caller of <code>mapElements</code>
    */
   Object processParentElement(PSItemDefinition def, Object args[])
      throws Exception;

   /**
    * Process the child element. The method accepts an
    * item def and item child with a set of arguments.
    * @param def Is the item, must never be <code>null</code>
    * @param child Is the child, must never be <code>null</code>
    * @param args An arg array, may be <code>null</code>
    * @return an appropriate object for the caller of <code>mapElements</code>
    */
   Object processChildElement(
      PSItemDefinition def,
      PSItemChild child,
      Object args[])
      throws Exception;
}
