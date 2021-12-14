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
package com.percussion.cas;

import com.percussion.cms.PSCmsException;
import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSCms;

/**
 * Tests if the addressed content is publishable or not.
 */
public class PSPublishableContent extends PSSimpleJavaUdfExtension
{
   /**
    * This UDF tests whether or not the addressed content is publishable or not.
    * Makes a call to isPublishable method to determine the item's publishable
    * status.
    *
    * @param params[0] a comma separated list of tokens that represent
    *    publishable content, may be <code>null</code> or empty, in which case
    *    the defaults <code>y,i</code> are used.
    * @param params[1] the content id of the item to test, may be
    *    <code>null</code>, in which case the content id of the supplied
    *    request is used. If no valid content id is supplied, an exception is
    *    thrown.
    * @param params[2] the revision of the item to test, may be
    *    <code>null</code>, in which case the current revision is used.
    * @param request the request to operate on, not <code>null</code>.
    * @return a <code>Boolean</code> with a value of <code>true</code> if the
    *    supplied content item is publishable, <code>false</code> otherwise.
    * @throws PSConversionException for any missing or invalid required
    *    parameter and any other error that can occur.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      Boolean result = Boolean.FALSE;
      try
      {
         result = PSCms.isPublishable(params,request);
      }
      catch(PSCmsException e)
      {
         throw new PSConversionException(e.getErrorCode(),e.getErrorArguments());
      }
      return result;
   }
}
