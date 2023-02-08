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

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.tools.ant.taskdefs.condition.Condition;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

/**
 * PSTableViewExistsWizCondition is a condition which will return
 * <code>true</code> when <code>eval</code> is invoked if the specified table or
 * view already exists in the database, else returns <code>false</code>.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the typedef:
 *
 *  <code>
 *  &lt;typedef name="tableViewExistsWizCondition"
 *              class="com.percussion.ant.install.PSTableViewExistsWizCondition"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to determine if the table or view exists.
 *
 *  <code>
 *  &lt;condition property="TABLE_EXISTS"&gt;
 *     &lt;tableViewExistsWizCondition isView="false" objectName="TABLE"/&gt;
 *  &lt;/condition&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSTableViewExistsWizCondition extends PSAction implements Condition
{
   /* (non-Javadoc)
    * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
    */
   public boolean eval()
   {
      return checkExists();
   }

   /**************************************************************************
    * private functions
    **************************************************************************/

   /**
    * Checks if the database object specified by <code>objectName</code>
    * already exists in the database.
    *
    * @return <code>true</code> if the database object specified by
    * <code>objectName</code> already exists in the database,
    * <code>false</code> otherwise.
    */
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   private boolean checkExists()
   {

      boolean exists = false;

      try
      {
         // get the Rhythmyx root directory
         String strInstallDir = getRootDir();

         if (strInstallDir == null)
            return false;

         if (!strInstallDir.endsWith(File.separator))
            strInstallDir += File.separator;

         // check if the "rxrepository.properties" file exists under the Rhythmyx
         // root directory
         File propFile = new File(strInstallDir +
         "rxconfig/Installer/rxrepository.properties");

         if (!(propFile.exists() && propFile.isFile()))
            return false;

         try(FileInputStream in = new FileInputStream(propFile)) {
            Properties props = new Properties();
            props.load(in);
            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
                    dbmsDef.getBackEndDB(),
                    dbmsDef.getDriver(), null);

            String pw = dbmsDef.getPassword();
            String driver = dbmsDef.getDriver();
            String server = dbmsDef.getServer();
            String database = dbmsDef.getDataBase();
            String uid = dbmsDef.getUserId();
            PSLogger.logInfo("Driver : " + driver + " Server : " + server + " Database : " + database + " uid : " + uid);
            try(Connection conn = InstallUtil.createConnection(driver,
                    server,
                    database,
                    uid,
                    pw)) {

               PSJdbcTableSchema objectSchema = PSJdbcTableFactory.catalogTable(
                       conn, dbmsDef, dataTypeMap, objectName, false);

               exists = (objectSchema != null);
               if (exists) {
                  // check if the object type matches
                  if (isView && !objectSchema.isView())
                     exists = false;
                  else if (!isView && objectSchema.isView())
                     exists = false;
               }
            }
         }
      }
      catch (Exception ex)
      {
         PSLogger.logInfo("ERROR : " + ex.getMessage());
         PSLogger.logInfo(ex);
      }

      return exists;
   }

   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/

   /**
    * Name of the table or view whose existence in the database is to be verified.
    *
    * @return the name of the database object whose existence is to be verified,
    * never <code>null</code> or empty
    */
   public String getObjectName()
   {
      return objectName;
   }

   /**
    * Sets the name of the table or view whose existence in the database is to
    * be verified.
    *
    * @param aObjectName the name of the database object whose existence is to
    * be verified, may not be <code>null</code> or empty
    *
    * @throws IllegalArgumentException if <code>aObjectName</code> is
    * <code>null</code> or empty
    */
   public void setObjectName(String aObjectName)
   {
      if ((aObjectName == null) || (aObjectName.trim().length() < 1))
         throw new IllegalArgumentException(
         "aObjectName may not be null or empty");
      objectName = aObjectName.trim();
   }

   /**
    * Returns whether the database object whose existence is to be verified is
    * a view.
    *
    * @return <code>true</code> if the object specified by <code>objectName</code>
    * is a view, <code>false</code> otherwise
    */
   public boolean getIsView()
   {
      return isView;
   }

   /**
    * Sets whether the database object whose existence is to be verified is
    * a view.
    *
    * @param aIsView <code>true</code> if the object specified by
    * <code>objectName</code> is a view, <code>false</code> otherwise
    */
   public void setIsView(boolean aIsView)
   {
      isView = aIsView;
   }

   /**************************************************************************
    * Bean properties
    **************************************************************************/

   /**
    * Name of the table or view whose existence in the database is to be verified,
    * may not be <code>null</code> or empty
    */
   private String objectName = "RXRELATEDCONTENT";

   /**
    * <code>true</code> if the object specified by <code>objectName</code> is
    * a view, <code>false</code> otherwise
    */
   private boolean isView = false;

}


