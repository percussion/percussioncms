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

package com.percussion.cas;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;

/**
 * Used by server to determine if a request should be cached using the sessionid
 * as one of the keys.
 */
public class PSCacheBySessionRule implements IPSUdfProcessor
{
   // see IPSUdfProcessor doc
   public void init(IPSExtensionDef def, File codeRoot) 
      throws PSExtensionException
   {
      // noop
   }
   
   /**
    * Determines if the supplied request should be cached by session id.  
    * Returns a <code>Boolean</code> whose value indicates the result.  Will be
    * <code>true</code> if the request is for a top-level Site Explorer page or
    * if it is an active snippet within a Site Explorer page.  This is 
    * true if the value of the <code>sys_command</code> parameter is not 
    * <code>null</code> and is equal to "<code>editrc</code>", and one of 
    * the following conditions are also true:
    * <ol>
    * <li>The <code>relateitemid</code> parameter is <code>null</code> 
    * (indicates a top-level page), or</li>
    * <li>The <code>sys_activeitemid</code> is not <code>null</code> and 
    * equal to the <code>relateditemid</code> parameter (indicates an active 
    * snippet)</li>
    * </ol>
    * 
    * @param params The input parameters.  None are expected.
    * @param request The request context, may not be <code>null</code>.
    * 
    * @return A <code>Boolean</code> indicating the result.  Never 
    * <code>null</code>.
    * 
    * @throws PSConversionException If <code>request</code> is 
    * <code>null</code>.
    */
   public Object processUdf(Object[] params, IPSRequestContext request) 
      throws PSConversionException
   {
      if (request == null)
         throw new PSConversionException(0, "request may not be null");
         
      boolean result = false;
      String sysCommand = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (sysCommand != null && 
         sysCommand.equals(PSAddAssemblerInfo.SYS_COMMAND_EDITRC))
      {
         String relatedItemId = request.getParameter("relateditemid");
         String activeItemId = request.getParameter(
            IPSHtmlParameters.SYS_ACTIVEITEMID);
         if (relatedItemId == null)
            result = true;
         else if (activeItemId != null && activeItemId.equals(relatedItemId))
            result = true;
      }
      
      return result;
   }
}
