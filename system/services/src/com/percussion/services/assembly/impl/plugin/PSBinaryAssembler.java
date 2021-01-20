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
            InputStream s = ((Property) data).getStream();
            item.setResultStream(s);
            IOUtils.closeQuietly(s);
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
