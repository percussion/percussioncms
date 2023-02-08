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

package com.percussion.ant.install;

import com.percussion.install.RxFileManager;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.util.PSSqlHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.tools.ant.taskdefs.condition.Condition;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

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
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
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



