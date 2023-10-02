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

import com.percussion.error.PSExceptionUtils;
import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.install.RxInstallerProperties;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableDataCollection;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.util.PSProperties;
import com.percussion.xml.PSXmlDocumentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Iterator;


/**
 * PSTableAction is a task which installs table definitions and data.
 *
 * The tables name property is used to point to the xml documents that store
 * the table and data definitions.  [TableName]Def.xml defines the table
 * definition and [TableName]Data.xml defines the data.  These files must be
 * in the resources via an RxISCustomFiles object.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="tableAction"
 *              class="com.percussion.ant.install.PSTableAction"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to install the repository.
 *
 *  <code>
 *  &lt;tableAction
 *      repositoryLocation="rxconfig/Installer/rxrepository.properties"
 *      tableData="cmsTableData.xml"
 *      tableDef="cmsTableDef.xml"
 *      tableFactoryLogFile="tableFactory.log"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSTableAction extends PSAction
{
   // see base class
   @SuppressFBWarnings("HARD_CODE_PASSWORD")
   @Override
   public void execute()
   {
      PSLogger.logInfo("Installing tables...");
      isRepositoryError = false;
      String newLine = System.getProperty("line.separator");
      repositoryErrorMsg =
         RxInstallerProperties.getResources().getString("repositoryConfError");
      repositoryErrorMsg += newLine;
      repositoryErrorMsg += newLine;
      PrintStream ps = System.out;

      try
      {
         PSJdbcTableSchemaCollection schemaColl = null;
         PSJdbcTableDataCollection dataColl = null;
         PSJdbcTableSchema schema = null;
         PSJdbcTableData data = null;
         PSJdbcDataTypeMap dataTypeMap = null;

         //log the tablefactory output to tablefactory.log
         String strLogFile = getTableFactoryLogFile();
         PSLogger.logInfo("tablefactory log file : " + strLogFile);
         ps = new PrintStream(new FileOutputStream(strLogFile, true));

         PSProperties props = new PSProperties(getRepositoryLocation());

         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);


         dataTypeMap = new PSJdbcDataTypeMap(dbmsDef.getBackEndDB(),
                 dbmsDef.getDriver(), null);
         InstallUtil.setRootDir(getRootDir());
         String pw = dbmsDef.getPassword();
         String driver = dbmsDef.getDriver();
         String server = dbmsDef.getServer();
         String database = dbmsDef.getDataBase();
         String uid = dbmsDef.getUserId();
         PSLogger.logInfo("Driver : " + driver + " Server : " + server + " Database : " + database + " uid : " + uid);

         //get table def files
         String[] tableDef = getTableDef();
         for (int i = 0; i < tableDef.length; i++)
         {
            String filePath = tableDef[i];

            Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  new FileInputStream(new File(filePath)),
                  false);

            if (schemaColl==null)
               schemaColl = new PSJdbcTableSchemaCollection(doc,
                       dataTypeMap);
            else
               schemaColl.addAll(new PSJdbcTableSchemaCollection(doc,
                       dataTypeMap));

         }
         try(Connection conn = InstallUtil.createConnection(driver,
                 server,
                 database,
                 uid,
                 pw)) {
            //get table data files
            String[] tableData = getTableData();
            for (int i = 0; i < tableData.length; i++) {
               String filePath = tableData[i];
               File f = new File(filePath);

               //set system property so that table factory can find external
               //resources if any.
               String fName = f.getName();
               //get table factory file name with no extension
               //ie: {cmstableData.external.root}
               //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
               fName = fName.substring(0, fName.length() - 4);
               System.setProperty("{" + fName + ".external.root}", f.getParent());

               Document doc = PSXmlDocumentBuilder.createXmlDocument(
                       new FileInputStream(f),
                       false);

               if (dataColl == null)
                  dataColl = new PSJdbcTableDataCollection(doc);
               else
                  dataColl.addAll(new PSJdbcTableDataCollection(doc));
               }

            Iterator it = schemaColl.iterator();


            int index = 0;

            while (it.hasNext()) {
               schema = (PSJdbcTableSchema) it.next();
               String tblName = schema.getName();
               data = dataColl.getTableData(tblName);
               // getTableData may return null if this table has no data associated
               // with it in cmdTableData.xml file. For such cases, construct
               // an empty table data object
               if (data == null)
                  data = new PSJdbcTableData(tblName, null);
               schema.setTableData(data);

               try {
                  PSLogger.logInfo("Table : " + tblName);
                  PSJdbcTableFactory.processTable(conn, dbmsDef, schema,
                          ps, true);
               } catch (Exception ex) {
                  PSLogger.logWarn(ex.getMessage());
               }
               index++;
            }

            PSLogger.logInfo("Table installation complete...");
         }
      }
      catch(Exception e)
      {
         PSLogger.logError(PSExceptionUtils.getMessageForLog(e));
         isRepositoryError = true;
         throw new BuildException(e.getMessage());
      }
   }

   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/

   /**
    * Accessor for the repository location
    */
   public String getRepositoryLocation()
   {
      return m_strRepositoryLocation;
   }

   /**
    * Mutator for the repository location.
    */
   public void setRepositoryLocation(String strRepositoryLocation)
   {
      m_strRepositoryLocation = strRepositoryLocation;
   }

   /**
    * returns the tablefactory log file path relative to the install directory
    * @return the tablefactory log file path relative to the install directory,
    * never <code>null</code> or empty
    */
   public String getTableFactoryLogFile()
   {
      return m_tableFactoryLogFile;
   }

   /**
    * Sets the tablefactory log file path relative to the install directory
    * @param tableFactoryLogFile the tablefactory log file path relative to
    * the install directory, may be <code>null</code> or empty in which the
    * default is used.
    */
   public void setTableFactoryLogFile(String tableFactoryLogFile)
   {
      m_tableFactoryLogFile = tableFactoryLogFile;
   }

   /**
    * Returns <code>true</code> if any error occurs during repository creation,
    * <code>false</code> otherwise.
    * @return <code>true</code> if any error occurs during repository creation,
    * <code>false</code> otherwise.
    */
   public boolean getIsRepositoryError()
   {
      return isRepositoryError;
   }

   /**
    * Returns the detailed error message if any error occurred during
    * repository creation. Returns empty string if repository creation
    * was successful.
    * @return the detailed error message if any error occurred during
    * repository creation. Returns empty string if repository creation
    * was successful.
    */
   public String getRepositoryErrorMsg()
   {
      if (isRepositoryError)
         return repositoryErrorMsg;
      return "";
   }

   /**
    * @return
    */
   public String[] getTableData()
   {
      return m_strTableData;
   }

   /**
    * @return
    */
   public String[] getTableDef()
   {
      return m_strTableDef;
   }

   /**
    * @param strings
    */
   public void setTableData(String strings)
   {
      m_strTableData = convertToArray(strings);
   }

   /**
    * @param strings
    */
   public void setTableDef(String strings)
   {
      m_strTableDef = convertToArray(strings);
   }

   /**************************************************************************
    * private function
    **************************************************************************/


   /**************************************************************************
    * Static Strings
    *************************************************************************/

   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    *  The Tables name for the table definitions.
    */
   private String[] m_strTableDef = {"$P(absoluteInstallLocation)/rxconfig"};

   /**
    *  The Types name for the table definitions.
    */
   private String[] m_strTableData = {"$P(absoluteInstallLocation)/rxconfig"};

   /**
    * The repository location.
    */
   private String m_strRepositoryLocation = getRootDir() + File.separator
   + "rxconfig/Installer/rxrepository.properties";

   /**
    * log for the tablefactory processing
    */
   private String m_tableFactoryLogFile = getRootDir() + File.separator
   + "rxconfig" + File.separator + "Installer" + File.separator  +"tableFactory.log";


   /**
    * initially <code>false</code>, <code>true</code> if any error occurs
    * during repository creation
    */
   private boolean isRepositoryError = false;

   /**
    * detailed error message about the error occuring during repository creation
    */
   private String repositoryErrorMsg = "";




}

