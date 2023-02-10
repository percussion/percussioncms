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
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
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
