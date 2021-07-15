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
package com.percussion.extensions.general;

import org.apache.commons.lang.StringUtils;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;


/**
 * Checks if a string is empty, null, or whitespace.
 */
public class PSValidateString extends PSSimpleJavaUdfExtension
{
  /* ************ IPSUdfProcessor Interface Implementation ************ */

  /**
   * Checks if the supplied string is empty, null, or whitespace. It
   * does this by calling <code>toString()</code> on the supplied object first.
   *
   * @param params A single parameter that will be converted to a String
   *   with the toString method, then <code>trim()</code> is called on this string.
   *
   * @param request Not used.
   *
   * @return An Object of type Boolean which is <code>false</code> if the string 
   * is invalid (empty, null, or whitespace), <code>true</code> otherwise.
   *
   * @throws  PSConversionException Never thrown.
   */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      return params[0] != null && StringUtils.isNotBlank(params[0].toString());
   }
}
