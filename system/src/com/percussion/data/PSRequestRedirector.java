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

package com.percussion.data;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRequestLink;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.error.PSResponseSendError;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSURLEncoder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The PSRequestRedirector class takes the current request context, adds
 * any additional data, then fires the chained request. This is most
 * commonly used by the update handler to return an update query result set
 * upon successful updating.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSRequestRedirector implements IPSResultGenerator
{
   /**
    * Construct a request redirector. This takes the current request
    * context, adds any additional data, then fires the chained request.
    * This is most commonly used by the update handler to return an update
    * query result set upon successful updating.
    *
    * @param      app      the application containing the data set we will
    *                      be linking to
    *
    * @param      link     the link definition
    */
   public PSRequestRedirector(PSApplicationHandler ah, PSRequestLink link)
      throws PSNotFoundException, PSIllegalArgumentException
   {
      super();

      m_appHandler = ah;

      /* store the target data set name so we can get its handler
       * at runtime
       */
      m_targetDataSetName = link.getTargetDataSet();
      PSDataSet ds = ah.getDataSetDefinition(m_targetDataSetName);

      // store the request page info so we can set it at runtime
      PSRequestor requestor = ds.getRequestor();

      m_requestURL = PSServer.makeRequestRoot(ah.getRequestRoot());
      if (m_requestURL == null)
         m_requestURL = "";
      m_requestURL += "/" + requestor.getRequestPage();
      int pos = requestor.getRequestPage().lastIndexOf('.');

      // are they forcing the extension to use?
      m_appendURLExtension = (pos == -1);

      PSApplication app = m_appHandler.getApplicationDefinition();
      m_requestTypeParam = app.getRequestTypeHtmlParamName();
      switch (link.getType()) {
         case PSRequestLink.RL_TYPE_UPDATE:
            m_requestTypeValue = app.getRequestTypeValueUpdate();
            break;

         case PSRequestLink.RL_TYPE_INSERT:
            m_requestTypeValue = app.getRequestTypeValueInsert();
            break;

         case PSRequestLink.RL_TYPE_DELETE:
            m_requestTypeValue = app.getRequestTypeValueDelete();
            break;

         // case PSRequestLink.RL_TYPE_QUERY:
         default:
            m_requestTypeValue = null;
            break;
      }

      m_useHttpRedirectResponse = link.useHttpResponseForRedirection();
   }

   /**
    * Generate the results for this request.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    */
   public void generateResults(PSExecutionData data)
   {
      PSRequest request = data.getRequest();

      /* store the target data set name as the data set name so we can
       * efficiently access it in the app handler
       */
      request.setCgiVariable(IPSCgiVariables.CGI_PS_DATA_SET_NAME, m_targetDataSetName);

      // we also need the request file URL
      String url = m_requestURL;
      // if required, append the extension (use the same as this req)
      if (m_appendURLExtension)
         url += request.getRequestPageExtension();
      request.setRequestFileURL(url);

      // disable cache reading (as we are probably requerying updated data
      request.setCgiVariable(IPSCgiVariables.CGI_HTTP_PRAGMA, "no-cache");

      // and we need to set/remove the request type param based on target
      java.util.HashMap params = request.getParameters();
      if (m_requestTypeParam != null)
      {
         if (m_requestTypeValue == null)
         {
            if (params != null)   // check for null (bug id TGIS-4BSTPH)
               params.remove(m_requestTypeParam);
         }
         else
         {
            if (params == null)   // check for null (bug id TGIS-4BSTPH)
            {
               params = new java.util.HashMap();
               request.setParameters(params);
            }
            params.put(m_requestTypeParam, m_requestTypeValue);
         }
      }

      /* Check if this redirector specifies http redirect and use it if 
         it does, otherwise call the app handler directly */
      if (m_useHttpRedirectResponse)
         sendHttpRedirectResponse(request, url);
      else
      {
         /* it may at first seem inefficient to use the app handler to get the
          * handler for the target data set. However, the app handler is where
          * the security is contained. We don't want to bypass security so we
          * are going through this components.
          */
         m_appHandler.processRequest(request);
      }
   }

   /**
    * Send a redirect (See Other) message for this request.
    * 
    * @param request The request context associated with this request, not
    *    <code>null</code>.
    * @param url The url associated with this request, not <code>null</code>
    */
   public void sendHttpRedirectResponse(PSRequest request, String url)
   {
      if (request == null)
         throw new IllegalArgumentException("Request must be specified.");

      if (url == null)
         throw new IllegalArgumentException(
            "Url must be specified for redirection.");
      
      // and we need to set/remove the request type param based on target
      Map params = request.getParameters();

      if (params != null)
      {
         // run through the parameters and tack them onto the url
         Iterator i = params.entrySet().iterator();
         String sepStr = "";
         
         if (url.indexOf("?") > -1)
            sepStr = "&";
         else
            sepStr = "?";
            
         while (i.hasNext())
         {
            Map.Entry e = (Map.Entry) i.next();
            Object value = e.getValue();
            if (value == null)
               continue;
            
            if (value instanceof List)
            {
               List l = (List) value;
               Iterator listIt = l.iterator();
               while (listIt.hasNext())
               {
                  String entryStr = listIt.next().toString();
                  url += sepStr + e.getKey().toString() + "=" + 
                     PSURLEncoder.encodePath(entryStr);
                  if (sepStr.equals("?"))
                     sepStr = "&";
               }
            } 
            else
            {
               url += sepStr + e.getKey().toString() + "=" + 
                  PSURLEncoder.encodePath(e.getValue().toString());
            }
            
            if (sepStr.equals("?"))
               sepStr = "&";
         }
      }

      try
      {
         request.getResponse().sendRedirect(url, request);
      } 
      catch (IOException e)
      {
         String errText = "Failed to send redirection response, exception text:"
            + e.toString();
            
         Object[] args = { request.getUserSessionId(), errText };

         PSResponseSendError err = new PSResponseSendError(m_appHandler.getId(),
            request.getUserSessionId(), IPSDataErrors.SEND_RESPONSE_EXCEPTION,
            args);
         
         m_appHandler.reportError(request, err);
      }
   }

   protected String                  m_targetDataSetName;
   protected String                  m_requestURL;
   protected boolean                  m_appendURLExtension;
   protected String                  m_requestTypeParam;
   protected String                  m_requestTypeValue;
   protected PSApplicationHandler   m_appHandler;

   /**
    * Should this redirection be done with http redirect codes?
    * <code>true</code> indicates that it should, <code>false</code> indicates
    * to use the app handler directly.  Set at construction time based on the
    * PSRequestLink's setting.
    */
   protected boolean                m_useHttpRedirectResponse;
}

