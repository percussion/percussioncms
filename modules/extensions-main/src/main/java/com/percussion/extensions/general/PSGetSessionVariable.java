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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;

/**
 * Rhythmyx pre-exit to fetch an session object and add it to request
 * parameters.
 *
 * See {@link PSGetSessionVariable#preProcessRequest preProcessRequest} for a
 * description.
 */
public class PSGetSessionVariable extends PSDefaultExtension
      implements IPSRequestPreProcessor
{
   /**
    * Gets a session object and adds it to the request parameters as a String.
    *
    * @param params An array providing the input paramters to this exit. Only
    * the first element will be processed, and must be the name of the key under
    * which the object is stored in the session.  This name is also used as the
    * parameter name under which this object is added to the request parameters.
    * The name is obtained by doing a <code>toString</code> on the supplied
    * object.  If <code>null</code> or empty, this method simply returns.  If
    * calling <code>toString</code> on the object returns an empty String, it is
    * not added to the request parameters.
    *
    * @param request The current request context. May not be <code>null</code>.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      if (null == request)
         throw new IllegalArgumentException("request context must not be null");

      // check for blank parameter array
      if (null == params || params.length < 1 || null == params[0])
      {
         return;
      }

      // retrieve first (and only valid) parameter
      String psxparam = params[0].toString().trim();

      /* retreive the id from the session object based upon the key of the html
       * parameter
       */
      Object sessObj = request.getSessionPrivateObject(psxparam);
      if (null == sessObj || sessObj.toString().length() < 1)
      {
         request.printTraceMessage("No session object key with that name");
      }
      else
      {
         // set the parameter value of the html request
         request.setParameter(psxparam, sessObj);
      }

      return;
   }


}
