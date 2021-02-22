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

import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMimeContentResult;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.filter.PSFilterException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

/**
 * The legacy assembler is responsible for invoking existing assembler requests
 * as a proxy. The assembler also acts as a marker that lets the assembly
 * service know that there should be no processing of the assembly item in terms
 * of bindings and such.
 * 
 * @author dougrand
 */
public class PSLegacyAssembler extends PSAssemblerBase
{

   @Override
   public IPSAssemblyResult assembleSingle(IPSAssemblyItem item)
   {
      IPSAssemblyTemplate template = item.getTemplate();
      String path = template.getAssemblyUrl();
      Map<String, Object> requestparams = new HashMap<>();
      for (String name : item.getParameters().keySet())
      {
         //Legacy assembler needs to pass empty parameters values
         String value = item.getParameterValue(name, "");
         requestparams.put(name, value);
      }

      // Always override the template id
      requestparams.put(IPSHtmlParameters.SYS_VARIANTID, Long.toString(template
            .getGUID().longValue()));

      if (requestparams.get(IPSHtmlParameters.SYS_AUTHTYPE) == null)
      {
         try
         {
            if (item.getFilter().getLegacyAuthtypeId() != null)
            {
               requestparams.put(IPSHtmlParameters.SYS_AUTHTYPE, Integer
                     .toString(item.getFilter().getLegacyAuthtypeId()));
            }
         }
         catch (PSFilterException e)
         {
            return getFailureResult(item, e.getLocalizedMessage());
         }
      }

      if (path == null)
      {
         return getFailureResult(item, "No assembly url defined");
      }

      path = path.trim();
      
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSInternalRequest ireq = PSServer.getInternalRequest(path, req,
            requestparams, false, null);

      if (ireq == null)
      {
         return getFailureResult(item, "Cannot find legacy assembly application: " + path);
      }
      if (item.isDebug())
      {
         try
         {
            if (!ireq.isBinary(req))
            {
               Document doc = ireq.getResultDoc();
               String xmlstr = PSXmlDocumentBuilder.toString(doc);
               return getMessageResult(item, xmlstr, Status.SUCCESS);
            }
            else
            {
               return getMessageResult(item,
                     "Can't get debug output from binary resource",
                     Status.SUCCESS);
            }
         }
         catch (PSInternalRequestCallException e)
         {
            return getFailureResult(item, e.getLocalizedMessage());
         }
      }
      if (!ireq.isBinary(req))
      {
         ByteArrayOutputStream stream = null;
         try
         {
            stream = ireq.getMergedResult();
            String mimeType = ireq.computeMimeType();
            if (StringUtils.isNotBlank(mimeType))
               item.setMimeType(mimeType);
            else
               item.setMimeType("text/html");
            item.setResultData(stream.toByteArray());
            item.setStatus(Status.SUCCESS);
            return (IPSAssemblyResult) item;
         }
         catch (PSInternalRequestCallException e)
         {
            return getFailureResult(item, e.getLocalizedMessage());
         }
         finally
         {
            IOUtils.closeQuietly(stream);   
         }
      }
      else
      {
         if (ireq.getInternalRequestHandler() instanceof IPSInternalResultHandler)
         {
            IPSInternalResultHandler rh = (IPSInternalResultHandler) ireq
                  .getInternalRequestHandler();
            PSExecutionData resultData = null;
            try
            {
               resultData = rh.makeInternalRequest(ireq.getRequest());
               PSMimeContentResult mimeContent = rh.getMimeContent(
                     resultData, false);
               if (mimeContent == null)
               {
                  item.setMimeType("text/plain");
                  item.setResultData("no content".getBytes());
               }
               else
               {
                  item.setMimeType(mimeContent.getMimeType());
                  item.setResultStream(mimeContent.getContent());
               }
               item.setStatus(Status.SUCCESS);
               return (IPSAssemblyResult) item;
            }
            catch (Exception e)
            {
               return getFailureResult(item, e.getLocalizedMessage());
            }
            finally
            {
               if (resultData != null)
               {
                  resultData.release();
               }
            }
         }
         else
         {
            return getFailureResult(item, "Can't handle request");
         }
      }
   }
}
