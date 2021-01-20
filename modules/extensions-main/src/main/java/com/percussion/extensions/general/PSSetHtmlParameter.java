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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import java.io.File;

/**
 * This pre-exit sets / creates or removes a single request parameter
 */
public class PSSetHtmlParameter implements IPSRequestPreProcessor
{
   /**
    * This is the implementation of the IPSRequestPreProcessor interface
    * This exit expects two parameters to be passed in: params[0] - parameter
    * name (required), params[1] - parameter value (optional), in case if nothing
    * is passed in, then the exit removes this parameter from the request object.
    *
    * @see IPSRequestPreProcessor
   */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      // expects two string parameters
      String paramName = getParameter(params, 0, true);
      String paramValue = getParameter(params, 1, false);

      if (paramValue!=null)
          request.setParameter(paramName, paramValue);
      else
          request.removeParameter(paramName);
   }


   // see IPSRequestPreProcessor
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // nothing to do
   }


   /**
    * Get a parameter from the parameter array, and return it as a string.
    *
    * @param params array of parameter objects from the calling function,
    * assumed never be <code>null</code>
    * @param index the integer index into the parameters
    * @param required indicates if this parameter is required or not
    *
    * @return a not-null, not-empty string which is the value of the parameter
    * @throws PSParameterMismatchException if the parameter is missing or empty
    **/
   private static String getParameter(Object[] params, int index, boolean required)
         throws PSParameterMismatchException
   {
      if (params.length < index + 1 || null == params[index] ||
            params[index].toString().trim().length() == 0)
      {
         if (required) {
            throw new PSParameterMismatchException(PSSetHtmlParameter.class +
               ": Missing exit parameter");
         }
         else {
            return null;
         }
      }
      else
      {
         return params[index].toString().trim();
      }
   }

}
