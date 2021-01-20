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
package com.percussion.test.http;

import com.percussion.HTTPClient.ModuleException;
import com.percussion.HTTPClient.NVPair;
import com.percussion.test.io.IOTools;
import com.percussion.test.io.LogSink;
import com.percussion.util.PSBase64Decoder;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSRemoteRequester;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Encapsulates an HTTP GET or POST request and the results of the request.
 * This class will not follow the redirect URL, which is acomplished by
 * {@link #getFollowRedirects()}.
 * <p>
 * This class is a replacement for {@link HttpRequest}. It implements all 
 * methods from {@link HttpRequest}, but is implemented with 
 * {@link PSRemoteRequester}.
 */
public class PSHttpRequest extends PSRemoteRequester
{

   /**
    * Construct a request to the given URL with the given method. If request
    * content is non-null, then the request content will be after the request
    * and the request headers.
    * <p>
    * If you happen to know the length (in bytes) of the content, you may want
    * to set the "Content-length" request header to that value. We will not do
    * it automatically, and we will <B>not</B> consider the setting of this
    * header when reading the content stream.
    * <p>
    * Also, if you happen to know the character encoding that the content uses,
    * you may want to set the "Char-encoding" request header. Again, we will
    * not consider this value when sending the content.
    *
    * @param   URL
    * @param   reqMethod
    * @param   content May be null if no POST data is needed.
    *
    */
   public PSHttpRequest(String URL, String reqMethod, InputStream reqContent)
         throws MalformedURLException
   {
      init(URL, reqMethod, reqContent);
   }

   /**
    * The resource of Rhythmyx, which may contains the query string
    */
   private String m_resource;
   private String m_reqMethod;
   private InputStream m_reqContent;
   
   private void init(String httpUrl, String reqMethod, InputStream reqContent)
         throws MalformedURLException
   {
      String RX_ROOT = "Rhythmyx";
      m_reqURL = new URL(httpUrl);
      m_reqMethod = reqMethod;
      m_reqContent = reqContent;
      
      Properties props = new Properties();
      props.put("hostName", m_reqURL.getHost());
      props.put("port", String.valueOf(m_reqURL.getPort()));
      if (m_reqURL.getProtocol().equalsIgnoreCase("https"))
         props.put("useSSL", "true");
      else
         props.put("useSSL", "false");
      
      int index = httpUrl.indexOf(RX_ROOT);
      m_resource = httpUrl.substring(index + RX_ROOT.length() + 1);
      
      super.init(props);
   }

   /**
    * Sets the outgoing request content for this request. If an existing
    * content had been specified (and it is not the same content stream
    * as the argument to this method), the existing content will be
    * closed first.
    *
    * @param   content
    *
    */
   public void setRequestContent(InputStream content)
   {
      if (m_reqContent != null && content != m_reqContent)
      {
         try
         {
            m_reqContent.close();
         }
         catch (IOException e)
         {
            /* ignore */
         }
      }
      m_reqContent = content;
   }

   /**
    * Gets the request method, usually "GET" or "POST".
    *
    * @return  String
    */
   public String getRequestMethod()
   {
      return m_reqMethod;
   }

   /**
    * Enables tracing status to the given PrintWriter.
    *
    * @param   logger
    *
    */
   public void enableTrace(LogSink logger)
   {
      m_logger = logger;
   }

   public void addRequestHeaders(HttpHeaders headers)
   {
      m_reqHeaders.addAll(headers);
   }

   /**
    * Adds a header that will be sent along with the request.
    *
    * @param   headerName
    * @param   headerValue
    *
    */
   public void addRequestHeader(String headerName, String headerValue)
   {
      m_reqHeaders.addHeader(headerName, headerValue);
   }

   /**
    * Returns the response headers.
    *
    * @return  Iterator
    * @throws ModuleException 
    * @throws IOException 
    */
   public HttpHeaders getResponseHeaders() throws IOException, ModuleException
   {
      //return respHeaders;
      HttpHeaders headers = new HttpHeaders();
      String value, name;
      Enumeration names = getResponse().listHeaders();
      while (names.hasMoreElements())
      {
         name = (String) names.nextElement();
         value = getResponse().getHeader(name);
         headers.addHeader(name, value);
      }
      
      return headers;
   }

   /**
    * Sends the request and parses the response. If request content was
    * supplied in the constructor, it will be sent.
    * <P>
    * The request content stream (if specified in the constructor) is
    * guaranteed to be closed after this method is called, even if
    * exceptions are thrown from this method.
    *
    * @throws  IOException
    * @throws HttpConnectException if all atempts failed to establish an HTTP
    *    connection.
    */
   public void sendRequest() throws IOException, ModuleException
   {
      if (m_reqMethod.equalsIgnoreCase("GET"))
      {
         getBinary(m_resource);
      }
      else if (m_reqMethod.equalsIgnoreCase("POST"))
      {
         byte[] data = getContentBytes();
         NVPair[] headers = null;
         if (data.length > 0)
         {
            headers = new NVPair[]
            {new NVPair("Content-Type", "text/xml; charset="
                  + PSCharSets.rxStdEnc())};
         }

         postRequest(m_resource, null, data, headers);
      }
      else
      {
         throw new RuntimeException("Unknown method: " + m_reqMethod);
      }
   }
   
   /**
    * This is called by super class. It combines the default headers from the
    * super class and the headers of this objects.
    * 
    * @return the combined headers described above, never <code>null</code>.
    */
   protected NVPair[] getDefaultHeaders()
   {
      NVPair[] superHeaders = super.getDefaultHeaders();
      NVPair[] httpHeaders = getHeaders();
      
      if (httpHeaders.length == 0)
      {
         return superHeaders;
      }
      else
      {
         // merge both headers
         NVPair[] defHeaders = new NVPair[superHeaders.length + httpHeaders.length];
         for (int i=0; i<superHeaders.length; i++) 
            defHeaders[i] = superHeaders[i];
         for (int i=0; i<httpHeaders.length; i++) 
            defHeaders[i + superHeaders.length] = httpHeaders[i];
            
         return defHeaders;
      }
   }

   /**
    * This is called by super class. We don't want the request to follow the
    * redirect URL since we need to get the info from the redirect URL.
    * 
    * @return <code>false</code> see description above.
    */
   protected boolean getFollowRedirects()
   {
      return false;
   }

   /**
    * @return <code>false</code>, let this object to process the status code
    *   of the response. 
    */
   protected boolean getProcessStatusCode()
   {
      return false;
   }
      
   /**
    * Get the normalized headers and set credential if needed.
    * 
    * @param con the connection used to set credential, assumed not 
    *    <code>null</code>.
    * 
    * @return the headers, never <code>null</code>, but may be empty.
    */
   private NVPair[] getHeaders()
   {
      List<NVPair> headers = new ArrayList<NVPair>();
      Iterator names = m_reqHeaders.getHeaderNames().iterator();
      String name, value;
      while (names.hasNext())
      {
         name = (String)names.next();
         value = m_reqHeaders.getHeader(name);
         if (name.equalsIgnoreCase("Authorization"))
         {
            String[] sa = value.split(" ");
            if (sa.length != 2)
               throw new RuntimeException("Unknown Authorization header value: " + value);
            String code = PSBase64Decoder.decode(sa[1]);
            sa = code.split(":");
            if (sa.length != 2)
               throw new RuntimeException("Unknown encode (from Authorization header value): " + code);
    
            setCredentials(sa[0], sa[1]);
         }
         else
         {
            headers.add(new NVPair(name, value));
         }
      }
      NVPair[] httpHeader = new NVPair[headers.size()];
      if (httpHeader.length > 0)
         headers.toArray(httpHeader);

      return httpHeader;
   }
   
   /**
    * @return the request content in byte array, never <code>null</code>, may
    *     be empty.
    *     
    * @throws IOException
    */
   private byte[] getContentBytes() throws IOException
   {
      if (m_reqContent == null)
         return new byte[0];
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream( 5000 );
      long totalBytes = IOTools.copyStream( m_reqContent, bos );
      m_reqContent.close();
      m_reqContent = null;

      return bos.toByteArray();
   }

   /**
    * Gets the response content stream, which may be null
    * or empty if getResponseCode() returns anything other
    * than 2xx. If we are currently waiting for data to
    * become available over the connection, this method
    * will block until either we have timed out or until
    * data becomes available.
    *
    * @return  InputStream
    * @throws ModuleException 
    * @throws IOException 
    */
   public InputStream getResponseContent() throws IOException, ModuleException
   {
      return getResponse().getInputStream();
   }

   /**
    * Gets the HTTP response code.
    *
    * @return  int
    * @throws ModuleException 
    * @throws IOException 
    */
   public int getResponseCode() throws IOException, ModuleException
   {
      return getResponse().getStatusCode();
   }

   /**
    * Closes the request. Any pending results are discarded,
    * and the response content is no longer valid.
    *
    * @throws  Exception;
    *
    */
   public void disconnect() throws IOException
   {
   }

   /**
    * The number of bytes sent in a request. Calculated during the processing
    * of <code>sendRequest</code>. 0 until this method called at least once.
    * Use {@link #getBytesSent} to retrieve.
    */
   protected int m_bytesSent = 0;
   protected LogSink m_logger;

   /* response */

   /* request */
   protected HttpHeaders m_reqHeaders = new HttpHeaders();
   protected URL m_reqURL;

}
