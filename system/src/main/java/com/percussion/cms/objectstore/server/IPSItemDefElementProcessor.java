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
