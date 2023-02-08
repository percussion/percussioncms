/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
