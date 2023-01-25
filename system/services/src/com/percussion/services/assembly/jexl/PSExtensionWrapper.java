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
package com.percussion.services.assembly.jexl;

import com.percussion.data.PSConversionException;
import com.percussion.error.PSNotFoundException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;

/**
 * Wrap an existing extension so it can be called from jexl. This is a helper
 * class, which will instantiate the extension and allow a varargs call to
 * invoke the extension function. This can be used as a direct base class as
 * well.
 * 
 * @author dougrand
 */
public class PSExtensionWrapper
{
   /**
    * The processor, never <code>null</code> after construction
    */
   private IPSUdfProcessor m_processor = null;

   /**
    * Create an extension wrapper
    * 
    * @param context the context, never <code>null</code> or empty
    * @param name the name of the extension, never <code>null</code> or empty
    */
   @SuppressWarnings("unchecked")
   public PSExtensionWrapper(String context, String name) {
      if (StringUtils.isBlank(context))
      {
         throw new IllegalArgumentException("context may not be null or empty");
      }
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (context.endsWith("/") == false)
      {
         context += "/";
      }
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      try
      {
         if(emgr != null) {
            Iterator<PSExtensionRef> refs = emgr.getExtensionNames(null, context,
                    "com.percussion.extension.IPSUdfProcessor", name);
            if (refs.hasNext() == false) {
               throw new RuntimeException("Missing extension " + name);
            }
            PSExtensionRef assemblerref = refs.next();
            m_processor = (IPSUdfProcessor) emgr.prepareExtension(assemblerref,
                    null);
         }
      }
      catch (PSExtensionException e)
      {
         throw new RuntimeException("Problem instantiating " + name, e);
      }
      catch (PSNotFoundException e)
      {
         throw new RuntimeException("Missing extension " + name, e);
      }
   }

   /**
    * Call the given extension using the passed varargs
    * 
    * @param args the varargs, never <code>null</code>
    * @return the value returned by the extension
    * @throws PSConversionException
    */
   public Object call(Object... args) throws PSConversionException
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return call(req, args);
   }
   
   /**
    * Call the given extension using the passed varargs and request
    * 
    * @param req the request to use, never <code>null</code>
    * @param args the varargs, never <code>null</code>
    * @return the value returned by the extension
    * @throws PSConversionException
    */
   public Object call(PSRequest req, Object... args) throws PSConversionException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
      return m_processor.processUdf(args, new PSRequestContext(req));
   }
}
