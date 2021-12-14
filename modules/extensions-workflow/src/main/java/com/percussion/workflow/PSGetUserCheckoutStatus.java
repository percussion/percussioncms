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
