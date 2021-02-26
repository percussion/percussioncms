/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.server;

import com.percussion.utils.date.PSDateFormatter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * An abstract class used for all HTTP responses as a base. Container for
 * generic members used.
 */
public abstract class PSBaseResponse implements Serializable
{
   /**
    * Constructs an new response for the supplied keep alive option and locale.
    * 
    * @param keepAlive string that specifies whether or not to keep connections
    *    alive. Keep alive is currently not supported and this parameter will
    *    be ignored. 
    * @param loc the locale to use, if <code>null</code> is provided we will use
    *    the default locale.
    */
   public PSBaseResponse(String keepAlive, Locale loc)
   {
      if (loc == null)
         m_locale = Locale.getDefault();
      else
         m_locale   = loc;
      
      /**
       * DO NOT HONOR KEEP-ALIVE UNLESS WE QUEUE CONNECTIONS IN THE LISTENER!!!
       *    if (keepAlive != null)
       *         m_keepAlive = keepAlive.equalsIgnoreCase(CONN_KEEP_ALIVE);
       *    else
       *       m_keepAlive = false;
       */    
   }

   /**
    * Constructs an new response for the supplied keep alive option and the
    * default locale.
    * 
    * @param keepAlive string that specifies whether or not to keep connections
    *    alive. Keep alive is currently not supported and this parameter will
    *    be ignored.
    */ 
   public PSBaseResponse(String keepAlive)
   {
      this(keepAlive, Locale.getDefault());
   }
   
   /**
    * Copy constructor.
    * 
    * @param resp the response to be copied from, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied object is 
    *    <code>null</code>.
    */
   public PSBaseResponse(PSBaseResponse resp)
   {
      copyFrom(resp);
   }

   /**
    * Create a copy from the supplied object.  A shallow copy of all 
    * header maps will be made.  Cookies are not copied.
    * 
    * @param resp the object to copy from, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied object is 
    *    <code>null</code>.
    */
   public void copyFrom(PSBaseResponse resp)
   {
      if (resp == null)
        throw new IllegalArgumentException("response cannot be null");
        
      m_statusLine = resp.m_statusLine;
      if (resp.m_generalHeaders != null)
         m_generalHeaders = new HashMap(resp.m_generalHeaders);
      if (resp.m_responseHeaders != null)
         m_responseHeaders = new HashMap(resp.m_responseHeaders);
      if (resp.m_entityHeaders != null)
         m_entityHeaders = new HashMap(resp.m_entityHeaders);
      m_isContentDoc = resp.m_isContentDoc;
      m_contentLength = resp.m_contentLength;
      m_contentStream = resp.m_contentStream;
      m_contentDoc = resp.m_contentDoc;
      m_keepAlive = resp.m_keepAlive;
      m_locale = resp.m_locale;
      m_statusCode = resp.m_statusCode;
   }

   /**
    * Reset the response, removing all header, status and content information.
    */
   public void clear() 
   {
      if (m_generalHeaders != null)
         m_generalHeaders.clear();
      
      if (m_responseHeaders != null)
         m_responseHeaders.clear();
      
      if (m_entityHeaders != null)
         m_entityHeaders.clear();
      
      m_statusLine = HTTP_OK_STATUS_LINE;
      m_statusCode = IPSHttpErrors.HTTP_OK;
      m_contentStream = null;
      m_contentDoc = null;
      m_isContentDoc = false;
      m_contentLength = 0;
   }

   /**
    * Get the preferred locale for sending responses.
    * 
    * @return the preferred locale, never <code>null</code>.
    */
   public Locale getPreferredLocale()
   {
      return m_locale;
   }

   /**
    * Get the length of the content set on this response.
    * 
    * @return The length, may be <code>0</code> if no content has been set on
    * this response.
    */
   public long getContentLength()
   {
      return m_contentLength;
   }

   /**
    * @return Returns the contentDoc.
    */
   public Document getContentDoc()
   {
      return m_contentDoc;
   }
   /**
    * @return Returns the contentStream.
    */
   public InputStream getContentStream()
   {
      return m_contentStream;
   }
   /**
    * @return Returns the entityHeaders.
    */
   public HashMap getEntityHeaders()
   {
      return m_entityHeaders;
   }
   /**
    * @return Returns the generalHeaders.
    */
   public HashMap getGeneralHeaders()
   {
      return m_generalHeaders;
   }
   /**
    * @return Returns the isContentDoc.
    */
   public boolean isContentDoc()
   {
      return m_isContentDoc;
   }
   /**
    * @return Returns the keepAlive.
    */
   public boolean isKeepAlive()
   {
      return m_keepAlive;
   }
   /**
    * @return Returns the locale.
    */
   public Locale getLocale()
   {
      return m_locale;
   }
   /**
    * @return Returns the responseHeaders.
    */
   public HashMap getResponseHeaders()
   {
      return m_responseHeaders;
   }
   /**
    * @return Returns the statusCode.
    */
   public int getStatusCode()
   {
      return m_statusCode;
   }
   /**
    * @return Returns the statusLine.
    */
   public byte[] getStatusLine()
   {
      return m_statusLine;
   }
   /**
    * Parses the supplied date String.
    * 
    * @param date the date String to parse, assumes the HTTP date format.
    * @return the parsed Date object, never <code>null</code>.
    * @throws ParseException if the supplied String could not be parsed.
    */
   public static Date parseDateFromHeader(String date) throws ParseException
   {
      return PSDateFormatter.parseDateFromHttp(date);
   }
    
   /**
    * Format a date for use as a header. HTTP requires dates in the following 
    * format:
    * <p>
    * Wed, 15 Nov 1995 04:58:08 GMT
    * 
    * @param date the date to be formatted, assumed not <code>null</code>.
    * @return the formatted date String, never <code>null</code>.
    */
   public static String formatDateForHeader(Date date)
   {
      return PSDateFormatter.formatDateForHttp(date);
   }

   /** Date format for cookies */
   protected static final FastDateFormat ms_cookieDateFormat =
      FastDateFormat.getInstance("EEE, dd-MMM-yyyy HH:mm:ss zzz");

   /** 
    * The response status line, might be <code>null</code>.
    */
   protected byte[] m_statusLine = HTTP_OK_STATUS_LINE;
   
   /**
    * The response status code, initialized to {@link IPSHttpErrors#HTTP_OK},
    * may be modified after that.
    */
   protected int m_statusCode = IPSHttpErrors.HTTP_OK;

   /** 
    * The general headers for the current response, might be <code>null</code>.
    */
   protected HashMap m_generalHeaders = null;

   /**
    * The response headers for the current response, might be <code>null</code>.
    */
   protected HashMap m_responseHeaders = null;

   /**
    * The entity headers for the current response, might be <code>null</code>.
    */
   protected HashMap m_entityHeaders = null;

   /**
    * Container for all cookies of this response, might be <code>null</code>.
    */
   protected HashMap m_cookies = null;

   /**
    * Flag to indicate whether this is a normal text response or a document
    * response. If <code>true</code>, the response content is maintained in a 
    * <code>Document</code>, otherwise its maintained in an 
    * <code>InputStream</code>. 
    */
   protected boolean m_isContentDoc =false;

   /**
    * The content lenght.
    */
   protected long m_contentLength = 0;

   /**
    * The content if the <code>m_isContentDoc</code> is <code>false</code>, 
    * might be <code>null</code>. This is transient because serialization has
    * to be special for this.
    */
   transient protected InputStream m_contentStream = null;
   
   /**
    * The content if the <code>m_isContentDoc</code> is <code>true</code>, 
    * might be <code>null</code>. This is transient because serialization has
    * to be special for this.
    */
   transient protected Document m_contentDoc = null;

   /**
    * A flag specifying whether or not to keep connections alive. Keep alive
    * is currently not supported and this will always be set to 
    * <code>false</code>.
    */
   protected boolean m_keepAlive = false;

   /**
    * The locale to use. Initialized in the constructor, never <code>null</code>
    * after that.
    */
   protected Locale m_locale = null;

   /**
    * The status line used for successful requests. Initialized while 
    * constructed, never changed after that.
    */
   protected static byte[] HTTP_OK_STATUS_LINE = null;

   /**
    * A constant for carriage return / line feed. Initialized once while
    * constructed, never changed after that.
    */
   protected static byte[] CR_LF = null;

   /**
    * A constant used as header break. Initialized once while consturcetd, never
    * changed after that.
    */
   protected static byte[] HEADER_VALUE_BREAK = null;

   /**
    * The supported HTTP version string. Never changed.
    */
   protected static final String HTTP_VERSION = "HTTP/1.0";
   
   /**
    * A constant for the keep alive flag. Never changed.
    */
   protected static final String CONN_KEEP_ALIVE = "Keep-Alive";

   /**
    * The following constants define all general header fields. For more
    * information consult the HTTP 1.1 specification.
    */
   public static final String   GHDR_CACHE_CONTROL = "Cache-Control";
   public static final String   GHDR_CONNECTION = "Connection";
   public static final String   GHDR_DATE = "Date";
   public static final String   GHDR_PRAGMA = "Pragma";
   public static final String   GHDR_TRANSFER_ENC = "Transfer-Encoding";
   public static final String   GHDR_UPGRADE = "Upgrade";
   public static final String   GHDR_VIA = "Via";

   /**
    * The following constants define all response header fields. For more
    * information consult the HTTP 1.1 specification.
    */
   public static final String   RHDR_AGE = "Age";
   public static final String   RHDR_LOCATION = "Location";
   public static final String   RHDR_PROXY_AUTH = "Proxy-Authenticate";
   public static final String   RHDR_PUBLIC = "Public";
   public static final String   RHDR_RETRY_AFTER = "Retry-After";
   public static final String   RHDR_SERVER = "Server";
   public static final String   RHDR_VARY = "Vary";
   public static final String   RHDR_WARNING = "Warning";
   public static final String   RHDR_SET_COOKIE = "Set-Cookie";
   public static final String   RHDR_WWW_AUTH = "WWW-Authenticate";
   public static final String   RHDR_ACCEPT_RANGES = "Accept-Ranges";
   
   /**
    * Custom response header fields
    */
   public static final String CRHDR_WIFX_REDIRECT = "WebImageFX-Redirect";

   /**
    * The following constants define all entity header fields. For more
    * information consult the HTTP 1.1 specification.
    */
   public static final String   EHDR_ALLOW = "Allow";
   public static final String   EHDR_CONT_BASE = "Content-Base";
   public static final String   EHDR_CONT_ENC = "Content-Encoding";
   public static final String   EHDR_CONT_TRF_ENC = "Content-Transfer-Encoding";
   public static final String   EHDR_CONT_LANG = "Content-Language";
   public static final String   EHDR_CONT_LENGTH = "Content-Length";
   public static final String   EHDR_CONT_LOC = "Content-Location";
   public static final String   EHDR_CONT_MD5 = "Content-MD5";
   public static final String   EHDR_CONT_RANGE = "Content-Range";
   public static final String   EHDR_CONT_TYPE = "Content-Type";
   public static final String   EHDR_ETAG = "ETag";
   public static final String   EHDR_EXPIRES = "Expires";
   public static final String   EHDR_LAST_MOD = "Last-Modified";

   /**
    * The default buffer size used, never changed.
    */
   protected static final int DEFAULT_BUF_SIZE = 2048;

   /**
    * A static map containing all general headers. The key is the lower cased
    * header value, while the value is the proper cased header value. 
    * Initialized once, never changed after that.
    */
   protected static final HashMap ms_GeneralHeaders;
   
   /**
    * A static map containing all response headers. The key is the lower cased
    * header value, while the value is the proper cased header value. 
    * Initialized once, never changed after that.
    */
   protected static final HashMap ms_ResponseHeaders;

   /**
    * A static map containing all entity headers. The key is the lower cased
    * header value, while the value is the proper cased header value. 
    * Initialized once, never changed after that.
    */
   protected static final HashMap ms_EntityHeaders;

   /**
    * Initialize static members.
    */
   static 
   {
      try
      {
         HTTP_OK_STATUS_LINE   = 
            (new String(HTTP_VERSION + " 200 OK")).getBytes("US-ASCII");
         CR_LF = (new String("\r\n")).getBytes("US-ASCII");
         HEADER_VALUE_BREAK   = (new String(": ")).getBytes("US-ASCII");
      }
      catch (UnsupportedEncodingException e)
      {
         // should never happen because Java supports US-ASCII
         PSConsole.printMsg("Server", e.getLocalizedMessage());
      }

      // init general header map
      ms_GeneralHeaders = new HashMap();
      ms_GeneralHeaders.put(GHDR_CACHE_CONTROL.toLowerCase(), GHDR_CACHE_CONTROL);
      ms_GeneralHeaders.put(GHDR_CONNECTION.toLowerCase(), GHDR_CONNECTION);
      ms_GeneralHeaders.put(GHDR_DATE.toLowerCase(), GHDR_DATE);
      ms_GeneralHeaders.put(GHDR_PRAGMA.toLowerCase(), GHDR_PRAGMA);
      ms_GeneralHeaders.put(GHDR_TRANSFER_ENC.toLowerCase(), GHDR_TRANSFER_ENC);
      ms_GeneralHeaders.put(GHDR_UPGRADE.toLowerCase(), GHDR_UPGRADE);
      ms_GeneralHeaders.put(GHDR_VIA.toLowerCase(), GHDR_VIA);

      // init response header map
      ms_ResponseHeaders = new HashMap();
      ms_ResponseHeaders.put(RHDR_AGE.toLowerCase(), RHDR_AGE);
      ms_ResponseHeaders.put(RHDR_LOCATION.toLowerCase(), RHDR_LOCATION);
      ms_ResponseHeaders.put(RHDR_PROXY_AUTH.toLowerCase(), RHDR_PROXY_AUTH);
      ms_ResponseHeaders.put(RHDR_PUBLIC.toLowerCase(), RHDR_PUBLIC);
      ms_ResponseHeaders.put(RHDR_RETRY_AFTER.toLowerCase(), RHDR_RETRY_AFTER);
      ms_ResponseHeaders.put(RHDR_SERVER.toLowerCase(), RHDR_SERVER);
      ms_ResponseHeaders.put(RHDR_VARY.toLowerCase(), RHDR_VARY);
      ms_ResponseHeaders.put(RHDR_WARNING.toLowerCase(), RHDR_WARNING);
      ms_ResponseHeaders.put(RHDR_SET_COOKIE.toLowerCase(), RHDR_SET_COOKIE);
      ms_ResponseHeaders.put(RHDR_WWW_AUTH.toLowerCase(), RHDR_WWW_AUTH);

      // init entity header map
      ms_EntityHeaders = new HashMap();
      ms_EntityHeaders.put(EHDR_ALLOW.toLowerCase(), EHDR_ALLOW);
      ms_EntityHeaders.put(EHDR_CONT_BASE.toLowerCase(), EHDR_CONT_BASE);
      ms_EntityHeaders.put(EHDR_CONT_ENC.toLowerCase(), EHDR_CONT_ENC);
      ms_EntityHeaders.put(EHDR_CONT_LANG.toLowerCase(), EHDR_CONT_LANG);
      ms_EntityHeaders.put(EHDR_CONT_LENGTH.toLowerCase(), EHDR_CONT_LENGTH);
      ms_EntityHeaders.put(EHDR_CONT_LOC.toLowerCase(), EHDR_CONT_LOC);
      ms_EntityHeaders.put(EHDR_CONT_MD5.toLowerCase(), EHDR_CONT_MD5);
      ms_EntityHeaders.put(EHDR_CONT_RANGE.toLowerCase(), EHDR_CONT_RANGE);
      ms_EntityHeaders.put(EHDR_CONT_TYPE.toLowerCase(), EHDR_CONT_TYPE);
      ms_EntityHeaders.put(EHDR_ETAG.toLowerCase(), EHDR_ETAG);
      ms_EntityHeaders.put(EHDR_EXPIRES.toLowerCase(), EHDR_EXPIRES);
      ms_EntityHeaders.put(EHDR_LAST_MOD.toLowerCase(), EHDR_LAST_MOD);
      ms_EntityHeaders.put(EHDR_CONT_TRF_ENC.toLowerCase(), EHDR_CONT_TRF_ENC);
   }
}

