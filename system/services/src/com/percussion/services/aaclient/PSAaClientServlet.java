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
package com.percussion.services.aaclient;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

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

   static public void pushResponse(HttpServletResponse httpResponse,
      String resp, String ctype, int respCode) throws IOException
   {
      /* Discard if the connection has closed */
      if (httpResponse.isCommitted())
         return;
      try
      {
         httpResponse.setContentType(ctype);
         byte[] respBytes = resp.getBytes("UTF-8");
         httpResponse.setContentLength(respBytes.length);
         httpResponse.setStatus(respCode);
         OutputStream os = httpResponse.getOutputStream();
         os.write(respBytes);
         os.flush();
      }
      finally
      {

      }
   }
}
