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

package com.percussion.data.jdbc;

import com.percussion.data.PSResultSet;
import com.percussion.data.PSResultSetColumnMetaData;
import com.percussion.data.PSResultSetMetaData;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.util.PSOsTool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * The PSOdbcDriverMetaData class implements driver level catalog support
 * for ODBC. In particular, the names of servers (DSNs) can be cataloged.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSOdbcDriverMetaData implements IPSDriverMetaData {

   /**
    * Construnct an ODBC driver meta data object.
    */
   public PSOdbcDriverMetaData()
   {}


   /* ************ IPSDriverMetaData Interface Implementation ************ */

   /**
    * Get the server names (DSNs) available through this driver.
    * <p>
    * The result set contains:
    * <OL>
    * <LI><B>SERVER_NAME</B> String => server name</LI>
    * </OL>
    *
    * @return      a result set containing one server per row
    *
    * @exception  SQLException   if an error occurs accessing the servers
    */
   public java.sql.ResultSet getServers()
      throws SQLException
   {

      HashMap cols = new HashMap(1);
      cols.put("SERVER_NAME", new Integer(1));

      /* the native method returns an array of strings with each string
       * representing one DSN
       */
      ArrayList[] data = { new ArrayList() };
      String[] servers = null;
      if (ms_libraryLoaded)
         servers = getDSNArray();
      else if (ms_useFileSystemOdbcIni)
      {
         if (m_dsnReader == null)
            m_dsnReader = new PSDsnReader(PSServer.getOdbcIniFile());
         servers = m_dsnReader.getDsnList();

         if (servers == null)
            servers = new String[] {};
      }
      else
         servers = new String[] {};

      if(servers != null)
      {
         for (int i = 0; i < servers.length; i++)
            data[0].add(servers[i]);
      }

      return new PSResultSet(data, cols, ms_GetServerRSMeta);
   }


   /**
    * Create a server (DSNs).
    *
    * @param description The description of the server (DSN) being created.
    *
    * @param serverAttrivutes The ini settings needed in the registry for
    *                       this server (DSN) to work.
    * @returns true if successful
    */
   public int createServer(String instDriver, String description, Properties serverAttributes)
   {
      //take the attributes and construct the attributes in a format the
      //SQLConfig understands
      //we will separate key=value with /t and end the attribute string with /t
      //this is done because our native code will replace the /t with /0 which is
      //what SQLConfig wants
      if (!ms_libraryLoaded)
      {
         return(0);  /* This is not supported */
      }

      Enumeration keys = serverAttributes.propertyNames();
      if(keys != null)
      {
         String strAttributes = new String("");
         while(keys.hasMoreElements())
         {
            String strKey = keys.nextElement().toString();
            String strValue = serverAttributes.getProperty(strKey);
            if(strValue != null)
               strAttributes += strKey + "=" + strValue + "\t";
         }

         if(strAttributes.length() > 0)
         {
            return(createDSN(instDriver, description, strAttributes));
         }
      }

      return(1);
   }

   /**
    * Remove a user server (DSNs).
    *
    * @param description The description of the server (DSN) being created.
    *
    * @param serverAttrivutes The ini settings needed in the registry for
    *                       this server (DSN) to work.
    * @returns true if successful
    */
   public int removeUserServer(String instDriver, String description, Properties serverAttributes)
   {
      if (!ms_libraryLoaded)
      {
         return(0);  /* This is not supported */
      }

      //take the attributes and construct the attributes in a format the
      //SQLConfig understands
      //we will separate key=value with /t and end the attribute string with /t
      //this is done because our native code will replace the /t with /0 which is
      //what SQLConfig wants
      Enumeration keys = serverAttributes.propertyNames();
      if(keys != null)
      {
         String strAttributes = new String("");
         while(keys.hasMoreElements())
         {
            String strKey = keys.nextElement().toString();
            String strValue = serverAttributes.getProperty(strKey);
            if(strValue != null)
               strAttributes += strKey + "=" + strValue + "\t";
         }

         if(strAttributes.length() > 0)
         {
            return(removeUserDSN(instDriver, description, strAttributes));
         }
      }

      return(1);
   }

   /**
    * Remove a system server (DSNs).
    *
    * @param description The description of the server (DSN) being created.
    *
    * @param serverAttrivutes The ini settings needed in the registry for
    *                       this server (DSN) to work.
    * @returns true if successful
    */
   public int removeServer(String instDriver, String description, Properties serverAttributes)
   {
      if (!ms_libraryLoaded)
      {
         return(0);  /* This is not supported */
      }

      //take the attributes and construct the attributes in a format the
      //SQLConfig understands
      //we will separate key=value with /t and end the attribute string with /t
      //this is done because our native code will replace the /t with /0 which is
      //what SQLConfig wants
      Enumeration keys = serverAttributes.propertyNames();
      if(keys != null)
      {
         String strAttributes = new String("");
         while(keys.hasMoreElements())
         {
            String strKey = keys.nextElement().toString();
            String strValue = serverAttributes.getProperty(strKey);
            if(strValue != null)
               strAttributes += strKey + "=" + strValue + "\t";
         }

         if(strAttributes.length() > 0)
         {
            return(removeDSN(instDriver, description, strAttributes));
         }
      }

      return(1);
   }

   protected native String[] getDSNArray()
      throws SQLException;

   private native int createDSN(String instDriver, String description, String serverAttributes);
   private native int removeUserDSN(String instDriver, String description, String serverAttributes);
   private native int removeDSN(String instDriver, String description, String serverAttributes);

   /** Do we need to use an odbc native call. */
   static private boolean ms_libraryLoaded = false;

   /** Do we need to use an odbc ini file. */
   static private boolean ms_useFileSystemOdbcIni = false;

   private static final PSResultSetMetaData ms_GetServerRSMeta;

   /** The odbc ini file reader (for unix systems). */
   private PSDsnReader m_dsnReader = null;

   static {
      PSResultSetColumnMetaData col;

      // build the getSever ResultSetMetaData object
      ms_GetServerRSMeta = new PSResultSetMetaData();
      col = new PSResultSetColumnMetaData(
         "SERVER_NAME", java.sql.Types.VARCHAR, 255);
      ms_GetServerRSMeta.addColumnMetaData(col);

      try
      {
         /* Check whether we are on a Unix system before trying to load lib,
            we don't want to show failure messages on Unix when we shouldn't
            even be attempting to load the library. */
         if (PSOsTool.isUnixPlatform())
         {
            ms_useFileSystemOdbcIni = true;
         }
         else
         {
            ms_libraryLoaded = true;
         }
      }
      catch (java.lang.UnsatisfiedLinkError e)
      {
         PSConsole.printMsg("Data", "Cannot load ODBC library: " + e.getMessage());
      }
   }
}
