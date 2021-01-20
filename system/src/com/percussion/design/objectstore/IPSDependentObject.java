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
