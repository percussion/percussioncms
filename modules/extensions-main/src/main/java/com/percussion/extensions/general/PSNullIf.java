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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/**
 * Forces the named request parameters' values to <code>null</code>, when their
 * current value is equal to a specified value.  Useful for clearing the
 * "-- Choose --" value of posted HTML &lt;select> tags.
 */
public class PSNullIf extends PSDefaultExtension
   implements IPSRequestPreProcessor
{

   /**
    * Compares the request parameters' values whose names are supplied to the
    * supplied value, and if they match, sets the request parameter's value to
    * <code>null</code>. Comparison is case-sensitive.
    *
    * @param params the parameters for this execution of the extension.  Any
    * number of parameters may be provided.  If <code>null</code>, exit takes
    * no action.<br>
    * <code>params[0]</code> - the value that will be converted to
    * <code>null</code>, if <code>null</code> or empty, exit takes no
    * action.<br>
    * <code>params[1] .. [n]</code> - the name of a request parameter whose
    * value will be set to <code>null</code> if equal to <code>params[0]</code>.
    * Skipped if <code>null</code> or empty.
    *
    * @param request the request context object, used to obtain the parameter
    * values.  If <code>null</code>, exit takes no action.
    *
    * @throws PSParameterMismatchException never
    * @throws PSExtensionProcessingException never
    * @throws PSAuthorizationException never
    * @throws PSRequestValidationException never
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
      PSParameterMismatchException, PSExtensionProcessingException
   {
      if (request == null)
         return;

      if (params == null || params[0] == null)
         return;

      String compareTo = params[0].toString().trim();
      if (0 == compareTo.length())
      {
         // nothing to compare to, just return
         return;
      }

      // loop through the remaining exit parameters, setting the request
      // parameter it names to null if its value matches the compareTo value
      for (int paramIndex = 1;
           paramIndex < params.length && null != params[paramIndex] &&
           params[paramIndex].toString().trim().length() > 0; paramIndex++)
      {
         String paramName = params[paramIndex].toString().trim();
         String paramValue = request.getParameter( paramName );
         if (paramValue != null && compareTo.equals( paramValue ))
         {
            request.printTraceMessage( "...setting " + paramName + " to null" );
            request.setParameter( paramName, null );
         }
      }
   }
}
