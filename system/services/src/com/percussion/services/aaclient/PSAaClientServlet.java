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
package com.percussion.services.aaclient;

import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * TODO
 */
public class PSAaClientServlet extends HttpServlet
{
   @Override
   public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      /* Discard if the connection has closed */
      if (response.isCommitted())
         return;
      String widget = request.getParameter("widget");

      if (!StringUtils.isEmpty(widget))
      {
         try
         {
            PSWidgetHandlerFactory.getHandler(widget).handleRequest(request,
               response);
         }
         catch (Exception e)
         {
            String resp = e.getLocalizedMessage();
            pushResponse(response, resp, "text/plain", 500);
         }
         return;
      }
      // Cannot handle the request
      String resp = "Servlet is not meant to handle the request";
      pushResponse(response, resp, "text/plain", 404);
   }

   public static void pushResponse(HttpServletResponse httpResponse,
      String resp, String ctype, int respCode) throws IOException
   {
      /* Discard if the connection has closed */
      if (httpResponse.isCommitted()) {
         return;
      }

      httpResponse.setContentType(ctype);
      byte[] respBytes = resp.getBytes(StandardCharsets.UTF_8);
      httpResponse.setContentLength(respBytes.length);
      httpResponse.setStatus(respCode);
      OutputStream os = httpResponse.getOutputStream();
      os.write(respBytes);
      os.flush();
   }
}
