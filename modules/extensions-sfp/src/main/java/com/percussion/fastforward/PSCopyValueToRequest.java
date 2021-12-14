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
package com.percussion.fastforward;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;

/**
 * UDF to set the supplied parameter (name-value pair) into the request as HTML
 * paramater.
 */
public class PSCopyValueToRequest
   extends PSDefaultExtension
   implements IPSUdfProcessor
{

   /**
    * Implementation of the interface method. A new HTML parameter is set in the
    * request context. The name of the parameter is the first member of the
    * parameter array and the value is the second member of the array. Both are
    * converted to strings before putting into the request.
    * 
    * @param params - Object[] parameters to the UDF, must have have two non
    *           <code>null</code> and non-empty members.
    * @param request - IPSRequestContext request object, must not be
    *           <code>null</code>.
    * @return Object always "0" to be ignored.
    * @throws PSConversionException
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      if (params.length < 2)
         throw new PSConversionException(0, "Atleast 2 parameters required");
      if (params[0] == null || params[0].toString().trim().equals(""))
         throw new PSConversionException(0, "param name cannot be null");
      if (params[1] == null || params[1].toString().trim().equals(""))
         throw new PSConversionException(0, "param value cannot be null");

      String paramname = params[0].toString();
      String paramvalue = params[1].toString();

      request.setParameter(paramname, paramvalue);

      return "0";
   }
}
