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
package com.percussion.workflow;

import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataExtractionException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.IPSRequestContext;

/**
 * Performs the same functionality as the base class 
 * {@link PSGetCheckoutStatus} except that the first param is the checked out
 * user name, not the content id of the item.
 */
public class PSGetUserCheckoutStatus extends PSGetCheckoutStatus
{
   /**
    * See base class for more info.  The only difference is that this method
    * expects the first parameter to be the checkedout user name.  May be 
    * <code>null</code> or empty.
    */
   @Override
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      // same behavior as base class if no params
      if ( null == params || params.length < 1)
         return "";
      
      String result;
      String checkedoutUser = params[0] == null ? "" : params[0].toString();
      
      try 
      {
         result = getCheckoutStatus(checkedoutUser, params, request);
      }
      catch (PSDataExtractionException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new PSConversionException(e.getErrorCode(),
            e.getErrorArguments());
      }
         
      return result;
   }
}
