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
package com.percussion.utils.jsr170;

import javax.jcr.RepositoryException;

/**
 * Interface to be implemented by jcr objects that need to report a size to
 * determine cache status for the assembly service. This is generally used to
 * calculate an approximate size, calculating a true size is impossible in Java
 * without knowing details of each JVM. Implementers should basically report
 * major memory usage such as strings and arrays.
 * 
 * @author dougrand
 * 
 */
public interface IPSJcrCacheItem
{
   /**
    * Return an approximate site in bytes for the component. If implemented by
    * an object that contains other objects, the implementation must recurse,
    * and the child objects should implement this method as well for simplicity.
    * 
    * @return the size in bytes, might be <code>0</code>
    */
   long getSizeInBytes() throws RepositoryException;
}
