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

package com.percussion.server.compare;


import com.percussion.HTTPClient.HTTPConnection;
import com.percussion.HTTPClient.HTTPResponse;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * This class is mainly used for preparing the documents for comparision.
 * The documents can be created by calling the constructor with request object
 * and itemnumber. If supplied itemnumber is neither 1 nor 2 then throws an
 * illegal argument exception. It sets the member variables like m_contentID,
 * m_revision etc. by getting the values from input html parameters by adding
 * itemnumber. It has the following utility methods.
 * getAssemblyURL method makes an internal request to AssemblyURL
 * resource to get the assembly url for the given variantid and then creates the
 * URL by adding other paramaters.
 * getAssemblyPage method makes an HTTP connection with the assembly url and
 * returns the assembly page
 */

public class PSCompare
{
   /**
    * Creates PSCompare object and initializes all member variables from
    * the request. The request url will have parameters like contentid and
    * revision for the two documents which need to be compared.
    * we need contentid, revision, variantid, authtype and context. Authtype
    * and context will be considered as 0 if they are missing. If any one of the
    * contentid, revision and variantid missing then <code>PSCompareException
    * </code> will be thrown with appropriate error code.
    *
    * @param request contains the html parameters SYS_CONTENTID,
    *    SYS_REVISION etc. that will be used initialize the members, must not be
    *    <code>null</code>.
    * @param itemNumber of the document allowed values are 1 and 2
    * @throws PSCompareException when any one of the contentid, variantid and
    *    revision is missing
    */
   public PSCompare(PSRequest request, String itemNumber)
      throws PSCompareException
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");     


      String lang = (String)request.getUserSession().getSessionObject(
         PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      m_contentID = request.getParameter(IPSHtmlParameters.SYS_CONTENTID +
         itemNumber);
      
      if(m_contentID == null || m_contentID.length()==0)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_CONTENTID_REQUIRED, itemNumber);
      }
      m_revision = request.getParameter(IPSHtmlParameters.SYS_REVISION +
         itemNumber);
      if(m_revision == null || m_revision.length()==0)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_VARIANTID_REQUIRED, itemNumber);
      }
      m_variantID = request.getParameter(IPSHtmlParameters.SYS_VARIANTID +
         itemNumber);
      if(m_variantID == null || m_variantID.length()==0)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_REVISION_REQUIRED, itemNumber);
      }

      m_context = request.getParameter(IPSHtmlParameters.SYS_CONTEXT +
         itemNumber, DEFAULT_CONTEXT);
      m_authType = request.getParameter(IPSHtmlParameters.SYS_AUTHTYPE +
         itemNumber, DEFAULT_AUTH_TYPE);
   }


   /**
    * This method makes a call to getAssemblyURL method by passing the request
    * object and gets the assembly page url. Then makes a http connection by
    * passing the assembly url and gets the response text for the given
    * assembly url and returns the result as a <code>String</code>
    *
    * @param request the request that needs to be passed to getAssemblyURL
    *    method to get the assembly url, must not be <code>null</code>.
    *
    * @return returns the assembly page html as string, if fails to get assembly
    *    page then <code>empty</code> string will be returned.
    * @throws PSCompareException if getAssemblyURL or HTTPConnection or
    *    HTTPResponse throws any exception
    */
   public String getAssemblyPage(PSRequest request)
   throws PSCompareException
   {
      String assemblyPage = "";
      String lang = (String)request.getUserSession().getSessionObject(
         PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      URL assemblyUrl = getAssemblyURL(request);
      HTTPConnection conn = null;
      try
      {
         conn = new HTTPConnection(assemblyUrl);
         conn.setAllowUserInteraction(false);
         HTTPResponse resp = conn.Get(assemblyUrl.getFile());
         assemblyPage = resp.getText();
      }
      catch(Exception e)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_HTTP_CONNECTION_ERROR,e.getMessage());
      }

      return assemblyPage;
   }
  /**
    * This method makes an internal request to find the assembly url of the
    * supplied parameters.
    *
    * @param request the request to process, must not be <code>null</code>.
    *
    * @return returns the assembly page url as <code>URL</code>
    *
    * @throws PSCompareException if it is not possible to build assembly url
    *    for the given parameters with appropriate error code.
    */
   private URL getAssemblyURL(PSRequest request) throws
      PSCompareException
   {
      URL url = null;
      String lang = (String)request.getUserSession().getSessionObject(
         PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      Map params = new HashMap(1);
      params.put( IPSHtmlParameters.SYS_VARIANTID, m_variantID );
      PSInternalRequest iReq = PSServer.getInternalRequest(
        ASSEMBLY_URL, request, params, false );
      if(iReq==null)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_IREQ_CANNOTBE_NULL,ASSEMBLY_URL);
      }
         // make the internal request and extract the URL from the result XML
      Document result = null;
      try
      {
         result = iReq.getResultDoc();
      }
      catch(PSInternalRequestCallException e)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_IREQ_CALL_EXCEPTION);
      }

      String assemblyURL = result.getDocumentElement().getAttribute(
         ATTRIB_ASSEMBLY_URL);
      if(assemblyURL.length()==0)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_ASSEMBLY_URL_EMPTY, m_variantID);
      }
      Map paramMap = new HashMap(6);
      paramMap.put(IPSHtmlParameters.SYS_SESSIONID,
         request.getUserSessionId());
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, m_contentID);
      paramMap.put(IPSHtmlParameters.SYS_VARIANTID, m_variantID);
      paramMap.put(IPSHtmlParameters.SYS_REVISION, m_revision);
      paramMap.put(IPSHtmlParameters.SYS_CONTEXT, m_context);
      paramMap.put(IPSHtmlParameters.SYS_AUTHTYPE, m_authType);
      String siteid = request.getParameter(IPSHtmlParameters.SYS_SITEID,"").trim();
      if (siteid.length()>0)
         paramMap.put(IPSHtmlParameters.SYS_SITEID, siteid);
      
      try
      {
         url = PSUrlUtils.createUrl(null,null, assemblyURL,
            paramMap.entrySet().iterator(), null,
            new PSRequestContext(request));
         url = PSServer.getResolvedURL(url);
      }
      catch(MalformedURLException e)
      {
         throw new PSCompareException(lang,
            IPSServerErrors.COMPARE_MALLFORMED_URL_ERROR);
      }
      catch (IOException e)
      {
         //PSServer.getResolvedURL throws an IOException if there is a problem
         //with the URL, converting it to MalformedURLException and throwing it.
         throw new PSCompareException(lang,
               IPSServerErrors.COMPARE_MALLFORMED_URL_ERROR);
      }
      return url;
   }

  /**
    * Gets the contentid.
    *
    * @return contentid, never <code>null</code>.
    */
   public String getContentID()
   {
      return m_contentID;
   }
   /**
    * Gets the revision.
    *
    * @return revision, never <code>null</code>.
    */
   public String getRevision()
   {
      return m_revision;
   }

   /**
    * Gets the variantid.
    *
    * @return variantid, never <code>null</code>.
    */
   public String getVariantID()
   {
      return m_variantID;
   }

   /**
    * Gets the context.
    *
    * @return context, never <code>null</code>.
    */
   public String getContext()
   {
      return m_context;
   }

   /**
    * Gets the authtype.
    *
    * @return authtype, never <code>null</code>.
    */
   public String getAuthType()
   {
      return m_authType;
   }


   /**
    * Variable to hold the contentid.
    */
   private String m_contentID;
   /**
    * Variable to hold the revision.
    */
   private String m_revision;
   /**
    * Variable to hold the variantid.
    */
   private String m_variantID;
   /**
    * Variable to hold the context.
    */
   private String m_context = DEFAULT_CONTEXT;
   /**
    * Variable to hold the authtype.
    */
   private String m_authType = DEFAULT_AUTH_TYPE;

   /**
    * Default value for the authType parameter if it is not avaialable in
    * request url
    */
   public static final String DEFAULT_AUTH_TYPE = "0";
   /**
    * Default value for the context parameter if it is not avaialable in
    * request url
    */
   public static final String DEFAULT_CONTEXT = "0";

   /**
    * Assembly URL application and resources name
    */
   public static final String ASSEMBLY_URL =
      "sys_casSupport/AssemblyUrl";
   /**
    * Assembly URL attribute name
    */
   public static final String ATTRIB_ASSEMBLY_URL = "current";

}
