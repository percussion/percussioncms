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

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFile;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSValidationException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The PSObjectStoreUtil class acts as a wrapper around the
 * <code>com.percussion.design.objectstore.PSObjectStore</code> class which
 * is obfuscated and hence not available to classes in non-obfuscated jar
 * files. This class creates a designer connection using the properties
 * specified in the ctor. This connection is then used to create a
 * <code>PSObjectStore</code> object and store it as a member variable. All
 * methods in this class then simply deletgate to this internal
 * <code>PSObjectStore</code> object.
 */
public class PSObjectStoreUtil
{
   /**
    * Constructs a designer connection using the connection information in the
    * specified properties file. This designer connection is used to create
    * <code>PSObjectStore</code> object to which all the method calls are
    * delegated.
    *
    * @param designerConnectionInfo used to create designer connection, may
    * not be <code>null</code>. See
    * {@link com.percussion.conn.PSDesignerConnection#PSDesignerConnection(
    * Properties)} for the supported/required keys.
    *
    * @throws PSServerException if the server is not responding
    * @throws PSAuthorizationException if access to the server is denied
    * @throws PSAuthenticationFailedException if userid or password submitted
    * is invalid
    */
   public PSObjectStoreUtil(Properties designerConnectionInfo)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException
   {
      if (designerConnectionInfo == null)
         throw new IllegalArgumentException(
            "designerConnectionInfo may not be null");

      m_conn = new PSDesignerConnection(designerConnectionInfo);
      m_objStore = new PSObjectStore(m_conn);
   }

   /**
    * Save the application file specified by its input stream
    * <code>appFileStream</code> under the application specified by
    * <code>appName</code> in the path specified by <code>appFile</code>.
    *
    * @param appName the name of the application to which the application file
    * belongs, may not be <code>null</code> or empty
    *
    * @param appFileStream input stream of the application file to save, may
    * not be <code>null</code>
    *
    * @param appFile specified the path at which the application file should be
    * saved, may not be <code>null</code>
    *
    * @param   overwriteIfExists <code>true</code> if the file should be
    * overwritten if it exists. If <code>false</code>, if the file exists
    * already, then it will not be overwritten and no error will be reported.
    *
    * @param releaseLock <code>true</code> if the application lock is to be
    * released. Use this when no further changes to the application will
    * be made. If <code>false</code>, the application lock will be extended an
    * additional 30 minutes.
    *
    * @throws PSServerException if the server is not responding
    * @throws PSAuthorizationException If the user does not have update access
    * on the application
    * @throws PSNotLockedException If a lock is not currently held on the
    * application
    * @throws PSValidationException if a validation error is encountered
    * @throws PSLockedException if another user already has the application
    * locked
    * @throws PSNotFoundException if an application by the name
    * <code>appName</code> does not exist
    */
   public void saveApplicationFile(String appName, InputStream appFileStream,
      File appFile, boolean overwriteIfExists, boolean releaseLock)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSNotLockedException,
         PSValidationException, PSLockedException,
         PSNotFoundException
   {
      if ((appName == null) || (appName.trim().length() < 1))
         throw new IllegalArgumentException(
            "appName may not be null or empty");

      if (appFileStream == null)
         throw new IllegalArgumentException(
            "appFileStream may not be null");

      if (appFile == null)
         throw new IllegalArgumentException(
            "appFile may not be null");

      PSApplication app = m_objStore.getApplication(appName, false);
      PSApplicationFile applicationFile = new PSApplicationFile(appFileStream,
         appFile);
      m_objStore.saveApplicationFile(app, applicationFile,
         overwriteIfExists, releaseLock);
   }

   /**
    * Returns the name of the system running the Rhythmyx Server. This method
    * simply delegates to the contained designer connection.
    *
    * @return host name of the system running Rhythmyx Server, never
    * <code>null</code> or empty
    */
   public String getServer()
   {
      return m_conn.getServer();
   }

   /**
    * Returns the version information of the server to which a designer
    * connection was established using the properties specified in the ctor.
    * This method simply delegates to the contained designer connection.
    *
    * @return server version information : major, minor, build number and
    * build date, never <code>null</code>
    */
   public PSFormatVersion getServerVersion()
   {
      return m_conn.getServerVersion();
   }

   /**
    * Closes the contained designer connection.
    *
    * @exception IOException if an i/o error occurs while closing the
    * connection
    */
   public void close() throws IOException
   {
      m_conn.close();
   }

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_LOGIN_ID}
    * for details.
    */
   public static final String PROPERTY_LOGIN_ID =
      PSDesignerConnection.PROPERTY_LOGIN_ID;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_LOGIN_PW}
    * for details.
    */
   public static final String PROPERTY_LOGIN_PW =
      PSDesignerConnection.PROPERTY_LOGIN_PW;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_PORT}
    * for details.
    */
   public static final String PROPERTY_PORT =
      PSDesignerConnection.PROPERTY_PORT;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_PROTOCOL}
    * for details.
    */
   public static final String PROPERTY_PROTOCOL =
      PSDesignerConnection.PROPERTY_PROTOCOL;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_LOCALE}
    * for details.
    */
   public static final String PROPERTY_LOCALE =
      PSDesignerConnection.PROPERTY_LOCALE;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_HOST}
    * for details.
    */
   public static final String PROPERTY_HOST =
      PSDesignerConnection.PROPERTY_HOST;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#DEFAULT_PORT}
    * for details.
    */
   public static final int DEFAULT_PORT =
      PSDesignerConnection.DEFAULT_PORT;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#DEFAULT_SSL_PORT}
    * for details.
    */
   public static final int DEFAULT_SSL_PORT =
      PSDesignerConnection.DEFAULT_SSL_PORT;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#PROPERTY_USE_JAVA_URL}
    * for details.
    */
   public static final String PROPERTY_USE_JAVA_URL =
      PSDesignerConnection.PROPERTY_USE_JAVA_URL;

   /**
    * See {@link com.percussion.conn.PSDesignerConnection#USE_JAVA_URL_ENABLED}
    * for details.
    */
   public static final String USE_JAVA_URL_ENABLED =
      PSDesignerConnection.USE_JAVA_URL_ENABLED;

   /**
    * Designer connection, initialized in the ctor, never <code>null</code>
    * or modified after that
    */
   private PSDesignerConnection m_conn = null;

   /**
    * All methods in this class simply delegate to this object. Initialized
    * in the ctor, never <code>null</code> or modified after that.
    */
   private PSObjectStore m_objStore = null;

}



