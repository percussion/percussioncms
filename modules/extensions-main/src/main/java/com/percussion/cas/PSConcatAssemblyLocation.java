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

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSAssemblyLocation;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSRequestContext;

import java.io.File;

/**
 * This assembly location generator concatenates all provided parameters.
 */
public class PSConcatAssemblyLocation implements IPSAssemblyLocation
{
   // See interface for details
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      m_def = def;
   }

   /**
    * This implementation takes as many parameters defined in the 
    * RXLOCATIONSCHEMEPARAMS table and simply concatenates them together. If
    * no parameters are specified, an empty String will be returned.
    * e.g. params[0] + params[1] + ... + params[n]. There will be no checks
    * made. All parameters provided with backslashes will be transformed to
    * forward slashes.
    */
   public String createLocation(Object[] params, IPSRequestContext request)
      throws PSExtensionException
   {
      String exitName = getClass().getName();
      request.printTraceMessage("Entering " + exitName + ".createLocation");

      StringBuilder location = new StringBuilder();
      try
      {
         for (Object param : params) {
            location.append(param.toString());
         }
         
         request.printTraceMessage("Location= " + location);
      }
      catch (Exception e)
      {
         request.printTraceMessage("Error: " + PSExceptionUtils.getMessageForLog(e));
         
         Object[] args = 
         { 
            m_def.getRef().getExtensionName(), 
            PSExceptionUtils.getMessageForLog(e)
         };
         throw new PSExtensionException(
            IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
      }
      finally
      {
         request.printTraceMessage("Leaving " + exitName + ".createLocation");
      }

      return location.toString();
   }
   
   /**
    * This is the definition for this extension. You may want to use it for
    * validation purposes in the <code>createLocation</code> method.
    */
   protected IPSExtensionDef m_def = null;
}
