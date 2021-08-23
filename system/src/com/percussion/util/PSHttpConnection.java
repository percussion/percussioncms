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
package com.percussion.util;

import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to handle the communications between an applet client
 * and a server with HTTP protocol.
 */
public class PSHttpConnection
{

   private static final Logger log = LogManager.getLogger(PSHttpConnection.class);

   public PSHttpConnection(URL rxCodeBase, String  psSessionId)
   {
      ms_baseUrl =  rxCodeBase;
      ms_psSessionId = StringUtils.isBlank(psSessionId) ? null : psSessionId;
   }
   
   
   public JSONObject getJSON(URL resource) throws IOException, PSException, JSONException
   {
      if (resource == null)
         throw new IllegalArgumentException("resource may not be null");

      JSONObject json = null;

      resource = appendPsSessionId(resource);
      String resp = getData(resource, "application/json");
      json = new JSONObject(resp);
      return json;
   }
   
   /**
    * Post a set of data to a server. The connections that are opened by this
    * method will be closed.
    *
    * @param url The destination it is going to send to. It may not
    *    <code>null</code>.
    *
    * @param paramMap The parameter map. It contains a set of data in
    *    <code>String<code> for both key and value. It is assumed the
    *    key is ASCII. The value of the key will be URL encoded before
    *    send to server. It may not be <code>null</code>, but may be empty.
    *
    * @return The response from the server. Never <code>null</code>.
    *
    * @throws PSException if received error code.
    *
    * @throws IOException if an error occurs during send/receive data
    */
   public String postData(URL url, Map paramMap)
      throws IOException, PSException
   {
      if (url == null)
         throw new IllegalArgumentException("url may not be null");
      if (paramMap == null)
         throw new IllegalArgumentException("paramMap may not be null");
      
      /* Using "multipart/form-data" to post the parameters, if it is not empty.
       *
       * "multipart/form-data" is more complicated than
       * "application/x-www-form-urlencoded", but current implementation of
       * URLEncode/URLDecode does not work for I18N, so we are using
       * "multipart/form-data" to post the parameters, which does not involve
       * URLEncode/URLDecode at the client/server side.
       *
       * The format of "multipart/form-data" looks like:
       *
       *  -----------------------------7d310e3120318
       *  sys_All
       *  -----------------------------7d310e3120318
       *  Content-Disposition: form-data; name="sys_workflowid"
       *
       *  1
       *  -----------------------------7d310e3120318
       *  Content-Disposition: form-data; name="sys_command"
       *
       *  modify
       *  -----------------------------7d310e3120318--
       */
      String boundary = getBoundary(paramMap); // get a unique boundary

      String prefix = "Content-Disposition: form-data; name=\"";
      String boundaryUsed = "--" + boundary;
      String endBoundary = boundaryUsed + "--\r\n\r\n";

      StringBuilder data = new StringBuilder(100);
      if (! paramMap.isEmpty())
      {
         Iterator i = paramMap.keySet().iterator();
         while (i.hasNext())
         {
            String key = (String)i.next();
            Object obj = paramMap.get(key);
            if(obj instanceof List) //deal with multivalued param
            {
               List vals = (List)obj;
               for(int ii=0; ii<vals.size(); ii++)
               {
                  data.append(boundaryUsed + "\r\n");
                  data.append(prefix + key + "\"\r\n\r\n");
                  data.append(vals.get(ii).toString() + "\r\n");
               }
            }
            else //assume single valued param
            {
               data.append(boundaryUsed + "\r\n");
               data.append(prefix + key + "\"\r\n\r\n");
               data.append(obj.toString() + "\r\n");
            }
         }
         data.append(endBoundary);

      }

      // send the data to server and get the response
      if (paramMap.isEmpty())
      {
         return postDataWithPSSessionId(url, "", "application/x-www-form-urlencoded");
      }
      else
      {
         return postDataWithPSSessionId(url, data.toString(),
            "multipart/form-data; charset=" + PSCharSets.rxStdEnc() +
            "; boundary=" + boundary);
      }
   }

   /**
    * This submits supplied XML document string as input data document (POST
    * request - multi-part/formdata) to the provided URL. This is equivalent to
    * submitting the XML string as a file attachment.
    *
    * @param url The destination it is going to send to. May not <code>null</code>.
    *
    * @param XmlDocString string (UTF-8) represntation of the XML document to post
    * to the URL specified. Must not be <code>null</code> or empty.
    *
    * @return the string result of the post call, may be empty, or an error page
    * if the post HTTP response code >= 400
    *
    * @throws PSException if received error code.
    *
    * @throws IOException if an error occurs during send/receive data
    */
   public String postXmlData(URL url, String XmlDocString)
      throws IOException, PSException
   {
      if (url == null)
         throw new IllegalArgumentException("url may not be null");
      if (XmlDocString == null || XmlDocString.length() < 1)
         throw new IllegalArgumentException("paramMap may not be null or empty");

      String boundary = "RhythmyxContentManager";

      String prefix = "Content-Disposition: form-data; name=\"inputdoc\"; "
         + "filename=\"inputDoc.xml\"\r\nContent-Type: text/xml";
      String boundaryUsed = "--" + boundary;
      String endBoundary = boundaryUsed + "--\r\n\r\n";

      StringBuilder data = new StringBuilder(100);
      data.append(boundaryUsed + "\r\n");

      data.append(prefix + "\"\r\n\r\n");
      data.append(XmlDocString + "\r\n");
      data.append(endBoundary);

      // send the data to server and get the response
      return postDataWithPSSessionId(url, data.toString(),
         "multipart/form-data; charset=" + PSCharSets.rxStdEnc() +
         "; boundary=" + boundary);
   }

   /**
    * Get a boundary value which is unique string amoung the values of the
    * given parameters.
    *
    * @param paramsMap The to be examed parameters, assume not <code>null</code>.
    *
    * @return The unique boundary string.
    */
   private String getBoundary(Map paramsMap)
   {
      String boundary = ms_boundary;
      boolean done = false;

      while (! done)
      {
         Iterator values = paramsMap.values().iterator();
         while (values.hasNext())
         {
            String value = values.next().toString();
            if ( value.indexOf(boundary) >= 0 ) // if not unique, get new one
            {                                   // and try again
               boundary = getNewBoundary(boundary);
               break;
            }
         }
         done = (! values.hasNext());
      }

      return boundary;
   }

   /**
    * Helper method to get a new boundary from an old one.
    *
    * @param boundary The old boundary value. Assume not <code>null</code> or
    *    emtpry.
    *
    * @return A base 64 encoded string from the old boundary, hopefully this
    *    will be unique among the input data.
    */
   private static String getNewBoundary(String boundary)
   {
      byte[] bytes = boundary.getBytes();

      try(java.io.ByteArrayInputStream in
              = new java.io.ByteArrayInputStream(bytes)){
        try( java.io.ByteArrayOutputStream out
                 = new java.io.ByteArrayOutputStream()) {

           PSBase64Encoder.encode(in, out);

           return new String(out.toByteArray());
        }
      }
      catch (java.io.IOException e)
      {
         throw new RuntimeException(e.toString());
      }

   }


   /**
    * Just like {@link #postData(URL, Map)}, except it sends a
    * <code>Document</code> and returns the response in a document object.
    * The connections that are opened by this method will be closed.
    *
    * @param doc The to be send document, may not be <code>null</code>.
    *
    * @return the responsed document, never <code>null</code>.
    *
    * @SAXException if error occurs while converting the response to a document.
    */
   public Document postData(URL url, Document doc)
      throws IOException, PSException, SAXException
   {
      if (url == null)
         throw new IllegalArgumentException("url may not be null");
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      String postData = PSXmlDocumentBuilder.toString(doc);
      String resp = postDataWithPSSessionId(url, postData, "text/xml; charset=" +
         PSCharSets.rxStdEnc());

      return PSXmlDocumentBuilder.createXmlDocument(new StringReader(resp),
         false);
   }

   /**
    * Just like {@link #postData(URL, String, String)}, except it appends PS
    * Session ID to the specified URL, then calls
    * {@link #postData(URL, String, String)}.
    */
   private String postDataWithPSSessionId(URL url, String data,
         String sendContentType) throws IOException, PSException
   {
      url = appendPsSessionId(url);
      return postData(url, data, sendContentType);
   }
   
   /**
    * Just like {@link #postData(URL, Map)}, except it sends a string data
    * for a given Content-Type. The connections that are opened by this method
    * will be closed.
    *
    * @param data The to be send data, assume not <code>null</code>, but may
    *    be empty.
    *
    * @param sendContentType The Content-Type of the <code>data</code>. It may be
    * <code>null</code> if there is no data.
    */
   private static String postData(URL url, String data, String sendContentType)
      throws IOException, PSException
   {
      HttpURLConnection connection = null;
      String msg = "";

      try
      {
         // open the connection
         connection = (HttpURLConnection)url.openConnection();

         connection.setRequestMethod("POST");

         // Make sure browser doesn't cache this URL.
         connection.setUseCaches(false);

         // Tell browser to allow me to send data to server.
         connection.setDoOutput(true);

         // POST requests are required to have Content-Length
         connection.setRequestProperty("Content-Length", "" + data.length());

         // Netscape sets the Content-Type to multipart/form-data
         // by default. So, if you want to send regular form data,
         // you need to set it to
         // application/x-www-form-urlencoded, which is the
         // default for Internet Explorer. If you send
         // serialized POST data with an ObjectOutputStream,
         // the Content-Type is irrelevant, so you could
         // omit this step.
         if (StringUtils.isNotBlank(sendContentType))
            connection.setRequestProperty("Content-Type", sendContentType);

         // Write POST data to connection output stream
         try(OutputStream out = connection.getOutputStream()) {
            out.write(data.getBytes(PSCharSets.rxJavaEnc()));

            int respCode = connection.getResponseCode();

            String sContentLength = connection.getHeaderField("Content-Length");
            int contentLength = (sContentLength != null) ?
                    Integer.parseInt(sContentLength) : -1;
            long readData;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

               try (InputStream in = connection.getInputStream()) {
                  readData = IOTools.copyStream(in, os);
               } catch (IOException e) {
                  try (InputStream in = connection.getErrorStream()) {
                     readData = IOTools.copyStream(in, os);
                  }
               }


               // Make sure received all the data if specified "Content-Length"
               if ((contentLength > 0) && (long) contentLength != readData) {
                  String[] args = {"" + readData, "" + contentLength};
                  throw new PSException(IPSUtilErrors.RECEIVE_DATA_ERROR, args);
               }

               byte[] byteData = os.toByteArray();
               // Get the character-set for the received data
               String rcvContentType = connection.getHeaderField("Content-Type");
               String charset = PSCharSets.rxJavaEnc();
               if (rcvContentType != null) {
                  Map params = new HashMap();
                  PSBaseHttpUtils.parseContentType(rcvContentType, params);
                  charset = (String) params.get("charset");
                  if (charset == null)
                     charset = PSCharSets.rxJavaEnc();
                  else
                     charset = PSCharSets.getJavaName(charset);
               }

               // Convert byte[] to string
               msg = new String(byteData, charset);

               // if error, print to console,
               // then throw exception with error code and message
               if (respCode >= 400) {
                  String[] args = {Integer.toString(respCode), msg};
                  throw new PSException(IPSUtilErrors.POST_DATA_ERROR, args);
               }
            }
            return msg;
         }
      } catch (IOException |NumberFormatException | PSException e) {
         throw new PSException(IPSUtilErrors.POST_DATA_ERROR, e.getMessage());
      }
   }
   
   /**
    * Just like {@link #postData(URL, Map)}, except it sends a string data
    * for a given Content-Type. The connections that are opened by this method
    * will be closed.
    *
    * @param url The to be send data, assume not <code>null</code>, but may
    *    be empty.
    *
    * @param sendContentType The Content-Type of the <code>data</code>. It may be
    * <code>null</code> if there is no data.
    */
   private static String getData(URL url, String sendContentType)
      throws IOException, PSException
   {
      HttpURLConnection connection = null;
      String msg = "";

      InputStream in = null;

      try
      {
         // open the connection
         connection = (HttpURLConnection)url.openConnection();

         connection.setRequestMethod("GET");

         // Make sure browser doesn't cache this URL.
         connection.setUseCaches(false);
         
         if (StringUtils.isNotBlank(sendContentType))
            connection.setRequestProperty("Content-Type", sendContentType);
         


         int respCode = connection.getResponseCode();

         String sContentLength = connection.getHeaderField("Content-Length");
         int contentLength = (sContentLength != null) ?
            Integer.parseInt(sContentLength) : -1;
         try
         {
            in = connection.getInputStream();
         }
         catch(IOException e)
         {
            in = connection.getErrorStream();
         }

         ByteArrayOutputStream os = new ByteArrayOutputStream();
         long readData = IOTools.copyStream(in, os);
         byte[] byteData = os.toByteArray();

         // Make sure received all the data if specified "Content-Length"
         if ( (contentLength >0) && (long) contentLength != readData)
         {
            String [] args = {""+readData, ""+contentLength};
            throw new PSException(IPSUtilErrors.RECEIVE_DATA_ERROR, args);
         }

         // Get the character-set for the received data
         String rcvContentType = connection.getHeaderField("Content-Type");
         String charset = PSCharSets.rxJavaEnc();
         if (rcvContentType != null)
         {
            Map params = new HashMap();
            PSBaseHttpUtils.parseContentType(rcvContentType, params);
            charset = (String) params.get("charset");
            if (charset == null)
               charset = PSCharSets.rxJavaEnc();
            else
               charset = PSCharSets.getJavaName(charset);
         }

         // Convert byte[] to string
         msg = new String(byteData, charset);

         // if error, print to console,
         // then throw exception with error code and message
         if (respCode >= 400)
         {
            String [] args = {Integer.toString(respCode), msg};
            throw new PSException(IPSUtilErrors.POST_DATA_ERROR, args);
         }
      }
      finally
      {
         // don't call connection.disconnect() as this will not allow keep-alive
         // instead close the streams to release resources, but allow the
         // peristent connection to remain open.
         if (in != null)
            try { in.close(); } catch (IOException e) {}

      }

      return msg;
   }
   
   /**
    * Append PS Session ID parameter to the query string of the specified URL.
    * Do nothing if the URL is used to request the PS Session ID.
    * This is needed for Firefox in the following scenario:
    * <UL>
    * <LI> Bring up Firefox, login CX
    * <LI> JSESSIONID and/or PS Session expired (due to either server 
    *      restart or session expired (more than 2 hours, for example)
    * <LI> Bring a new (after close down the current) Firefox session or refresh
    *      the same Firefox window, login CX, get new JSESSIONID (and 
    *      pssessionid)
    * <LI> perform some operation in CX (for example, bring up an Content Editor
    * <LI> Some of the request from java applet may not have the
    *      authenticated cookie, which is retained from step [3].
    * </UL>
    * <p>
    * Note, this will not be called when requesting the PS Session ID for the
    * current applet session.
    * 
    * @param url the URL in question, assumed not <code>null</code>.
    * 
    * @return the URL that has appended PS Session ID parameter. It can never 
    * be <code>null</code>.
    */
   public URL appendPsSessionId(URL url)
   {
      String sUrl = url.toString();
      if (url.toString().endsWith(GET_SESSIONID_URL))
         return url;

      if (sUrl.indexOf("pssessionid=") != -1)
         return url;
      
      if (sUrl.indexOf('?') == -1)
         sUrl = sUrl + "?pssessionid=" + getPsSessionId();
      else
         sUrl = sUrl + "&pssessionid=" + getPsSessionId();
      
      try
      {
         return new URL(sUrl);
      }
      catch (MalformedURLException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Gets the PS Session ID of the current applet session.
    * 
    * @return the PS Session ID, never <code>null</code> or empty.
    */
   private synchronized String getPsSessionId()
   {
      if (ms_psSessionId != null)
         return ms_psSessionId;
      
      try
      {
         URL url = new URL(ms_baseUrl, GET_SESSIONID_URL);
         String response = PSHttpConnection.postData(url, "", null);
         // the response is in the following format:
         //
         //     var pssessionid = "30b30e2603585e0fc198aef6207d1c9d116226a3";
         int beginIndex = response.indexOf("\"");
         int lastIndex = response.lastIndexOf("\"");
         if (beginIndex >= lastIndex)
         {
            String errorMsg = "Failed to get pssessionid. The response data is: " + response;
            System.out.println(errorMsg);
            throw new RuntimeException(errorMsg);
         }

         String sId = response.substring(beginIndex+1, lastIndex);
         //System.out.println("Session ID: " + sId);
         ms_psSessionId = sId;
         return ms_psSessionId;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   /**
    * Gets the PS Session ID of the current applet session.
    * 
    * @return the PS Session ID, never <code>null</code> or empty.
    */
   public synchronized String getCurrentPsSessionId()
   {
      return ms_psSessionId;
   }
   
   public URL getBaseUrl()
   {
      return ms_baseUrl;
   }
   /**
    * Reset the cached info which may contain data used from previous 
    * applet session. This is called during applet initialization (that is
    * called from {@link PSContentExplorerApplet#init()}).
    * 
    * @param baseUrl the new base URL of the applet session. It is in the
    * format of protocol://host:port/Rhythmyx/sys_resources.
    * @param psSessionId the value of "pssessionid" applet parameter. It may
    * be <code>null</code> or empty if it is not specified.
    */
   
   /**
    * The current base URL of the applet session. This is used to request
    * the PS Session ID for the current applet (or JSESSIONID) session.
    */
   private URL ms_baseUrl = null;
   
   /**
    * The current PS Session ID. Default to <code>null</code> if has not been
    * set for current applet session.
    */
   private String ms_psSessionId = null;
   
   /**
    * The URL used to retrieve the PS Session ID from server.
    */
   private static final String GET_SESSIONID_URL = "/util/getPSSessionID.jsp";
   
   /**
    * Set the boundary for posting data of type "multipart/form-data"
    * @param boundary
    */
   public void setBoundary(String boundary)
   {
      ms_boundary = StringUtils.isEmpty(boundary) ? DEFAULT_BOUNDARY : boundary;  
   }
   
   /**
    * Initial value boundary for posting data of type "multipart/form-data"
    */
   public static final String CX_BOUNDARY = "||--------------------RxCxApplet";
   
   /**
    * Default boundary for posting data of type "multipart/form-data"
    */
   public static final String DEFAULT_BOUNDARY = 
        "||--------------------7d310e3120318";

   /**
    * Default value for creating a unique boundary for "multipart/form-data"
    */
   private String ms_boundary = DEFAULT_BOUNDARY;
}
