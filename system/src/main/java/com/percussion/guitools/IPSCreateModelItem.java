/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.guitools;

/**
 * @author DougRand
 *
 * This interface allows a model to state that it can create new instances
 * of an appropriate type for itself.
 */
public interface IPSCreateModelItem
{
   /**
    * Create a new instance of the appropriate object for the given model.
    * If there is a problem, throws an exception.
    * 
    * @return an instance of the correct class for the model, see the
    * specific model code for details.
    * 
    * @throws InstantiationException if an instance cannot be created.
    */
   Object createInstance() throws InstantiationException;
}
