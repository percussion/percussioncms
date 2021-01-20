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
package com.percussion.webdav;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that forwards all "legacy" WebDAV requests to the internal
 * Rhythmyx webdav servlet.  This implementation simply looks for the same 
 * request url under the rx servlet.  Thus a request for "/rxwebdav/foo" is
 * essentially forwarded to "/Rhythmyx" + "/rxwebdav/foo".  
 */
public class PSWebDavForwardServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   /**
    * Initialize the log and WebDAV configuration from the specified 
    * servlet configuration.
    *
    * @param config a servlet configuration object, not <code>null</code>.
    * 
    * @throws ServletException if initialization failed.
    */
   public void init(ServletConfig config) throws ServletException,
         IllegalArgumentException
   {
      super.init(config); 
      m_forwardBase = config.getInitParameter("RhythmyxServletURI");
   }
   
   // see HttpServlet.service(HttpServletRequest req, HttpServletResponse resp)
   protected void service(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      RequestDispatcher rd = 
         req.getSession().getServletContext().getContext(
            m_forwardBase).getRequestDispatcher(req.getRequestURI());
      rd.forward(req, resp);
   }
   
   /**
    * The base url to use to lookup the request dispatcher for forward requests,
    * initialized in the {@link #init(ServletConfig)} method, never modified
    * after that.
    */
   private String m_forwardBase;
}

