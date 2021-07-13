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

package com.percussion.ant.install;

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.utils.io.PathUtils;
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
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
                    props.getProperty("DB_BACKEND"),
                    props.getProperty("DB_DRIVER_NAME"), null);

            String pw = props.getProperty("PWD");
            try {
               pw = PSEncryptor.getInstance("AES",
                       PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).decrypt(pw);
            } catch (PSEncryptionException | java.lang.IllegalArgumentException e) {
               pw = PSLegacyEncrypter.getInstance(
                       PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
               ).decrypt(pw,
                       PSJdbcDbmsDef.getPartOneKey(), null);
            }
            try(Connection conn = InstallUtil.createConnection(props.getProperty("DB_DRIVER_NAME"),
                    props.getProperty("DB_SERVER"),
                    props.getProperty("DB_NAME"),
                    props.getProperty("UID"),
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


