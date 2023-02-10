/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.data.jdbc;

import com.percussion.server.PSRequest;
import com.percussion.utils.request.PSRequestInfo;

import java.sql.SQLException;

/**
 * The PSXmlDriver class extends the File System driver for XML file support.
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSXmlDriver extends PSFileSystemDriver {

   /**
    * @author   chadloder
    *
    * Constructor
    *
    * @since   1.2 1999/5/7
    *
    */
   public PSXmlDriver()
   {
      super(ms_driverName, ms_majorVersion, ms_minorVersion);
   }

   /**
    * Connect to the XML file or directory specified by the url.
    * <p>
    * The URL syntax supported by this driver is:
    * <pre><code>
    *    jdbc:psxml:xml-file-url
    * </code></pre>
    * At this time, only file based URLs are supported (file://file-path).
    *
    * @param   url       the URL of the XML file or directory to open
    * @param   info
    * @exception   SQLException    if this is an appropriate URL for this
    *                          driver, but the XML file or directory cannot
    *                          be accessed
    */
    public java.sql.Connection connect( java.lang.String url,
      java.util.Properties info)
      throws SQLException
   {
      try
      {
         // if this is the wrong type of URL, return null
         if (!url.toLowerCase().startsWith("jdbc:psxml"))
            return null;
      }
      catch (Exception e)
      {
         throw new SQLException(e.toString());
      }

      PSRequest currentReq = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return new PSXmlConnection(
         info.getProperty("catalog"),
         url,
         (currentReq == null) ? null : currentReq.getUserSessionId(),
         this);
   }

   /**
    * Does the URL match the syntax supported by this driver?
    * <p>
    * The URL syntax supported by this driver is:
    * <pre><code>
    *    jdbc:psxml:xml-file-url
    * </code></pre>
    * At this time, only file based URLs are supported (file://file-path).
    *
    * @param   url             the URL to validate
    * @return   <code>true</code> if the URL is supported,
    *                          <code>false</code> otherwise
    * @exception   SQLException    if an error occurs
    */
   public boolean acceptsURL(java.lang.String url)
      throws SQLException
   {
      return url.toLowerCase().startsWith("jdbc:psxml");
   }

   /**
    * This is not currently supported and always returns an empty array.
    *
    * @param   url       the URL being used
    * @param   info     the info constructed so far
    * @return   always returns an empty array
    * @exception   SQLException    if an error occurs
    */
   public java.sql.DriverPropertyInfo[] getPropertyInfo(
      java.lang.String url,
      java.util.Properties info)
      throws SQLException
   {
      return new java.sql.DriverPropertyInfo[0];
   }

   /**
    * Is this driver a fully JDBC COMPLIANT (tm) driver?
    *
    * @return   always returns <code>false</code> as this driver does not
    *           currently support all JDBC features
    */
   public boolean jdbcCompliant()
   {
      return false;
   }

   private static void printMsg(Throwable t)
   {
      com.percussion.server.PSConsole.printMsg("XmlDriver", t);
   }

   /** version info */
   private static final int ms_majorVersion = 1;
   private static final int ms_minorVersion = 0;
   private static final String  ms_driverName =
      "Percussion Software XML Driver";

   static
   {
      try
      {
         java.sql.DriverManager.registerDriver(
            new PSXmlDriver());
      }
      catch (SQLException e)
      {
         printMsg(e);
      }
   }
}
