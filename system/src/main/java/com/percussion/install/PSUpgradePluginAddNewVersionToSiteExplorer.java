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
package com.percussion.install;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSQLStatement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Initial release of Site Explorer did not have New Version menu item. The aim
 * of this plugin is to look for the New Version menu item relation for site
 * explorer and if does not exists add it.
 */
public class PSUpgradePluginAddNewVersionToSiteExplorer implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginAddNewVersionToSiteExplorer()
   {
   }

   /**
    * Implements process method of IPSUpgradePlugin.
    * @param config IPSUpgradeModule object.
    *    may not be <code>null<code>.
    * @param elemData data element of plugin.
    * @return <code>null</code>
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println(
         "Running New Version relation plugin");
      Statement stmt = null;
      ResultSet rs = null;
      Connection conn = null;
      try
      {
            Properties repprops = new Properties();
            repprops.load(new FileInputStream(new File(RxUpgrade.getRxRoot() +
               "rxconfig/Installer/rxrepository.properties")));
            repprops.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            conn = RxLogTables.createConnection(repprops);
            stmt = PSSQLStatement.getStatement(conn);
            rs = stmt.executeQuery("SELECT * FROM RXMODEUICONTEXTACTION " +
               "WHERE MODEID=1 AND UICONTEXTID=1 AND ACTIONID=109");
            if(null != rs && !rs.next())
            {
               stmt.executeUpdate("INSERT INTO RXMODEUICONTEXTACTION " +
                    "VALUES(1,1,109)");
            }
            rs = stmt.executeQuery("SELECT * FROM RXMODEUICONTEXTACTION " +
               "WHERE MODEID=1 AND UICONTEXTID=3 AND ACTIONID=109");
            if(null != rs && !rs.next())
            {
               stmt.executeUpdate("INSERT INTO RXMODEUICONTEXTACTION " +
                    "VALUES(1,3,109)");
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
