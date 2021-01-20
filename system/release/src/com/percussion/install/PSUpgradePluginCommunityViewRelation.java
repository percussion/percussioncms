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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSQLStatement;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.w3c.dom.Element;

/**
 * While upgrading the community views related data from RXSYSCOMPONENTRELATIONS
 * will be added through general tables plugin except for the relation between
 * content administrator left nav and community content component.
 * If communities are enabled on the system this plugin adds the relation if
 * the relation does not exist already.
 */
public class PSUpgradePluginCommunityViewRelation implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginCommunityViewRelation()
   {
   }

   /**
    * Implements process method of IPSUpgradePlugin.
    * @param config IPSUpgradeModule object.
    *    may not be <code>null<code>.
    * @param elemData data element of plugin.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println(
         "Running community view relation plugin");
      Statement stmt = null;
      ResultSet rs = null;
      Connection conn = null;
      try
      {
         Properties servprops = new Properties();
         servprops.load(new FileInputStream(new File(RxUpgrade.getRxRoot() +
            "rxconfig/Server/server.properties")));
         String comm = servprops.getProperty("communities_enabled","no");
         if(comm.equalsIgnoreCase("yes"))
         {
            Properties repprops = new Properties();
            repprops.load(new FileInputStream(new File(RxUpgrade.getRxRoot() +
               "rxconfig/Installer/rxrepository.properties")));
            repprops.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            conn = RxLogTables.createConnection(repprops);
            stmt = PSSQLStatement.getStatement(conn);
            rs = stmt.executeQuery("SELECT * FROM RXSYSCOMPONENTRELATIONS " +
               "WHERE COMPONENTID=6 AND CHILDCOMPONENTID=100");
            if(null != rs && !rs.next()){
               stmt.executeUpdate("INSERT INTO RXSYSCOMPONENTRELATIONS " +
                    "VALUES(6,100,'slt_ca_nav',4)");
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
      }
      finally
      {
         if(stmt!=null)
         {
            try
            {
               stmt.close();
            }
            catch(Exception e)
            {
            }
            stmt = null;
         }
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
            conn = null;
         }
         config.getLogStream().println(
            "leaving the process() of the plugin...");
      }
      return null;
   }
}
