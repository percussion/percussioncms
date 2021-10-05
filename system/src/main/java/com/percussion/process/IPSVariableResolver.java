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

package com.percussion.process;

import java.util.Map;

/**
 * Interface to be implemented by process parameter resolvers. The framework
 * uses classes implementing this interface in the following way:
 * <p>While parsing the xml:
 * <ol>
 *    <li>Instantiate the class defined as the resolver.</li>
 *    <li>Call {@link #setName(String) setName} with the name supplied in
 * the def.</li>
 *    <li>Call {@link #setValue(String) setValue} with the value supplied in
 * the def. If no value is present, "" is set.</li>
 * </ol>
 * During process instantiation, the {@link #getName()} and {@link 
 * #getValue(Map) getValue} methods are called and their results are passed to
 * the process or process container.
 */
public interface IPSVariableResolver
{
   /**
    * Returns the resolved value using the supplied context.
    *
    * @param value the string to resolve, may be <code>null</code> or empty
    * @param ctx a {@link Map map} that contains data for executing the
    * process, may not be <code>null</code>. Each entry has a <code>String
    * </code> key and a <code>String</code> value. The supplied parameters
    * are dependent upon the context in which the process is executed.
    *
    * @return the resolved string, may be empty, never <code>null</code>
    *
    * @throws PSResolveException if any error occurs resolving the specified
    * string
    */
   public String getValue(String value, Map ctx) throws PSResolveException;
}
