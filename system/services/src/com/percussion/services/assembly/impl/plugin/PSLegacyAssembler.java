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
import java.io.IOException;
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
         try(ByteArrayOutputStream stream = ireq.getMergedResult()){
            String mimeType = ireq.computeMimeType();
            if (StringUtils.isNotBlank(mimeType))
               item.setMimeType(mimeType);
            else
               item.setMimeType("text/html");

            item.setResultData(stream.toByteArray());
            item.setStatus(Status.SUCCESS);
            return (IPSAssemblyResult) item;
         }
         catch (PSInternalRequestCallException | IOException e)
         {
            return getFailureResult(item, e.getLocalizedMessage());
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
