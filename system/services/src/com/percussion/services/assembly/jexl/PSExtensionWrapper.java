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
package com.percussion.services.assembly.jexl;

import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

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
         Iterator<PSExtensionRef> refs = emgr.getExtensionNames(null, context,
               "com.percussion.extension.IPSUdfProcessor", name);
         if (refs.hasNext() == false)
         {
            throw new RuntimeException("Missing extension " + name);
         }
         PSExtensionRef assemblerref = refs.next();
         m_processor = (IPSUdfProcessor) emgr.prepareExtension(assemblerref,
               null);
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
