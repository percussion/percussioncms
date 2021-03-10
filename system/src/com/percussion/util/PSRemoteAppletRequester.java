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
package com.percussion.util;

import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class is similar with <code>PSRemoteRequester</code>, except it is used
 * by an applet. It does not need credential info from the caller. The 
 * credentail info already handled by the applet/browser framework.
 * See <code>PSRemoteRequester</code> and <code>IPSRemoteRequester</code> for 
 * detail
 */
public class PSRemoteAppletRequester implements IPSRemoteRequester
{
   private static final Logger log = LogManager.getLogger(PSRemoteAppletRequester.class);
   
   
   private PSHttpConnection m_conn = null;
   /**
    * Constructs an instance from an URL object. 
    * 
    * @param url The object, which may be created by 
    *    <code>Applet.getRhythmyxCodeBase()</code>. It may not be <code>null</code>.
    */
   public PSRemoteAppletRequester(PSHttpConnection conn, URL url)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      if (url == null)
         throw new IllegalArgumentException("url may not be null");
      m_conn = conn;
      m_url = url;
   }

   /**
    * See {@link IPSRemoteRequester#getDocument(String, Map)} for detail
    */
   public Document getDocument(String resource, Map params)
      throws IOException, SAXException
   {
      if (resource == null || resource.trim().length() == 0)
         throw new IllegalArgumentException("resource may not be null or empty");
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      return postData(resource, params);
   }

   /**
    * See {@link IPSRemoteRequester#sendUpdate(String, Map)} for detail
    */
   public Document sendUpdate(String resource, Map params)
      throws IOException, SAXException
   {
      if (resource == null || resource.trim().length() == 0)
         throw new IllegalArgumentException("resource may not be null or empty");
      if (params == null)
         throw new IllegalArgumentException("params may not be null");

      return postData(resource, params);
   }


   /**
    * See {@link IPSRemoteRequester#sendUpdate(String, doc)} for detail
    */
   public Document sendUpdate(String resource, Document doc)
      throws IOException, SAXException
   {
      if (resource == null || resource.trim().length() == 0)
         throw new IllegalArgumentException("resource may not be null or empty");
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      return postData(resource, doc);
   }

   /**
    * Just like {@link #getDocument(String, Map)}, except it assumes all 
    * parameters are not <code>null</code> or empty for strings.
    * <p>
    * The connections that are opened by this method will be closed. 
    * @see {@link PSHttpConnection#postData(URL, Map)}
    */
   private Document postData(String resource, Map paramsMap)
      throws IOException, SAXException
   {
      String resp;
      Document doc = null;
      if (resource.startsWith("/")) {
         resource = resource.substring(1);
      }
      URL url = new URL(m_url, "../" + resource);
      
      log.debug("posting to url {} ", url.toString());
      log.debug("Params = {} ", paramsMap);
   
      try
      {
         resp = m_conn.postData(url, paramsMap);
         doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(resp),
            false);
      }
      catch (PSException e)
      {
         
         // throw new PSCmsException(IPSCmsErrors.ERROR_SEND_DATA, e.toString());
            log.error("RemoteAppletRequester error {}",e.getMessage());
            log.debug(e.getMessage(),e);
            throw new IOException(e.toString());
         
      }
      
      return doc;
   }

 
   
   
   
   /**
    * Just like {@link #sendUpdate(String, doc)}, except it assumes all 
    * parameters are not <code>null</code> or empty for strings.
    * <p>
    * The connections that are opened by this method will be closed.
    * @see {@link PSHttpConnection#postData(URL, Document)}
    */
   private Document postData(String resource, Document docData)
      throws IOException, SAXException
   {
      URL url = new URL(m_url, "../" + resource);
      try
      {
         return m_conn.postData(url, docData);
      }
      catch (PSException e)
      {
         log.error("RemoteAppletRequester error {} ",e.getMessage());
         log.debug(e.getMessage(),e);
         //throw new PSCmsException(IPSCmsErrors.ERROR_SEND_DATA, e.toString());
         throw new IOException(e.toString());
      }
   }


   /**
    * See {@link IPSRemoteRequester#shutdown()} for detail
    */
   public void shutdown()
   {
      // this is no need for shutdown connections, because it has already 
      // been closed after send and receive data in the other methods
   }

   /**
    * The base URL, it may be from <code>Applet.getRhythmyxCodeBase()</code>.
    * Initialized by constructor, never <code>null</code> after that.
    */
   private URL m_url;


}
