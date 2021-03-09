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

package com.percussion.util.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.percussion.HTTPClient.PSBinaryFileData;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.IPSRemoteRequesterEx;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * This class handles the communication between a servlet and the
 * Rhythmyx Server, similar with <code>PSRemoteRequester</code>, except
 * the remote client is a servlet.
 */
public class PSServletRequester implements IPSRemoteRequesterEx
{
   /**
    * Creates an instance from the servlet parameters.
    *
    * @param request The request object, it may not be <code>null</code>.
    *
    * @param response The response object, it may not be <code>null</code>.
    *
    * @param rxContext The servlet context of the RhythmyxServlet. It is
    *    from <code>getServletContext.getContext("/Rhythmyx"), assume the
    *    "/Rhythmyx" is the context path of the RhythmyxServlet. it may not
    *    be <code>null</code>.
    *
    * @param uriPrefix The URI prefix for the resource that is the parameter
    *    for {@link #getDocument(String, Map)}, {@link #sendUpdate(String,Map)}
    *    or {@link #sendUpdate(String, Document)}. It may be <code>null</code>
    *    or empty. The URI prefix should be <code>null</code> or empty when the
    *    Rhythmyx servlet is in another web app with the url-pattern as '/*'.
    *    It may be '/Rhythmyx' when the Rhythmyx Servlet url-pattern is
    *    '/Rhythmyx/*' and it exits in a web app, such as 'rxwebdav'.
    */
   public PSServletRequester(
      HttpServletRequest request,
      HttpServletResponse response,
      ServletContext rxContext,
      String uriPrefix)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (response == null)
         throw new IllegalArgumentException("response may not be null");
      if (rxContext == null)
         throw new IllegalArgumentException("rxContext may not be null");

     m_request = request;
     m_response = response;
     m_rxContext = rxContext;
     m_uriPrefix = (uriPrefix == null) ? "" : uriPrefix;
   }

   /**
    * Convenient constructor, calls
    * {@link #PSServletRequester(HttpServletRequest,HttpServletResponse,
    * ServletContext, null)}
    */
   public PSServletRequester(
      HttpServletRequest request,
      HttpServletResponse response,
      ServletContext rxContext)
   {
      this(request, response, rxContext, null);
   }

   // See {@link IPSRemoteRequester#getDocument(String, Map)} for detail
   public Document getDocument(String resource, Map params)
      throws IOException, SAXException
   {
      PSInternalRequestMultiPart irq = new PSInternalRequestMultiPart(m_request);

      // Add community override parameter if needed
      params = addCommunityOverride(params);

      if (params != null && (! params.isEmpty()))
      {
         Iterator paramList = params.entrySet().iterator();

         while (paramList.hasNext())
         {
            Map.Entry entry = (Map.Entry)paramList.next();
            if (entry.getKey() == null)
               throw new IllegalArgumentException(
                     "params may not contain a null key");

            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            irq.setParameter(key, value);
         }
      }
      irq.emptyBody(); // we already have everything specified in the parameters

      try
      {
         PSInternalResponseXML iresp = new PSInternalResponseXML(m_response);
         sendRequest(resource, irq, iresp);
         return iresp.getDocument();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   // See {@link IPSRemoteRequester#sendUpdate(String, Map)} for detail
   public Document sendUpdate(String resource, Map params)
      throws IOException, SAXException
   {
      return getDocument(resource, params);
   }

   /**
    * This is not supported for now.
    *
    * @see IPSRemoteRequester#sendUpdate(String, Document)}
    */
   public Document sendUpdate(String resource, Document doc)
      throws IOException, SAXException
   {
      throw new UnsupportedOperationException("sendUpdate");
   }

   /*
    * @see com.percussion.util.IPSRemoteRequesterEx#getBinary(java.lang.String, java.util.Map)
    */
   public byte[] getBinary(String resource, Map params) throws IOException
   {
      PSInternalRequestMultiPart irq = new PSInternalRequestMultiPart(m_request);

      // Add community override parameter if needed
      params = addCommunityOverride(params);

      if (params != null && (! params.isEmpty()))
      {
         Iterator paramList = params.entrySet().iterator();

         while (paramList.hasNext())
         {
            Map.Entry entry = (Map.Entry)paramList.next();
            if (entry.getKey() == null)
               throw new IllegalArgumentException(
                     "params may not contain a null key");

            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            irq.setParameter(key, value);
         }
      }

      irq.prepareBody(); // prepare body and set header accordingly

      try
      {
         PSInternalResponse irsp = new PSInternalResponse(m_response);
         sendRequest(resource, irq, irsp);
         try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try(InputStream is = irsp.getInputStream()) {
               PSHttpUtils.copyStream(is, bos);
               return bos.toByteArray();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * Send the supplied request to the target resource.
    *
    * @param resource The target resource to send the request, assume not
    *    <code>null</code> or empty.
    * @param ireq The request, assume not <code>null</code>.
    * @param irsp The response to return to caller, assume not
    *    <code>null</code>.
    *
    * @throws ServletException if servlet error occurs
    * @throws IOException if IO error occurs
    */
   private void sendRequest(String resource,
         PSInternalRequest ireq, PSInternalResponse irsp)
         throws ServletException, IOException
   {
      String internalURI = getURIForRxServlet(resource);
      RequestDispatcher rd = m_rxContext.getRequestDispatcher(internalURI);
      rd.include(ireq, irsp);
   }

   // Implements IPSRemoteRequesterEx#sendBinary()
   public PSLocator updateBinary(
      PSBinaryFileData[] files,
      String resource,
      Map params)
      throws IOException
   {
      PSInternalRequestMultiPart irq = new PSInternalRequestMultiPart(m_request);

      // Add community override parameter if needed
      params = addCommunityOverride(params);

      if (params != null && (! params.isEmpty()))
      {
         Iterator paramList = params.entrySet().iterator();

         while (paramList.hasNext())
         {
            Map.Entry entry = (Map.Entry)paramList.next();
            if (entry.getKey() == null)
               throw new IllegalArgumentException(
                     "params may not contain a null key");

            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            irq.setParameter(key, value);
         }
      }

      //Add binaries
      if(files != null)
      {
         for(int i = 0; i < files.length; i++)
         {
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
               bos.write(files[i].getData());
               // Determine the content type
               String contentType = files[i].getContentType() == null ?
                       CT.guessContentType(files[i].getFileName()) :
                       files[i].getContentType();
               // Create the body part
               PSHttpBodyPart part =
                       new PSHttpBodyPart(
                               files[i].getFieldName(),
                               files[i].getFileName(),
                               contentType,
                               PSCharSets.rxJavaEnc(),
                               bos);
               // add the body part
               irq.addBodyPart(part);
            }
         }
      }
      irq.prepareBody(); // prepare body and set header accordingly

      try
      {
         PSInternalResponse irsp = new PSInternalResponse(m_response);
         sendRequest(resource, irq, irsp);
         String location  = irsp.getHeader("Location");
         if(location == null)
         {
            throw new IOException(
                  "Unexpected ERROR: no Location header in response header, "
                        + "server error message is:\n" + irsp.getString());
         }
         else
         {
            return parseLocatorFromLocation(location);
         }
      }
      catch (ServletException e)
      {
         String errorMsg = e.getLocalizedMessage();
         if (errorMsg == null || errorMsg.trim().length() == 0)
            errorMsg = e.toString();
         throw new IOException(errorMsg);
      }
   }


   // See {@link IPSRemoteRequester#shutdown()} for detail
   public void shutdown()
   {
      // this is no need for shutdown operation
   }

   /**
    * Parses the contentid and revision from a location header
    * string and creates and returns a <code>PSLocator</code>
    *
    * @param loc the location string, cannot be <code>null</code>.
    *
    * @return the locator from the supplied location, never <code>null</code>.
    *
    * @throws IOException if the contentid or revision could not be parsed.
    */
   private PSLocator parseLocatorFromLocation(String loc) throws IOException
   {
      if(loc == null)
         throw new IllegalArgumentException("Location string cannot be null.");

      int sPos = -1;
      int ePos = -1;
      String contentid = "";
      String revision = "";
      if((sPos = loc.indexOf("sys_contentid=")) != -1)
      {
         ePos = loc.indexOf("&", sPos) == -1 ?
            loc.length() :
            loc.indexOf("&", sPos);
         contentid = loc.substring(sPos + 14, ePos);

      }
      if((sPos = loc.indexOf("sys_revision=")) != -1)
      {
         ePos = loc.indexOf("&", sPos) == -1 ?
            loc.length() :
            loc.indexOf("&", sPos);
         revision = loc.substring(sPos + 13, ePos);
      }
      if(contentid.length() != 0 && revision.length() != 0)
         return new PSLocator(contentid, revision);

      // Cannot find content ID and revision,
      // previous request failed, try to get error message from "loc"
      String errorMsg = getErrorMsg(loc);

      throw new IOException(errorMsg);
   }

   /**
    * Retrieves error message from the supplied URL, which is the value
    * of "Location" header of a response.
    *
    * @param errorUrl the value of "Location" header, assume not
    *    <code>null</code> or empty.
    *
    * @return the error message, never <code>null</code>, may be empty
    */
   private String getErrorMsg(String errorUrl)
   {
      String errorMsg = "";
      try
      {
         URL url = new URL(errorUrl);
         String resource = url.getFile();
         // skip the leading "/Rhythmyx"
         if (resource.charAt(0) == '/')
         {
            String newResource = resource.substring(1);
            int index = newResource.indexOf('/');
            if (index != -1)
               resource = newResource.substring(index);
         }

         PSInternalRequest req = new PSInternalRequest(m_request);
         req.setMethod("GET");

         PSInternalResponse resp = new PSInternalResponse(m_response);
         sendRequest(resource, req, resp);

         errorMsg = resp.getString();

         // try to extract the "DisplayError" element from the errorMsg
         try
         {
            byte[] bytes = errorMsg.getBytes();
            try(ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
               Document doc = PSXmlDocumentBuilder.createXmlDocument(input, false);
               Element root = doc.getDocumentElement();
               Element errorElem = PSXMLDomUtil.getFirstElementChild(root,
                       "DisplayError");
               errorMsg = PSXmlDocumentBuilder.toString(errorElem);
            }
         }
         catch (Exception e)
         {
            // ignore error if fail to extract "DisplayError" element
         }
      }
      catch (Exception e)
      {
         // ignore error if fail to get error message
      }

      return errorMsg;
   }

   /**
    * Get the resource URI from Rhythmyx servlet. For example, if the resource
    * is "../casFoobar/foobar.xml", then the URI from Rhythmyx servlet is
    * "$RX-PREFIX/casFoobar/foobar.xml", where "$RX-PREFIX" may be empty or
    * "/Rhythmyx", which is from <code>m_uriPrefix</code>.
    *
    * @param resource The resource URI, assume not <code>null</code> or empty.
    *
    * @return The URI from Rhythymx servlet, never <code>null</code>.
    */
   private String getURIForRxServlet(String resource)
   {
      if (! resource.startsWith("/"))
      {
         if (resource.startsWith("../"))
         {
            int index = resource.indexOf("/");
            resource = resource.substring(index);
         }
         else
         {
            resource = "/" + resource;
         }
      }

      return m_uriPrefix + resource;
   }

   /**
    * Adds the community override param to a map of
    * parameters if m_overrideCommunityid is not <code>null</code>
    * @param params
    * @return modified map, or new map if params is <code>null</code>
    * and the override is needed.
    */
   private Map addCommunityOverride(Map params)
   {
      if(params == null)
         params = new HashMap();
      if(m_overrideCommunityid != null)
      {
         if(params == null)
            params = new HashMap();
         params.put(
            IPSHtmlParameters.SYS_OVERRIDE_COMMUNITYID,
            m_overrideCommunityid);
      }
      return params;
   }

   /**
    * Sets the community that this remote agent will always
    * override to when making a request. No override will
    * occur if set to <code>null</code>.
    * @param id the community id that will be used, may be <code>null</code>
    * , but if not then it must be an integer string.
    */
   public void setOverrideCommunityId(String id)
   {
      if(id == null)
      {
         m_overrideCommunityid = null;
         return;
      }

      try
      {
         Integer.parseInt(id);
         m_overrideCommunityid = id;
      }
      catch(NumberFormatException nfe)
      {
         throw new IllegalArgumentException(
           "Override community id must be a string of " +
           "numeric characters.");
      }

   }

   /**
    * We extend the URLConnection class so that we can use
    * the guessContentTypeFromName method which is protected.
    */
   private static class CT extends URLConnection
   {
     private CT()
     {
        super(null);
     }
     public void connect(){}
     private static String guessContentType(String filename)
     {
        String type = guessContentTypeFromName(filename);
        return type == null ? "application/octet-stream" : type;
     }

   }

   /**
    * If the value of the field is not <code>null</code> then
    * the community will be overriden to this community in the
    * request methods.
    * May be <code>null</code>.
    */
   private String m_overrideCommunityid = null;

   /**
    * Initialized by ctor, never <code>null</code> after that.
    * see ctor for its description.
    */
   private HttpServletRequest m_request;

   /**
    * Initialized by ctor, never <code>null</code> after that.
    * see ctor for its description.
    */
   private HttpServletResponse m_response;

   /**
    * Initialized by ctor, never <code>null</code> after that.
    * see ctor for its description.
    */
   private ServletContext m_rxContext;

   /**
    * The URI prefix, see ctor for more info. Init by ctor, never
    * <code>null</code> after that, but may be empty.
    */
   private String m_uriPrefix;
}
