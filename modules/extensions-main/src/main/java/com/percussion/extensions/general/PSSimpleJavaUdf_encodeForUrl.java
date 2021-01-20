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

import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

/**
 * Takes a string and encodes it so it is safe to use as a URL query param value.
 *
 * @author paulhoward
 */
public class PSSimpleJavaUdf_encodeForUrl extends PSSimpleJavaUdfExtension
{
   /**
    * See class description.
    * 
    * @param params Expect 1 param. Will do a <code>toString</code> on it and
    * then encode it for URL use. If <code>null</code> or empty, the empty
    * string is returned.
    * 
    *  @param request Not used.
    */
   public Object processUdf(Object[] params, 
         @SuppressWarnings("unused") IPSRequestContext request)
   {
      try
      {
         if (params == null || params.length == 0)
            return StringUtils.EMPTY;
         String s = params[0].toString();
         String result = URLEncoder.encode(s, "UTF-8");
         //we don't want to encode the path separators
         result = result.replace("%2F", "/");
         return result;
      }
      catch (UnsupportedEncodingException e)
      {
         //should never happen
         throw new RuntimeException(e);
      }
   }
}
