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

