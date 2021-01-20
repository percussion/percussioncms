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
package com.percussion.extension;

/**
 * Marker interface for jexl function bindings. Classes marked with this
 * interface are usually registered with the extensions manager for use in the
 * evaluation of jexl expressions. The classes are instantiated and bound to a
 * set naming scheme. $rx.name is bound for each system context class and
 * $user.name is bound for each user context class. 
 * <p>
 * For example, $rx.codec is
 * bound to the implementation class for codec utilities. This bound name can
 * then be dereferenced to call methods, e.g. $rx.codec.base64Decoder(string)
 * will do a base64 decode of the passed string.
 * <p>
 * The velocity tools are typically also bound, but they do not implement this
 * marker interface.
 * 
 * @author dougrand
 * 
 */
public interface IPSJexlExpression extends IPSExtension
{
   // No methods defined
}
