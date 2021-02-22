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

package com.percussion.ant.install;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.tools.ant.taskdefs.condition.Condition;

import com.percussion.install.RxFileManager;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;

/**
 * If FastForward tables have been installed with data, then this condition
 * returns <code>true</code>.
 * 
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the typedef:
 * 
 *  <code>  
 *  &lt;typedef name="ffTableDataInstallProdCondition"
 *              class="com.percussion.ant.install.PSFFTableDataInstallProdCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 * 
 * Now use the task to determine if an eval is installed.
 * 
 *  <code>
 *  &lt;condition property="FF_TABLE_DATA_INSTALLED"&gt;
 *     &lt;ffTableDataInstallProdCondition/&gt;
 *  &lt;/condition&gt;
 *  </code>
 * 
 * </pre>
 * 
 */
public class PSFFTableDataInstallProdCondition extends PSAction
implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      boolean isFFTableDataInstall = false;
    try
      {
         String strRootDir = getRootDir();
         
         RxFileManager rxfm = new RxFileManager(strRootDir);
         File propFile = new File(rxfm.getRepositoryFile());
         if (propFile.exists())
         {
            try(FileInputStream in = new FileInputStream(propFile)) {
               Properties props = new Properties();
               props.load(in);

               props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
               try(Connection conn = RxLogTables.createConnection(props)) {

                  if (conn != null) {
                     String db = props.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY);
                     String schema = props.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY);

                     if (db.trim().length() < 1)
                        db = null;

                     // First check to see if FF table exists
                     DatabaseMetaData dbmd = conn.getMetaData();
                     ResultSet rs = dbmd.getTables(db, schema, FF_TABLE,
                             new String[]{"TABLE"});

                     if ((rs != null) && (rs.next())) {
                        // Now check if FastForward data has been installed
                        String qualTableName = PSSqlHelper.qualifyTableName(FF_TABLE,
                                db, schema, props.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY));

                        String queryStmt = "SELECT CONTENTID FROM " + qualTableName;

                        try (PreparedStatement stmt = conn.prepareStatement(queryStmt)) {

                           rs = stmt.executeQuery();

                           if ((rs != null) && (rs.next()))
                              isFFTableDataInstall = true;
                        }
                     }
                  }
               }
            }
            
         }
      }
      catch (Exception e)
      {
         log(e.getMessage());
      }
      return isFFTableDataInstall;
   }
   
   private static final String FF_TABLE = "RXS_CT_SHARED";
   
}



