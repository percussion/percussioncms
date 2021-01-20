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
package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;


public class PSBinaryChooser extends PSDefaultExtension
   implements IPSUdfProcessor
{
   /**
    * Compares the first param to the 2nd param as Strings. If they are equal,
    * the 3rd param is returned, otherwise the 4th param is returned. The
    * comparison is performed case insensitive. If the first param is empty or
    * <code>null</code>, the 3rd param is returned w/o performing a compare.
    * <p>This was written to allow setting an attribute to different values
    * based on the data in a column.
    *
    * @param params The first one can be <code>null</code>, but the other 3
    *    must be non-<code>null</code>. toString is called on
    *    the first 2 before the comparison. 3 and 4 are returned as is.
    *
    * @param request Never <code>null</code>.
    *
    * @throws PSConversionException If 3, non-<code>null</code> params
    *    are not found.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if (null == params || params.length != 4
            || params[1] == null || params[2] == null || params[3] == null)
      {
         throw new PSConversionException(0, "Need 4, non-null params");
      }

      if (params[0] == null || params[0].toString().trim().length() == 0
            || params[0].toString().equalsIgnoreCase(params[1].toString()))
      {
         return params[2];
      }
      else
         return params[3];
   }
}
