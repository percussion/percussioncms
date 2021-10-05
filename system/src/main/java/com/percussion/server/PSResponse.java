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


package com.percussion.server;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.error.PSErrorManager;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.util.PSCountWriter;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * The PSResponse class hides the details of the HTTP protocol, allowing
 * for simplified response generation. This also contains all the
 * HTTP status codes for easy reference.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSResponse extends PSBaseResponse
{
   /**
    * Construct a response object.
    *
    * @param   keepAlive   the Connection header sent with the request
    *
    * @param   loc         the locale to use
    */
   public PSResponse(String keepAlive, Locale loc)
   {
      super(keepAlive, loc);
   }

   /**
    * Construct a response object.
    *
    * @param   keepAlive   the Connection header sent with the request
    */
   public PSResponse(String keepAlive)
   {
      this(keepAlive, Locale.getDefault());
   }

   /**
    * Set the status code and text to use as the body of the message.
    * The body can contain plain text or HTML, which will be wrapped with
    * the <HTML> and </HTML> tags.
    *
    * @param   statusCode         the HTTP status code to return
    *
    * @param   statusMessage      the status message to provide
    */
   public void setStatus(int statusCode, String statusMessage)
   {
      // set the status line
      setStatus(statusCode);

      // and the content
      byte[] ba         = (new String(   "<HTML>" + statusMessage +
                                       "</HTML>")).getBytes();
      try(ByteArrayInputStream bs = new ByteArrayInputStream(ba)) {
         resetContentStream(bs);
         m_contentLength = ba.length;
         setEntityHeader(EHDR_CONT_LENGTH, String.valueOf(m_contentLength));
         setEntityHeader(EHDR_CONT_TYPE, IPSMimeContentTypes.MIME_TYPE_TEXT_HTML);
      } catch (IOException e) {
         PSConsole.printMsg("Server", e);
      }
   }

   /**
    * Set the status code to use on the status line. Use setContent to
    * set the message body, unless a message body is not required for
    * the specified status code.
    *
    * @param   statusCode         the HTTP status code to return
    */
   public void setStatus(int statusCode)
   {
      try
      {
         // the status line is: HTTP-Version SP Status-Code SP Reason-Phrase
         m_statusCode = statusCode;
         m_statusLine = new String(   HTTP_VERSION + " " +
            String.valueOf(statusCode) + " " +
            PSErrorManager.getErrorText(statusCode, false, m_locale)).getBytes(
               "US-ASCII");
      }
      catch (java.io.UnsupportedEncodingException e)
      {
         PSConsole.printMsg("Server", e);
      }
   }

   /**
    * Set the status code to use on the status line (default text is used).
    *
    * @param   header         the header field name
    *
    * @param   value            the header field value
    */
   public void setHeader(String header, String value)
   {
      if (!setGeneralHeader(header, value))
         if (!setResponseHeader(header, value))
            setEntityHeader(header, value);   // this is the catch all
   }

   /**
    * Set a general header field.
    *
    * @param   header         the header field name
    *
    * @param   value            the header field value
    *
    * @return                  <code>true</code> if the header field was
    *                                                                           added as a general header field;
    *                                                                           <code>false</code> if it wasn't, in which
    *                                                                           case it may be a response or entity header
    *                                                                           field
    */
   public boolean setGeneralHeader(String header, String value)
   {
      String useHdr = (String)ms_GeneralHeaders.get(header.toLowerCase());
      if (useHdr == null)
         return false;

      if (m_generalHeaders == null)
         m_generalHeaders = new HashMap();

      m_generalHeaders.put(useHdr, value);
      return true;
   }

   /**
    * Set a response header field.
    *
    * @param   header         the header field name
    *
    * @param   value            the header field value
    *
    * @return                  <code>true</code> if the header field was
    *                                                                           added as a response header field;
    *                                                                           <code>false</code> if it wasn't, in which
    *                                                                           case it may be a general or entity header
    *                                                                           field
    */
   public boolean setResponseHeader(String header, String value)
   {
      String useHdr = (String)ms_ResponseHeaders.get(header.toLowerCase());
      if (useHdr == null)
         return false;

      // *TODO* we should add a check to see if this is a cookie, in
      // which case we need to parse the name and value and call the
      // setCookie method

      if (m_responseHeaders == null)
         m_responseHeaders = new HashMap();

      m_responseHeaders.put(useHdr, value);
      return true;
   }

   /**
    * Set an entity header field.
    *
    * @param   header         the header field name
    *
    * @param   value            the header field value
    *
    * @return                  <code>true</code> if the header field was
    *                                                                           added as an entity header field;
    *                                                                           <code>false</code> if it wasn't, in which
    *                                                                           case it may be a general or response header
    *                                                                           field
    */
   public boolean setEntityHeader(String header, String value)
   {
      String useHdr = (String)ms_EntityHeaders.get(header.toLowerCase());
      if (useHdr == null)   // treat this as an entity extension
         useHdr = header;

      if (m_entityHeaders == null)
         m_entityHeaders = new HashMap();

      m_entityHeaders.put(useHdr, value);
      return true;
   }

   public String getEntityHeader(String header)
   {
      String ret = null;
      String useHdr = (String)ms_EntityHeaders.get(header.toLowerCase());
      if (useHdr == null)   // treat this as an entity extension
         useHdr = header;

      if (m_entityHeaders != null)
      {
         ret = (String)m_entityHeaders.get(useHdr);
      }
      return ret;
   }

   /**
    * Set a cookie. Cookies are actually response header fields, however,
    * there may be multiple cookies in the header. To avoid overwriting
    * other cookies, use of this method is recommended.
    *
    * @param   cookie         the name of the cookie
    *
    * @param   value            the cookie's value (including any optional
    *                                                                           data)
    */
   public void setCookie(String name, String value)
   {
      if (m_cookies == null)
         m_cookies = new HashMap();

      m_cookies.put(name, value);
   }

   /**
    * @return the cookies
    */
   public HashMap getCookies()
   {
      return m_cookies;
   }

   /* protected call to set cookies, used by PSRequestContext */
   void setCookies(HashMap cookies)
   {
      m_cookies = cookies;
   }

   /**
    *                                                      A convenience routine for setting a cookie which will be sent along
    *                                                         with the response.<p>
    *
    *                                                      Cookies are often used to provide context information (state) between
    *                                                         and server.
    *
    *                                                      @param      name   the name of the cookie (eg, mycookie)
    *
    *                                                      @param      value   the value of the cookies (eg, myvalue)
    *
    *                                                      @param      path   the URL path the cookie is valid for (eg, / for
    *                                                                           the entire site, /x for requests in the /x path,
    *                                                                           etc.)
    *
    *                                                      @param      domain   the host the cookie is valid for (eg, www.percussion.com)
    *
    *                                                      @param      expires   the date the cookie expires on
    *
    *                                                      @param      secure   <code>true</code> if the cookie should only be
    *                                                                              sent over HTTPS connections
    */
   public void setCookie(String name, String value, String path,
                                 String domain, java.util.Date expires,
                                 boolean secure)
   {
      StringBuilder cookieValue = new StringBuilder();

      cookieValue.append((value == null) ? "" : value);
      if (path != null)
         cookieValue.append("[;path="+path+"]");
      if (domain != null)
         cookieValue.append("[;domain="+domain+"]");
      if (expires != null)
      {
         cookieValue.append("[;expires=" + ms_cookieDateFormat.format(expires) + "]");
      }
      if (secure == true)
      {
         cookieValue.append("[;secure]");
      }

      setCookie(name, cookieValue.toString());
   }


   /**
    * A simpler version of the 4 parameter method ({@link #setContent(
    * InputStream, long, String, boolean) setContent }. Defaults the flag that
    * indicates the charset parameter of the ContentType header variable should
    * be set if it is not.
    */
   public void setContent(InputStream content, long length, String type )
   {
      /* Fix for bug Rx00-04-0004. This is the original version of this method.
         In some cases, it is desired to override this behavior. In these cases
         the 4 param method can be called directly. */
      setContent( content, length, type, true );
   }

   /**
    * Set the content (message body) associated with the response. The
    * input stream (content) should not be touched in any way by the
    * caller until a call to send or clear has been made.
    *
    * @param content an input stream from which to read the content.  May be
    * <code>null</code>.  This method assumes ownership of the stream, and it
    * will be closed once the response has been sent.
    *
    * @param length the amount of data in the stream
    *
    * @param type the content type (eg, text/xml)
    *
    * @param addCharsetSpec If <code>true</code>, will add the 'charset='
    * param to the ContentType header if it's not already there. The value will
    * be the Rx server standard encoding.
    */
   public void setContent(InputStream content, long length, String type,
         boolean addCharsetSpec )
   {
      if (content == null)
         length = 0;

      setEntityHeader(EHDR_CONT_LENGTH,   String.valueOf(length));
      setEntityHeader(EHDR_CONT_TYPE,      type);

      // bug# Rx-99-12-0015: set content type encoding for text
      type = type.toLowerCase().trim();
      if (!type.startsWith("text/")
         && !type.equals(IPSMimeContentTypes.MIME_TYPE_APPLICATION_XML)
         && !type.equals(IPSMimeContentTypes.MIME_TYPE_APPLICATION_XSL)
         && !type.equals(IPSMimeContentTypes.MIME_TYPE_APPLICATION_DTD))
      {
         setEntityHeader(EHDR_CONT_TRF_ENC, "binary");
      }
      else
      {
         // what charset are we going to write this with ?
         String charSet = null;
         HashMap contentParams = new HashMap();
         try
         {
            PSBaseHttpUtils.parseContentType(type, contentParams);
         }
         catch (IllegalArgumentException e)
         {
            /* ignore malformed user-set content-type header */
         }
         charSet = (String)contentParams.get("charset");
         if (charSet == null && addCharsetSpec )
         {
            charSet = PSCharSets.rxStdEnc(); // user did not specify
            type = type + "; charset=" + charSet;
         }
      }

      setEntityHeader(EHDR_CONT_LENGTH,   String.valueOf(length));
      setEntityHeader(EHDR_CONT_TYPE,      type);

      resetContentStream(content);
      m_contentDoc      = null;
      m_isContentDoc      = false;
      m_contentLength   = length;
   }

   /**
    * Reset the content stream to the specified input stream.
    * The existing content stream will be closed afterwards if it is not
    * <code>null</code>.
    * <p>
    * NOTE: this should only be called if the caller is responsible to
    *       set other corresponding values, such as m_contentLength, or
    *       the new input stream contains exactly the same content as
    *       the existing one.
    *
    * @param content The to be set content stream, it may be <code>null</code>.
    */
   protected void resetContentStream(InputStream content)
   {
      if (m_contentStream != null)
      {
         try {
            m_contentStream.close();
         }
         catch (Exception e){} // ignore error
      }
      m_contentStream = content;
   }

   /**
    * Set the content (message body) associated with the response to
    * an XML document, using MIME type text/xml.
    * The XML document should not be touched in any way
    * by the caller until a call to send or clear has been made.
    *
    * @param   doc            the XML document to use as the output
    */
   public void setContent(Document doc)
   {
      setContent(doc,
         IPSMimeContentTypes.MIME_TYPE_TEXT_XML
         + "; charset=" + PSCharSets.rxStdEnc());
   }

   /**
    * Set the content (message body) associated with the response to
    * an XML document. The XML document should not be touched in any way
    * by the caller until a call to send or clear has been made.
    *
    * @param   doc            the XML document to use as the output
    *
    * @param   type            the content type (eg, text/xml)
    */
   public void setContent(Document doc, String type)
   {
      long length = 0;

      if (doc != null) {
         try {

            // what charset are we going to write this with ?
            String charSet = null;
            HashMap contentParams = new HashMap();
            try
            {
               PSBaseHttpUtils.parseContentType(type, contentParams);
            }
            catch (IllegalArgumentException e)
            {
               /* ignore malformed user-set content-type header */
            }
            charSet = (String)contentParams.get("charset");
            if (charSet == null)
            {
               charSet = PSCharSets.rxStdEnc(); // user did not specify
               type = type + "; charset=" + charSet;
            }
            PSCountWriter counter = null;
            try {
               counter = new PSCountWriter(charSet);
               PSXmlDocumentBuilder.write(doc, counter, charSet);
               length = counter.getLength();
            } finally {
               if (counter!=null) try {counter.close();} catch (Exception e) {/*ingnore*/};
            }
         }
         catch (Exception e)
         {
            Object[] args = { "PSXmlDocumentBuilder/write",
                     com.percussion.error.PSException.getStackTraceAsString(e) };
               com.percussion.log.PSLogManager.write(
                  new com.percussion.log.PSLogServerWarning(
                  com.percussion.server.IPSServerErrors.ARGUMENT_ERROR, args,
                  true, "PSResponse"));
         }
      }

      setEntityHeader(EHDR_CONT_LENGTH, String.valueOf(length));
      setEntityHeader(EHDR_CONT_TYPE, type);

      m_contentDoc      = doc;
      resetContentStream(null);
      m_isContentDoc      = true;
      m_contentLength   = length;
   }


   /**
    * Prepares the reponse to send a redirection response by setting the HTTP
    * status code to <code>IPSHttpErrors.HTTP_MOVED_TEMPORARILY</code> and the
    * Location header to the URL provided.  Does not actually send the response.
    * If the request has the IPSHtmlParameters.WIFXUPLOAD flag set 
    * to <code>true</code> then send the special WebImageFx response and
    * special redirect header that will be handled by javascript.
    * 
    * @param url the URL to redirect to; cannot be <code>null</code> or empty
    * @param request the {@link com.percussion.server.PSRequest} object;
    * cannot be <code>null</code> or empty.
    * @throws IOException if an I/O error occurs
    */
   public void sendRedirect(String url, PSRequest request)
      throws IOException
   {
      if (null == url || url.trim().length() == 0)
         throw new IllegalArgumentException("Must provide a valid URL.");

      // IE only supports URLs up to 2083 characters (KB Article: #Q208427)
      if (url.length() > 2083)
      {
         Object[] args = {url, "2083"};
         PSLogManager.write(
            new PSLogServerWarning(
               IPSServerErrors.REDIRECT_URL_TOO_LONG, args));
      }
      
      String wifxUpload = 
         request.getParameter(IPSHtmlParameters.WIFXUPLOAD);
      if(wifxUpload != null && 
         (wifxUpload.equalsIgnoreCase("true") ||
         wifxUpload.equalsIgnoreCase("yes")))
      {           
         setWifxUploadRedirect(url);          
      }
      else
      {
         setStatus(IPSHttpErrors.HTTP_MOVED_TEMPORARILY);
         setHeader(RHDR_LOCATION, url);
      }
      
      
   }
   
   /**
    * Sends the special WebImageFx response and sets the special redirect
    * header "WebImageFx-Redirect" which is intended to be parsed by the
    * javascript in the content editor.
    * 
    * @param url the url to redirect to , cannot be <code>null</code> or
    * empty.
    * @throws IOException if an I/O error occurs
    */
   private void setWifxUploadRedirect(String url) throws IOException
   {
      if(null == url || url.trim().length() == 0)
         throw new IllegalArgumentException("Must provide a valid URL.");
      
      ByteArrayInputStream bis = 
         new ByteArrayInputStream(
            ms_wifxResponse.toString().trim().
               getBytes(PSCharSetsConstants.rxJavaEnc()));
      setStatus(200);
      setHeader(CRHDR_WIFX_REDIRECT, url.trim());
      setContent(bis, ms_wifxResponse.length(), "text/xml");
   }


   /**
    * Sends the actual output, the headers are written by the servlet code.
    *
    * @param os <code>OutputStream</code> to send the output to
    * @exception   IOException         if an i/o error occurs
    */
   public void send(OutputStream os)
      throws IOException
   {
      try {
         if (m_contentLength != 0L)
         {
            if (m_isContentDoc)
            {
               /* write the xml data */

               // figure out what character set we should use to write this
               String contentType = getEntityHeader(EHDR_CONT_TYPE);
               String charSet = null;
               if (contentType != null)
               {
                  HashMap contentParams = new HashMap();
                  try
                  {
                     PSBaseHttpUtils.parseContentType(contentType, contentParams);
                  }
                  catch (IllegalArgumentException e)
                  {
                     /* ignore malformed user-set content-type header */
                  }
                  charSet = (String)contentParams.get("charset");
               }

               if (charSet == null)
                  charSet = "ISO-8859-1";

               /* and finally the content (if any) */
               Writer writer = new OutputStreamWriter(os, charSet);
               PSXmlDocumentBuilder.write(m_contentDoc, writer, charSet);
            }
            else // writing bytes, no character conversion
            {
               /* write the output stream */
               byte[] data = new byte[DEFAULT_BUF_SIZE];
               int iCur;

               for (long iTotal = 0L; iTotal < m_contentLength; iTotal += iCur) {
                  iCur = m_contentStream.read(data);
                  if (iCur == -1) {
                     /* this should never happen! We need to log this as
                      * Content-Length is too big which will cause the client
                      * to wait for the rest of the data (in other words,
                      * timeout)
                      */
                     break;
                  }

                  os.write(data, 0, iCur);
               }
            }
         }

         os.flush();
      }
      catch (SocketException e)
      {
         // ignore socket exceptions if status is HTTP_NOT_MODIFIED.  This
         // happens when IE closes the socket as soon as it gets this status,
         // before we've finished writing to the socket.
         if (m_statusCode != IPSHttpErrors.HTTP_NOT_MODIFIED)
            throw e;
      }
      finally
      {
         if (m_contentStream != null)
            try {m_contentStream.close();} catch (IOException e){}

         clear();
      }
   }

   /**
    * Flags whether this repsonse contains a result that is an error rather than
    * the content expected by the requestor.
    *
    * @param isError <code>true</code> indicates the response is an error
    * message, <code>false</code> indicates it is the expected response.
    */
   public void setIsErrorResponse(boolean isError)
   {
      m_isError = isError;
   }

   /**
    * Determines if this repsonse contains a result that is an error rather than
    * the content expected by the requestor.
    *
    * @return <code>true</code> if is contains an error message,
    * <code>false</code> if it will send the expected results.
    */
   public boolean isErrorResponse()
   {
      return m_isError;
   }

   /**
    * Write a set of headers.
    *
    * @param      buf               the buffer to write to
    *
    *   *   @param      headers            the headers to write
    *
    * @exception   IOException         if an i/o error occurs
    */
   private void writeHeaders(   BufferedOutputStream buf,
                              HashMap headers)
      throws IOException
   {
      if (headers != null)
         if (headers.size() > 0) {
            /* go through each header and write it out */
            Iterator keys      = headers.keySet().iterator();
            Iterator values   = headers.values().iterator();

            for (; keys.hasNext(); ) {
               String key = (String)keys.next();
               String value = (String)values.next();

               buf.write(key.getBytes("US-ASCII"));;
               buf.write(HEADER_VALUE_BREAK);
               buf.write(value.getBytes("US-ASCII"));
               buf.write(CR_LF);
            }
         }
   }

   /**
    * Write the cookies as response headers.
    *
    * @param      buf               the buffer to write to
    *
    * @exception   IOException         if an i/o error occurs
    */
   private void writeCookies(BufferedOutputStream buf)
      throws IOException
   {
      if (m_cookies != null)
         if (m_cookies.size() > 0) {
            /* go through each header and write it out */
            Iterator keys      = m_cookies.keySet().iterator();
            Iterator values   = m_cookies.values().iterator();

            for (; keys.hasNext(); ) {
               String key = (String)keys.next();
               String value = (String)values.next();

               buf.write(RHDR_SET_COOKIE.getBytes("US-ASCII"));
               buf.write(HEADER_VALUE_BREAK);
               buf.write(key.getBytes("US-ASCII"));
               buf.write(("=").getBytes("US-ASCII"));
               buf.write(value.getBytes("US-ASCII"));
               buf.write(CR_LF);
            }
         }
   }

   /**
    * This flag indicates whether or not this is an error response. Initialized
    * to <code>false</code>, set through {@link #setIsErrorResponse(boolean)}.
    */
   private boolean m_isError = false;

   /**
    * The response close delay in milliseconds, can be set by using
    * server property "responseCloseDelay". Defaults to 10 milliseconds,
    * and can never be less then 10 milliseconds.
    */
   private static int ms_delay = 10;
   static
   {
      String delayString = 
         PSServer.getServerProps().getProperty(
            PSServer.PROP_RESP_CLOSE_DELAY, "10");
      try
      {               
         ms_delay = Integer.parseInt(delayString);
         if(ms_delay < 10)
            ms_delay = 10;
      }
      catch(NumberFormatException ignore){}
   }
   
   
   /**
    * Special xml response needed for WebImageFx
    */
   private static final StringBuilder ms_wifxResponse =
      new StringBuilder();
      
   static
   {   
      ms_wifxResponse.append("<XML ID=EktronFileIO>\n"); 
      ms_wifxResponse.append("<?xml version=\"1.0\"?>\n");
      ms_wifxResponse.append("<UPLOAD>\n");
      ms_wifxResponse.append("<FILEINFO ID=\"0\" discard=\"False\">\n");
      ms_wifxResponse.append("<FSRC>C:\\dummy\\wwwroot\\Arrows\\");
      ms_wifxResponse.append("next0.gif</FSRC>\n");
      ms_wifxResponse.append("<FURL>http://www.dummy.com/ewebeditpro4/");
      ms_wifxResponse.append("upload/me(1).gif</FURL>\n");
      ms_wifxResponse.append("<FID></FID>\n");
      ms_wifxResponse.append("<FSIZE>128</FSIZE>\n");
      ms_wifxResponse.append("<DESC></DESC>\n");
      ms_wifxResponse.append("<THUMBURL></THUMBURL>\n");
      ms_wifxResponse.append("<THUMBHREF></THUMBHREF>\n");
      ms_wifxResponse.append("<FTYPE>image/gif</FTYPE>\n");
      ms_wifxResponse.append("<DWIDTH>0</DWIDTH>\n");
      ms_wifxResponse.append("<DHEIGHT>0</DHEIGHT>\n");
      ms_wifxResponse.append("<DBORDER>0</DBORDER>\n");
      ms_wifxResponse.append("<FRAGMENT></FRAGMENT>\n");
      ms_wifxResponse.append("<FERROR value=\"0\"></FERROR>\n");
      ms_wifxResponse.append("</FILEINFO>\n");
      ms_wifxResponse.append("</UPLOAD>\n");
      ms_wifxResponse.append("</XML>\n");     
   }

}

