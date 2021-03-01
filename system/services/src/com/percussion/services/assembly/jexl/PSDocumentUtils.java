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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.security.PSServletRequestWrapper;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSHtmlBodyInputStream;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.timing.PSStopwatchStack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Utilities to make document requests from a velocity macro
 * 
 * @author dougrand
 */
public class PSDocumentUtils extends PSJexlUtilBase
{
   /**
    * Calls the specified URL and returns the result document data or an empty
    * string on error.
    * 
    * @param url The url, must not be <code>null</code> or empty
    * @return the result document data
    * @throws HttpException
    * @throws IOException
    * @throws ServletException 
    */
   @IPSJexlMethod(description = "Calls the specified URL and returns the "
         + "result document data or an empty string on error.", params =
   {@IPSJexlParam(name = "url", description = "The url, must not be null or empty.")})
   public String getDocument(String url) throws HttpException, IOException, ServletException
   {
      return getDocument(url, null, null);
   }

   /**
    * Calls the specified URL and returns the result document data or an empty
    * string on error.
    * 
    * @param url The url, must not be <code>null</code> or empty
    * @param user The username, may be <code>null</code> or empty
    * @param password The password, may be <code>null</code> or empty
    * @return the result document
    * @throws HttpException
    * @throws IOException
    * @throws ServletException 
    */
   @IPSJexlMethod(description = "Calls the specified URL and returns the "
         + "result document data or an empty string on error.", params =
   {
         @IPSJexlParam(name = "url", description = "The url, must not be null or empty."),
         @IPSJexlParam(name = "user", description = "The user name, may be null or empty."),
         @IPSJexlParam(name = "password", description = "The password, may be null or empty.")})
   public String getDocument(String url, String user, String password)
         throws HttpException, IOException, ServletException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#getDocument");
      try
      {
         if (url.startsWith("../"))
         {
            // Rewrite as absolute to the server
            url = PSServer.getRequestRoot() + url.substring(2);
         }
         if (url.startsWith(PSServer.getRequestRoot()))
         {
            return getInternalDocument(url);
         }
         else
         {
            return getExternalDocument(url, user, password);
         }
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Parse the url and create an internal request to call a servlet in the
    * Rhythmyx web application. This should only be called if the url starts
    * with the context for Rhythmyx. The context will be set on the called
    * request, along with the parsed out parameters and such.
    * 
    * @param url the url, never <code>null</code> or empty
    * @return the resulting document, never <code>null</code>
    * @throws IOException
    * @throws ServletException
    */
   private String getInternalDocument(String url) throws ServletException,
         IOException
   {
      try
      {
         PSRequest psreq = PSThreadRequestUtils.changeToInternalRequest(true);
         PSServletRequestWrapper reqwrapper = (PSServletRequestWrapper) 
            psreq.getServletRequest();
         MockHttpServletRequest req = 
            (MockHttpServletRequest) reqwrapper.getRequest();
         if (!PSRequestInfo.isInited())
         {
            PSRequestInfo.initRequestInfo(req);
         }
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, psreq);
         
         req.setMethod("GET");
         String rxroot = PSServer.getRequestRoot();
         req.setContextPath(rxroot);
         // Remove leading context path
         url = url.substring(rxroot.length());
         // Split on the query separator
         int q = url.indexOf("?");
         String query = null;
         if (q > 0)
         {
            query = url.substring(q + 1);
            url = url.substring(0, q);
         }
         req.setServletPath(url); // All that's left is the path
         req.setQueryString(query);
         // Now, parse the query and set the parameters
         String parts[] = query != null ? query.split("&") : new String[0];
         for (String part : parts)
         {
            String s[] = part.split("=");
            if (s.length > 2)
            {
               throw new MalformedURLException("Bad url parameter: " + part);
            }
            if (s.length < 2) continue; // Skip empty parameters
            String name = URLDecoder.decode(s[0], "UTF-8");
            String value = URLDecoder.decode(s[1], "UTF-8");
            req.setParameter(name, value);
         }
         // Invoke and return
         
         MockHttpServletResponse resp = (MockHttpServletResponse) PSServletUtils
               .callServlet(req);
         resp.setCharacterEncoding(PSCharSets.rxStdEnc());
         return resp.getContentAsString();
      }
      finally
      {
         PSThreadRequestUtils.restoreOriginalRequest();
      }
   }

   /**
    * Call an external url for a document using the given user name and 
    * password. 
    * 
    * @param url the url of the request, assumed not <code>null</code>
    * @param user the user name, may be <code>null</code>
    * @param password the password, may be <code>null</code>
    * @return the resulting document from the request
    * @throws UnknownHostException
    * @throws MalformedURLException
    * @throws IOException
    * @throws HttpException
    */
   private String getExternalDocument(String url, String user, String password) throws UnknownHostException, MalformedURLException, IOException, HttpException
   {
      HttpClient client = new HttpClient();
      client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

      HttpMethod method = new GetMethod(url);
      if (user != null && password != null)
      {
         UsernamePasswordCredentials cred = new UsernamePasswordCredentials(
               user, password);
         String header = BasicScheme.authenticate(cred);
         method.addRequestHeader("Authorization", header);
      }
      int stat = client.executeMethod(method);
      if (stat == 200)
      {
         String rval = method.getResponseBodyAsString();
         method.releaseConnection();
         return rval;
      }
      else
      {
         return "";
      }
   }

   /**
    * Extract the body from the byte stream from the result. Make sure to handle
    * the character set specified in the original result. If none specified
    * assumes UTF8.
    * <p>
    * Note that the input document does not need to be xml compliant. The
    * underlying implementation simply looks for start and end body tags,
    * without regard for syntactical correctness.
    * 
    * @param rval the original result data, never <code>null</code>
    * @return the body content or the entire content if there is no body element
    * @throws IOException
    */
   @IPSJexlMethod(description = "Extract the body from the byte stream from "
         + "the result. Make sure to handle the character set specified in "
         + "the original result. If none specified assumes UTF8.", params =
   {@IPSJexlParam(name = "resultData", description = "the original result data, assumed not null")})
   public String extractBody(IPSAssemblyResult rval) throws IOException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#extractBody");
      try
      {
         if (rval == null)
         {
            throw new IllegalArgumentException("rval may not be null");
         }
         StringWriter w = new StringWriter();
         InputStream stream = new ByteArrayInputStream(rval.getResultData());
         PSHtmlBodyInputStream bodyInputStream = new PSHtmlBodyInputStream(
               stream);
         Charset cset = PSStringUtils
               .getCharsetFromMimeType(rval.getMimeType());
         String input = new String(rval.getResultData(), cset.name());
         if (!input.toLowerCase().contains("<body"))
            return input;
         else
         {
            Reader r = new InputStreamReader(bodyInputStream, cset);
            char buf[] = new char[65536];
            while (true)
            {
               int count = r.read(buf);
               if (count <= 0)
                  break;
               w.write(buf, 0, count);
            }
            w.close();
            return w.toString();
         }
      }
      finally
      {
         sws.stop();
      }
   }

   /**
    * Extract the body from the byte stream from the text.
    * 
    * @param input an html document
    * @return the body content or the entire document if there is no body
    *         element
    * @throws IOException
    */
   @IPSJexlMethod(description = "Extract the body from the byte stream from "
         + "the text.", params =
   {@IPSJexlParam(name = "input", description = "an html document")})
   public String extractBody(String input) throws IOException
   {
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start(getClass().getCanonicalName() + "#extractBody");
      try
      {
         if (!input.toLowerCase().contains("<body"))
            return input;
         else
         {
            StringWriter w = new StringWriter();
            InputStream stream = new ByteArrayInputStream(
                  input.getBytes("UTF8"));
            PSHtmlBodyInputStream bodyInputStream = new PSHtmlBodyInputStream(
                  stream);
            Reader r = new InputStreamReader(bodyInputStream, "UTF8");
            char buf[] = new char[65536];
            while (true)
            {
               int count = r.read(buf);
               if (count <= 0)
                  break;
               w.write(buf, 0, count);
            }
            w.close();
            return w.toString();
         }
      }
      finally
      {
         sws.stop();
      }
   }
}
