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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.servlets;

import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSBaseResponse;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jndi.PSJndiObjectLocator;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.xml.PSEntityResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author dougrand
 * 
 * Handle application requests
 */
public class PSAppServlet extends HttpServlet
{
   /**
    * serial version id
    */
   private static final long serialVersionUID = 3760848952157025078L;

   /**
    * Logger to use, never <code>null</code>.
    */
    private static final Logger ms_log = LogManager.getLogger(PSAppServlet.class);

   /**
    * These parameters are placed in the per-thread log4j MDC context. This is
    * primarily for debugging purposes.
    */
   static String[] ms_parameters =
   {IPSHtmlParameters.SYS_CONTENTID, IPSHtmlParameters.SYS_REVISION,
         IPSHtmlParameters.SYS_AUTHTYPE, IPSHtmlParameters.SYS_SITEID,
         IPSHtmlParameters.SYS_CONTEXT, IPSHtmlParameters.SYS_COMMUNITYID};

   
   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   protected void service(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException
   {

      // Get the PSRequest created by the security filter
      PSRequest psreq = (PSRequest) PSRequestInfo.getRequestInfo(
         PSRequestInfo.KEY_PSREQUEST);
      String filename  = req.getParameter("fileName");
      if (psreq == null)
      {
         // this should never happen
         throw new RuntimeException(
            "The request was not properly initialized by the security filter");
      }
      
      PSRequest origRequest = null;
      try
      {
         //check for an include request
         PSRequest includeReq = psreq.getRequestForIncludeURI(req, res);
         if (includeReq != null)
         {
            origRequest = psreq;
            psreq = includeReq;
            PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, 
               includeReq);
         }
         
         try
         {
            psreq.parseBody();
         }
         catch (PSRequestParsingException e)
         {
            throw new ServletException(e);
         }
         /* find the appropriate handler for this request */
         IPSRequestHandler rh = PSServer.getRequestHandler(psreq);
         if (rh == null)
         {

             res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
         }
         else
         {
               rh.processRequest(psreq);
         }


         /* Handle the output from the request */
         PSResponse psresp = psreq.getResponse();
         
         /* Discard if the connection has closed */
         if (res.isCommitted()) return;


         /* First set the status, as setting headers may cause the buffer to 
          * flush and the response's status line to be sent
          */
         int status = psresp.getStatusCode();
         if (status==HttpServletResponse.SC_NOT_FOUND)
         {
             getServletContext().getNamedDispatcher("default").forward(req, res);
             return;
         }
         res.setStatus(psresp.getStatusCode());
         
         /* Then output all the headers, this includes the content type */
         outputHeaders(res, psresp.getGeneralHeaders());
         outputHeaders(res, psresp.getEntityHeaders());
         outputHeaders(res, psresp.getResponseHeaders());
         if(filename != null && filename != "" ){
            Map headers = new HashMap();
            String contentType = (String) psresp.getEntityHeaders().get("Content-Type");
             if(contentType != null && contentType != "null" && !contentType.contains("image") && !contentType.contains("pdf")){
                headers.put("Content-Disposition", "attachment; filename="+filename);
                outputHeaders(res, headers);
             }
         }


         /* Output the content length */
         res.setContentLength((int) psresp.getContentLength());
         
         /* Set the 'Accept-Ranges' response header if it hasn't been set.
          * See CML-2894.
          */
         if (!res.containsHeader(PSBaseResponse.RHDR_ACCEPT_RANGES))
         {
            res.setHeader(PSBaseResponse.RHDR_ACCEPT_RANGES, "bytes");
         }

         /* Send the response data */
         psresp.send(res.getOutputStream());
      }
      catch (Exception e)
      {
         if (PSServletUtils.isClientAbortException(e))
         {
            ms_log.debug("Client abort exception:", e);
         }
         else 
         {
            throw new ServletException("ServletError", e);
         }
      }
      finally
      {
         // psreq.getRequestTimer().stop();
         ms_log.debug("Request " + psreq.getRequestFileURL() + " took "
               + psreq.getRequestTimer().toString());

         psreq.release();
         
         if (origRequest != null)
         {
            PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, 
               origRequest);
         }
      }
   }

   /**
    * Output headers to servlet response
    * 
    * @param res the servlet response, assumed not <code>null</code>
    * @param headers the headers, assumed not <code>null</code>
    */
   @SuppressWarnings(value={"unchecked"})
   private void outputHeaders(HttpServletResponse res, Map headers)
   {
      if (headers == null)
         return;
      Iterator entryiter = headers.entrySet().iterator();

      while (entryiter.hasNext())
      {
         Map.Entry entry = (Entry) entryiter.next();
         res.setHeader((String) entry.getKey(), (String) entry.getValue());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
    */
   public void init(ServletConfig conf) throws ServletException
   {
      // first init base services
      boolean initialized = false;
      try
      {
         super.init(conf);

         
         PSServer.setRxDir(PathUtils.getRxDir(null));
         
         PSEntityResolver.setResolutionHome(PathUtils.getRxDir(null));
         
         // initialize jndi prefix
         String jndiLookupPrefix = conf.getInitParameter("jndiPrefix");
         PSJndiObjectLocator.setPrefix(jndiLookupPrefix);
         
         // Initialize servlet utils
         PSServletUtils.initialize(conf.getServletContext());

         // Initialize Spring
         initialized = PSBaseServiceLocator.isInitialized();
         
         initialized = true;
      }
      catch (RuntimeException e)
      {
         ms_log.error("Unexpected error during initialization: " + 
            e.getLocalizedMessage(), e);
         throw new ServletException(e);
      }      
      finally
      {
         if (!initialized)
         {
            // this will asynchronously tell JBoss to shutdown once it's up
            PSServer.scheduleShutdown(0);
            return;
         }
      }
      
      // now we can initialize the application engine
      initialized = PSServer.init(conf, new String[] {}, ~0);
      if (!initialized)
      {
         ms_log.warn("Server did not initialize");
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.Servlet#destroy()
    */
   public void destroy()
   {
      super.destroy();

      PSServer.shutdown();
   }

   /**
    *  See {@link HttpServlet#HttpServlet()}  
    */
   public PSAppServlet() 
   {
      super();
   }
}
