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

package com.percussion.data.jdbc;

import com.percussion.data.vfs.IPSVirtualDirectory;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.utils.request.PSRequestInfoBase;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;

/**
 * The PSFileSystemDriver class implements the File System driver.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSFileSystemDriver extends PSJdbcDriver
{
   public PSFileSystemDriver()
   {
      super(ms_driverName, ms_majorVersion, ms_minorVersion);
   }

   protected PSFileSystemDriver(String displayName, int majorVer, int minorVer)
   {
      super(displayName, majorVer, minorVer);
   }

   /**
    * Connect to the file or directory specified by the url.
    * <p>
    * The URL syntax supported by this driver is:
    * <pre><code>
    *      jdbc:psfilesystem:
    * </code></pre>
    *
    * @param      url      the URL of the file or directory to open
    * @param      info     has a catalog property set to the directory
    * and a sessionId property for session-related security.
    * @exception  SQLException   if this is an appropriate URL for this
    *                            driver, but the file or directory cannot
    *                            be accessed
    */
   public java.sql.Connection connect( java.lang.String url,
      java.util.Properties info)
      throws SQLException
   {
      try
      {
         // if this is the wrong type of URL, return null
         if (!url.toLowerCase().startsWith("jdbc:psfilesystem"))
            return null;
      }
      catch (Exception e)
      {
         throw new SQLException(e.toString());
      }
      PSRequest currentReq = (PSRequest) PSRequestInfoBase
            .getRequestInfo(PSRequestInfoBase.KEY_PSREQUEST);
      return new PSFileSystemConnection(
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
    *      jdbc:psfilesystem:directory-path
    * </code></pre>
    *
    * @param      url            the URL to validate
    * @return                    <code>true</code> if the URL is supported,
    *                            <code>false</code> otherwise
    * @exception  SQLException   if an error occurs
    */
   public boolean acceptsURL(java.lang.String url)
      throws SQLException
   {
      return url.toLowerCase().startsWith("jdbc:psfilesystem");
   }

   /**
    * This is not currently supported and always returns an empty array.
    *
    * @param      url      the URL being used
    * @param      info     the info constructed so far
    * @return     always returns an empty array
    * @exception  SQLException   if an error occurs
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
    * @return     always returns <code>false</code> as this driver does not
    *             currently support all JDBC features
    */
   public boolean jdbcCompliant()
   {
      return false;
   }

   /**
    * Adds a virtual directory entry.
    *
    * @author   chadloder
    *
    *
    * @param   vdir
    *
    */
   public static void addVirtualDirectory(IPSVirtualDirectory vdir)
   {
      printMsg("Adding virtual directory: "
         + vdir.getVirtualDirectory() + " -> " + vdir.getPhysicalPath(new File(".")).toString());

      // add this virtual directory to the hash. the key is the "directory"
      // the vdir answers for
      m_vdirs.put(vdir.getVirtualDirectory(), vdir);
   }

   /**
    * Removes the virtual directory entry that answers for <CODE>vdir</CODE>. 
    * 
    * @param vdir The directory to remove, may be <code>null</code> or empty
    * in which case <code>null</code> is returned. 
    *  
    * @return The removed directory, or <code>null</code> if no matching 
    * directory was located for removal.
    */
   public static IPSVirtualDirectory removeVirtualDirectory(String vdir)
   {
      printMsg("Removing virtual directory: " + vdir);
      return m_vdirs.remove(vdir);
   }

   /**
    * Atomically deletes the virtual directory that answers for
    * <CODE>oldName</CODE> and adds the new virtual directory.
    *
    * @author   chadloder
    *
    * @since 1.5 1999/07/13
    *
    *
    * @param   oldVdir
    * @param   newVdir
    *
    */
   public static void renameVirtualDirectory(
      String oldVdir,
      IPSVirtualDirectory newVdir
      )
   {
      synchronized (m_vdirs)
      {
         removeVirtualDirectory(oldVdir);
         addVirtualDirectory(newVdir);
      }
   }

   /**
    * For use by the different compononents of the XML / file system driver.
    * Given a catalog, gives the physical path corresponding to that
    * catalog. Every bit in the requiredAccess field must be a right
    * granted to the given session, or else a PSAuthorizationException is
    * thrown.
    *
    * @author   chadloder
    *
    * @since 1.5 1999/07/13
    *
    * @param   catalog
    *
    * @param   session
    *
    * @param   requiredAccess
    *
    * @return   File
    */
   static File getPhysicalPath(String catalog,
                                     PSUserSession session,
                                     int requiredAccess)
      throws PSAuthorizationException
   {
      if (catalog == null)
         throw new IllegalArgumentException("vfs convert path error" + catalog);

      IPSVirtualDirectory vdir;

      // get the highest root of the catalog, and use this to get the
      // virtual directory entry
      File catalogFile = new File(catalog);
      File root = catalogFile.getParentFile();

      while (root != null)
      {
         catalogFile = root;
         root = catalogFile.getParentFile();
      }

      String rootName = catalogFile.getName();

      vdir = m_vdirs.get(rootName);

      if (vdir == null)
      {
         // we did not find any virtual directories that answer to this name
         throw new IllegalArgumentException("vfs convert path error" + catalog);
      }

      // see if we have the required permissions
      if (!vdir.hasPermissions(session, requiredAccess))
      {
         String sessionId = "";

         if (session != null)
             sessionId = session.getId();

         throw new PSAuthorizationException("catalog", catalog, sessionId);
      }

      //
      //get relative path
      //

      String relPath = ".";

      //normalize slash
      String normalPath = catalog.replace('\\','/');

      int slashInd = normalPath.indexOf('/');

      if (slashInd >= 0)
      {
        relPath = normalPath.substring(rootName.length() + 1);
        //localize slash
        relPath = relPath.replace('/', File.separatorChar);
      }

      return vdir.getPhysicalPath(new File(relPath));
   }

   private static void printMsg(String msg)
   {
      com.percussion.server.PSConsole.printMsg("FileSysDriver", msg);
   }


   /** the map from catalog names to virtual directory entries */
   private static final Map<String,IPSVirtualDirectory> m_vdirs
      = Collections.synchronizedMap(new HashMap<>());

   private static final int               ms_majorVersion = 1;
   private static final int               ms_minorVersion = 0;
   private static final java.lang.String  ms_driverName   =
      "Percussion Software File System Driver";

   static
   {
      try
      {
         java.sql.DriverManager.registerDriver(
            new PSFileSystemDriver(
               ms_driverName,
               ms_majorVersion,
               ms_minorVersion    )
               );
      }
      catch (SQLException e)
      {
         String arg = "ERROR: PSFileSystemDriver cannot initialize.";
         com.percussion.server.PSConsole.printMsg(arg, e);
      }
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new UnsupportedOperationException("This method is not yet implemented");
   }
}
