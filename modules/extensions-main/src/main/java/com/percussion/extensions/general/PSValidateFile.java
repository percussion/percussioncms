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

import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;

import org.apache.commons.lang.StringUtils;

/**
 * Validates that either no source file was supplied, <code>null</code> or 
 * empty, or that the content of the supplied file is not empty.
 */
public class PSValidateFile extends PSSimpleJavaUdfExtension
{
   /**
    * See {@link #processUdf(Object[], IPSRequestContext)}.
    * @param params the file to validate, may be <code>null</code>.
    */
   public Object processUdf(Object[] params, @SuppressWarnings("unused")
         IPSRequestContext request)
   {
      if (params != null && params[0] instanceof PSPurgableTempFile)
      {
         PSPurgableTempFile tempFile = (PSPurgableTempFile) params[0];
         String sourceFile = tempFile.getSourceFileName();
         // the "source file name" (filename) is required when creating the item
         // but it is not required for updating the item.
         if (StringUtils.isEmpty(sourceFile))
         {
            boolean isUpdate = StringUtils.isNotBlank(request
                  .getParameter(IPSHtmlParameters.SYS_CONTENTID));
            return isUpdate;
         }
         
         return tempFile.length()> 0;
      }
      
      return Boolean.TRUE;
   }
}

