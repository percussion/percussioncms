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

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.install.RxInstallerProperties;
import com.percussion.tablefactory.*;
import com.percussion.util.PSProperties;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
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
      Connection conn = null;

      try
      {
         PSJdbcTableSchemaCollection schemaColl = null;
         PSJdbcTableDataCollection dataColl = null;
         PSJdbcTableSchema schema = null;
         PSJdbcTableData data = null;
         PSProperties props = null;
         PSJdbcDbmsDef dbmsDef = null;
         PSJdbcDataTypeMap dataTypeMap = null;

         //log the tablefactory output to tablefactory.log
         String strLogFile = getTableFactoryLogFile();
         PSLogger.logInfo("tablefactory log file : " + strLogFile);
         ps = new PrintStream(new FileOutputStream(strLogFile, true));

         props = new PSProperties(getRepositoryLocation());
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

         dbmsDef = new PSJdbcDbmsDef(props);
         dataTypeMap = new PSJdbcDataTypeMap(
               props.getProperty("DB_BACKEND"),
               props.getProperty("DB_DRIVER_NAME"), null);

         InstallUtil.setRootDir(getRootDir());

         String pw = props.getProperty("PWD");
         try{
            pw = PSEncryptor.getInstance().decrypt(pw);
         }catch(PSEncryptionException e){
            pw = PSLegacyEncrypter.getInstance().decrypt(pw,
                    PSJdbcDbmsDef.getPartOneKey());
         }
         conn = InstallUtil.createConnection(props.getProperty("DB_DRIVER_NAME"),
                 props.getProperty("DB_SERVER"),
                 props.getProperty("DB_NAME"),
                 props.getProperty("UID"),
                 pw);


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

         //get table data files
         String[] tableData = getTableData();
         for (int i = 0; i < tableData.length; i++)
         {
            String filePath = tableData[i];
            File f = new File(filePath);

            //set system property so that table factory can find external
            //resources if any.
            String fName = f.getName();
            //get table factory file name with no extension
            //ie: {cmstableData.external.root}
            //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
            fName = fName.substring(0, fName.length()-4);
            System.setProperty("{" + fName + ".external.root}", f.getParent());

            Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  new FileInputStream(f),
                  false);

            if (dataColl==null)
               dataColl = new PSJdbcTableDataCollection(doc);
            else
               dataColl.addAll(new PSJdbcTableDataCollection(doc));
         }

         Iterator it = schemaColl.iterator();


         int index = 0;

         while (it.hasNext())
         {
            schema = (PSJdbcTableSchema)it.next();
            String tblName = schema.getName();
            data = dataColl.getTableData(tblName);
            // getTableData may return null if this table has no data associated
            // with it in cmdTableData.xml file. For such cases, construct
            // an empty table data object
            if (data == null)
               data = new PSJdbcTableData(tblName, null);
            schema.setTableData(data);

            try
            {
               PSLogger.logInfo("Table : " + tblName);
               PSJdbcTableFactory.processTable(conn, dbmsDef, schema,
                     ps, true);
            }
            catch (Exception ex)
            {
               if ((tblName.equalsIgnoreCase("RXSYSCOMPONENTPROPERTY")) ||
                     (tblName.equalsIgnoreCase("RXLOCATIONSCHEMEPARAMS")) ||
                     (tblName.equalsIgnoreCase("RXEXTERNAL")))
               {
                  // RXSYSCOMPONENTPROPERTY and RXLOCATIONSCHEMEPARAMS have
                  // schema changes where non-nullable
                  // columns have been added. Tablefactory will throw exception
                  // in such cases. Need to ignore the exception for these
                  // two tables. This code should be removed once the
                  // tablefactory has been modified to handle such cases.
                  // RXEXTERNAL will throw exception on Oracle since this table's
                  // ITEMURL column has been changed from LONG to VARCHAR2 (2100)
               }
               else
               {
                  throw new BuildException(ex.toString());
               }
            }
            index++;
         }

         PSLogger.logInfo("Table installation complete...");
      }
      catch(Exception e)
      {
         PSLogger.logError(e.getMessage());
         ps.println(e.toString());
         e.printStackTrace(ps);
         isRepositoryError = true;
         repositoryErrorMsg += e.getMessage();
         throw new BuildException(e.getMessage());
      }
      finally
      {
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

