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

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;


/**
 * Strips leading and trailing white space from the supplied string.
 */
public class PSStringTrimmerUdf extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Strips leading and trailing white space from the supplied string. It
    * does this by calling <code>toString()</code> on the supplied object
    * first.
    *
    * @param params A single parameter that will be converted to a String
    *    with the toString method, then trim() is called on this string before
    *    it is returned. If <code>null</code> is supplied, <code>null</code>
    *    is returned.
    *
    * @param request   Not used.
    *
    * @return A copy of the supplied string with all leading and trailing
    *    white space removed.
    *
    * @throws  PSConversionException Never thrown.
    */
   @SuppressWarnings("unused") //param and exception
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if ( size == 0 || null == params[0] )
         return null;

      return params[0].toString().trim();
   }
}
