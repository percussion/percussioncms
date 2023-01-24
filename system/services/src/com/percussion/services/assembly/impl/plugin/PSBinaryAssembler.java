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
package com.percussion.services.assembly.impl.plugin;

import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;

import java.io.InputStream;
import java.util.Map;

import javax.jcr.Property;
import javax.jcr.Value;

import com.percussion.utils.request.PSRequestInfo;
import org.apache.poi.util.IOUtils;

/**
 * This assembler produces a binary output (base64 encoded) from the input
 * item(s). The bindings for $sys.mimetype and $sys.data supply the necessary
 * information.
 * 
 * @author dougrand
 */
public class PSBinaryAssembler extends PSAssemblerBase
{

   @Override
   public IPSAssemblyResult assembleSingle(IPSAssemblyItem item)
   {
      // Use the bound values $sys.binary and $sys.mimetype to
      // process the result
      Map<String, Object> bindings = item.getBindings();
      Object sys = bindings.get("$sys");

      if (!(sys instanceof Map))
      {
         return getFailureResult(item, "$sys was not bound to a map.");
      }

      Map sysmap = (Map) sys;
      Object mtype = sysmap.get("mimetype");
      Object data = sysmap.get("binary");
      String mimetype;
      

      try
      {
         if (mtype == null)
         {
            return getFailureResult(item, "$sys.mimetype was not bound");
         }

         if (data == null)
         {
            return getFailureResult(item, "$sys.binary was not bound");
         }

         if (mtype instanceof Property)
         {
            mimetype = ((Property) mtype).getString();
         }
         else if (mtype instanceof String)
         {
            mimetype = (String) mtype;
         }
         else
         {
            return getFailureResult(item,
                  "$sys.mimetype must be bound to a property"
                        + " or string value");
         }

         if (data instanceof Property)
         {
            PSRequest req = (PSRequest) PSRequestInfo
                    .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
            req.setParameter("allowBinary","true");
            try(InputStream s = ((Property) data).getStream()) {
               item.setResultStream(s);
            }
         }
         else if (data instanceof Value)
         {
            InputStream s = ((Value) data).getStream();
            item.setResultStream(s);
            IOUtils.closeQuietly(s);
         }
         else if (data instanceof byte[])
         {
            byte[] bdata = (byte[]) data;
            item.setResultData(bdata);
         }
         else
         {
            return getFailureResult(item, "$sys.binary must be bound to a property"
                  + " or byte[] value");
         }

         item.setMimeType(mimetype);
         item.setStatus(Status.SUCCESS);
         return (IPSAssemblyResult) item;
      }
      catch (Exception e)
      {
         ms_log.error("Problem extracting data from property",e);
         return getFailureResult(item, "Problem extracting data from property");
      }
   }

}
