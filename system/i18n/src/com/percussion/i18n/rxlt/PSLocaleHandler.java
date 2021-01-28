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
package com.percussion.i18n.rxlt;

import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSQLStatement;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.utils.container.PSMissingApplicationPolicyException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class handles the action to add a new language to the Rhythmyx Content
 * Manager. Adding new language simply means to add a new row to RXLOCALE table.
 * However, this is only the first step in adding a new language to Rhythmyx
 * Content Manager as whole. The next step would be to generate the TMX document
 * with that include the stubs for new language. This Document goes to a
 * translator. The translated docment is then merged with that on Rhythmyx server.
 * <p>
 * This class also includes some static utility methods relevant to languages.
 * For repeated use of the static methods, construct and hold an instance of
 * the class to prevent any cached member data from being garbage collected.
 * </p>
 */

public class PSLocaleHandler
   implements IPSActionHandler
{
   /**
    * This is a utility method to get array of all languages registered in the
    * Rhythmyx Content Manager. It makes use of the database settings from the
    * repository properties file which is written by the installer during
    * installation.
    * @param rxroot Rhythmyx root directory, must not be <code>null</code> and
    * may be <code>empty</code>
    * @return array of language strings (like "en-us"), may be <code>empty</code>
    * but not <code>null</code>.
    * @throws FileNotFoundException if the repository file is not be found with
    * given Rhythmyx root directoy
    * @throws IOException in case of error reading repository properties file
    * @throws SAXException in case of error building the result XML document
    * with the list of locales
    * @throws PSJdbcTableFactoryException in case of an error during JDBC table
    * factory processing.
    * @throws SQLException if any JDBC error occurs
    * @throws PSMissingApplicationPolicyException if an error occurs while 
    * getting the repository properties
    * @throws PSInvalidXmlException if an error occurs while getting the
    * repository properties 
    * @see #getLocaleDocument
    */
   public static Object[] getLocaleStrings(String rxroot)
      throws FileNotFoundException, IOException, SAXException,
            PSJdbcTableFactoryException, SQLException, PSInvalidXmlException,
            PSMissingApplicationPolicyException
   {
      Document doc = getLocaleDocument(rxroot);
      List<String> list = new ArrayList<String>();
      NodeList nl = doc.getElementsByTagName("column");
      Element elem = null;
      String temp = null;
      Node node = null;
      for(int i=0; nl != null && i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         temp = elem.getAttribute(PSRxltConfigUtils.ATTR_NAME);
         if(!temp.equals("LANGUAGESTRING"))
            continue;

         node = elem.getFirstChild();
         if(node instanceof Text)
            temp = ((Text)node).getData();
         if(temp!=null)
            temp = temp.trim();
         if(temp != null && temp.length() > 0)
         list.add(temp);
      }
      return list.toArray();
   }

   /**
    * Another utility method that returns the XML document with RXLOCALE table
    * data. The document conforms to the DTD defined in PSJdbcTableFactory
    * package. The repository properties file that is written by the installer
    * during installation is used to get the database settings for Rhythmyx
    * server and then JDBC table factory is used to get the locale data.
    *
    * @param rxroot Rhythmyx root directory, must not be <code>null</code> and
    * may be <code>empty</code>
    *
    * @return DOM Document of the data from the RXLOCALE table.
    *
    * @throws IllegalArgumentException if <code>rxroot</code> is
    * <code>null</code>.
    * @throws FileNotFoundException if repository properties file is not found
    * with the given Rhythmyx root directory
    * @throws IOException any error reading the repository properties file
    * @throws SAXException any parse error or error building the result XML
    * document for the locales
    * @throws PSJdbcTableFactoryException if there is any error during JDBC
    * table factory processing
    * @throws SQLException if any JDBC error occurs
    * @throws PSMissingApplicationPolicyException if an error occurs while
    * getting the repository properties
    * @throws PSInvalidXmlException if an error occurs while getting the
    * repository properties
    */
   public static Document getLocaleDocument(String rxroot)
      throws FileNotFoundException, IOException, SAXException,
            PSJdbcTableFactoryException, SQLException, PSInvalidXmlException,
            PSMissingApplicationPolicyException
   {
      if (rxroot == null)
         throw new IllegalArgumentException("rxroot may not be null");

      PSJdbcDbmsDef dbmsDef = getDbmsDef(rxroot);
      PSJdbcDataTypeMap dataTypeMap = getDataTypeMap(rxroot);
      Connection conn = PSJdbcTableFactory.getConnection(dbmsDef);
      return getTableDataDoc(conn, dbmsDef, dataTypeMap);
   }


   /**
    * Get the data type map used when querying or updating the repository.
    *
    * @param rxroot Rhythmyx root directory, must not be <code>null</code> and
    * may be <code>empty</code>
    *
    * @return The data type map, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>rxroot</code> is
    * <code>null</code>.
    * @throws FileNotFoundException if repository properties file is not found
    * with the given Rhythmyx root directory
    * @throws IOException any error reading the repository properties file
    * @throws PSJdbcTableFactoryException if any other error occurs.
    * @throws SAXException if an error occurs while getting the repository
    * properties
    * @throws PSMissingApplicationPolicyException if an error occurs while
    * getting the repository properties
    * @throws PSInvalidXmlException if an error occurs while getting the
    * repository properties
    */
   public static PSJdbcDbmsDef getDbmsDef(String rxroot)
      throws FileNotFoundException, IOException, PSJdbcTableFactoryException,
      PSInvalidXmlException, PSMissingApplicationPolicyException, SAXException
   {
      if (rxroot == null)
         throw new IllegalArgumentException("rxroot may not be null");

      Properties props = getRepositoryProperties(rxroot);

      return new PSJdbcDbmsDef(props);
   }

   /**
    * Get the data type map used when querying or updating the repository.
    *
    * @param rxroot Rhythmyx root directory, must not be <code>null</code> and
    * may be <code>empty</code>
    *
    * @return The data type map, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>rxroot</code> is
    * <code>null</code>.
    * @throws FileNotFoundException if repository properties file is not found
    * with the given Rhythmyx root directory
    * @throws IOException any error reading the repository properties file
    * @throws SAXException any parse error or error building the data type map.
    * @throws PSJdbcTableFactoryException if there are any other errors.
    * @throws PSMissingApplicationPolicyException if an error occurs while
    * getting the repository properties 
    * @throws PSInvalidXmlException if an error occurs while getting the
    * repository properties 
    */
   public static PSJdbcDataTypeMap getDataTypeMap(String rxroot)
      throws FileNotFoundException, IOException, SAXException,
            PSJdbcTableFactoryException, PSInvalidXmlException,
            PSMissingApplicationPolicyException
   {
      if (rxroot == null)
         throw new IllegalArgumentException("rxroot may not be null");

      Properties props = getRepositoryProperties(rxroot);
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
         props.getProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY),
         props.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY), null);

      return dataTypeMap;
   }

   /*
    * Implementation of the method defined in the interface
    * See {@link IPSSectionHandler#process(Element)} for
    * details about this method.
    */
   public void process(Element cfgData)
      throws PSActionProcessingException
   {
      throw new PSActionProcessingException("Not Implemented");
   }

   /**
    * Method to enable or disable the specified language. The data is taken
    * from the the configuration element. First the current state of the language
    * is queried. If the status is enabled it will disabled and vice versa.
    * @param cfgData must not be <code>null</code>.
    * @throws PSActionProcessingException
    */
   @SuppressWarnings("unused")
   private void processEnableDisableLanguage(Element cfgData)
      throws PSActionProcessingException
   {
      String rxroot = cfgData.getOwnerDocument().getDocumentElement().
         getAttribute(PSRxltConfigUtils.ATTR_RXROOT);
      String languageString = cfgData.getAttribute("languagestring");
      String newStatus = "1";
      ResultSet rs = null;
      try
      {
         PSJdbcDbmsDef dbmsDef = getDbmsDef(rxroot);
         Connection conn = PSJdbcTableFactory.getConnection(dbmsDef);
         String currentStatus = "0";
         try
         {

            String query = "SELECT STATUS FROM RXLOCALE " +
               " WHERE LANGUAGESTRING=?";
            try(PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(conn, query)) {
               stmt.setString(1, languageString);
               rs = stmt.executeQuery(query);
               if (rs == null || !rs.next()) {
                  throw new PSActionProcessingException(
                          "Language chosen does not exist");
               }
               currentStatus = rs.getString(1);
               if (currentStatus == null)
                  currentStatus = "";
            }
         }
         catch(SQLException e)
         {
         PSCommandLineProcessor.logMessage("errorToGetCurrentLangStatus",
            e.getMessage());
         }
         if(currentStatus.equals("1"))
            newStatus = "0";


         try(PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(conn,
                 "UPDATE RXLOCALE SET STATUS=? " +
                 " WHERE LANGUAGESTRING=?")){
            stmt.setInt(1, Integer.parseInt(newStatus));
            stmt.setString(2, languageString);
            stmt.executeUpdate();
         }

      }
      catch(Exception e)
      {
         PSCommandLineProcessor.logMessage("processFailedError", e.getMessage());
         throw new PSActionProcessingException(e.getMessage());
      }

      String[] args={languageString, "enabled"};
      if(!newStatus.equals("1"))
         args[1] = "disabled";
      PSCommandLineProcessor.logMessage("languageEnabledDisabled", args);
   }

   /**
    * Method to add the language. Data is taken from the configuration data
    * element. The new language is disabled by default. Finds the next Localeid
    * and sort order by querying the existing data.
    * @param cfgData must not be <code>null</code>.
    * @throws PSActionProcessingException if there is an error adding the
    * language to Rhythmyx Content Manager
    */
   @SuppressWarnings("unused")
   private void processAddLanguage(Element cfgData)
      throws PSActionProcessingException
   {
      String rxroot = cfgData.getOwnerDocument().getDocumentElement().
         getAttribute(PSRxltConfigUtils.ATTR_RXROOT).trim();
      String languageString =
         cfgData.getAttribute(PSRxltConfigUtils.ATTR_LANGUAGESTRING);
      String displayName =
         cfgData.getAttribute(PSRxltConfigUtils.ATTR_DISPLAYNAME);
      String desc = cfgData.getAttribute(PSRxltConfigUtils.ATTR_DESCRIPTION);
      PSCommandLineProcessor.logMessage("addingNewLanguage", languageString);
      try
      {
         PSJdbcDbmsDef dbmsDef = getDbmsDef(rxroot);
         PSJdbcDataTypeMap dataTypeMap = getDataTypeMap(rxroot);
         Connection conn = PSJdbcTableFactory.getConnection(dbmsDef);

         // new locales are disabled by default
         boolean saved = saveLocale(conn, dbmsDef, dataTypeMap, languageString,
            displayName, desc, 0, false);

         if (!saved)
            PSCommandLineProcessor.logMessage("languageAlreadyExists",
               languageString);

         /*
          * Now that language addition is successful, we create the resource
          * copies as stubs for future translation. Copy even when the language
          * to be added already exists
          */
         PSCommandLineProcessor.logMessage("creatingRxResourceCopies", "");
         PSLocaleRxResourceCopyHandler resourceCopier =
            new PSLocaleRxResourceCopyHandler(rxroot, languageString);
         resourceCopier.processResourceCopy();
      }
      catch(Exception e)
      {
         PSCommandLineProcessor.logMessage("processFailedError", e.getMessage());
         throw new PSActionProcessingException(e.getMessage());
      }

      PSCommandLineProcessor.logMessage("newLanguageAdded", "");
   }

   /**
    * Method to edit the display name of the specified language (locale).
    * The data is taken from the the configuration element. Finds the specified
    * language and changes displayname and description attributes.
    * @param cfgData must not be <code>null</code>.
    * @throws PSActionProcessingException if there is an error editing an existing
    * language or locale
    */
   @SuppressWarnings("unused")
   private void processEditLanguage(Element cfgData)
      throws PSActionProcessingException
   {
      String rxroot = cfgData.getOwnerDocument().getDocumentElement().
         getAttribute(PSRxltConfigUtils.ATTR_RXROOT);
      String languageString = cfgData.getAttribute("languagestring");
      String newDisplayName = cfgData.getAttribute("displayname");
      String newDescription = cfgData.getAttribute("description");

      try
      {
         PSJdbcDbmsDef dbmsDef = getDbmsDef(rxroot);
         Connection conn = PSJdbcTableFactory.getConnection(dbmsDef);


         if (newDisplayName.length() > 0)
         {
            try(PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(conn,
                    "UPDATE RXLOCALE SET DISPLAYNAME = ?" +
                 " WHERE LANGUAGESTRING=?")) {
               stmt.setString(1, newDisplayName);
               stmt.setString(2, languageString);
               stmt.executeUpdate();
            } catch (SQLException throwables) {
               PSCommandLineProcessor.logMessage("processFailedError", throwables.getMessage());
            }
         }
         if (newDescription.length() > 0)
         {
            try(PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(conn,
                    "UPDATE RXLOCALE SET DESCRIPTION = ?" +
                            " WHERE LANGUAGESTRING=?")) {

               stmt.setString(1,newDescription);
               stmt.setString(2,languageString);
               stmt.executeUpdate();

            } catch (SQLException throwables) {
               PSCommandLineProcessor.logMessage("processFailedError", throwables.getMessage());
            }
         }

      }
      catch(Exception e)
      {
         PSCommandLineProcessor.logMessage("processFailedError", e.getMessage());
         throw new PSActionProcessingException(e.getMessage());
      }

   }

   /**
    * This is utility method to generate backend table data XML document.
    *
    * @param conn SQL connection to the database, must not be <code>null</code>.
    * @param dbmsDef DBMS definition object, must not be <code>null</code>.
    * @param dataTypeMap datatyppemap object, must not be <code>null</code>.
    *
    * @return DOM document containing the table data. The DTD for this is
    * defined JDBC TableFactory package, may be <code>null</code> if the backend
    * table cannot be located.
    *
    * @throws IllegalArgumentException if any of the arguments is
    * <code>null</code>
    * @throws PSJdbcTableFactoryException in case of any error in JDBC table
    * factory processing
    */
   static public Document getTableDataDoc(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap)
         throws PSJdbcTableFactoryException
   {
      if(conn == null)
         throw new IllegalArgumentException("conn must not be null");

      if(dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef must not be null");

      if(dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap must not be null");

      Document dataDoc = null;

      PSJdbcTableData tableData = getTableData(conn, dbmsDef, dataTypeMap,
         null);

      if(tableData != null)
      {
         dataDoc = PSXmlDocumentBuilder.createXmlDocument();
         dataDoc.appendChild(dataDoc.importNode(
            tableData.toXml(dataDoc), true));
      }

      return dataDoc;
   }

   /**
    * Utility method to generate backend table data representing the specified
    * locale(s).  All rows in the data will have an action of
    * {@link PSJdbcRowData#ACTION_INSERT}.
    *
    * @param conn SQL connection to the repository, may not be <code>null</code>
    * and must be a valid connection.
    * @param dbmsDef The dbms defintion for the database containing the
    * repository, may not be <code>null</code>.
    * @param dataTypeMap The dbms defintion for the database containing the
    * repository, may not be <code>null</code>.
    * @param languageString The identifier for the locale to retrive, may be
    * <code>null</code> to get all locales defined in the repository.  May not
    * be empty.
    *
    * @return The table data for the specified language(s).  May be
    * <code>null</code> if no rows were returned or if the backend table cannot
    * be located.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSJdbcTableFactoryException if there are any other errors.
    */
   public static PSJdbcTableData getTableData(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap,
      String languageString) throws PSJdbcTableFactoryException
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (dbmsDef == null)
         throw new IllegalArgumentException("dbmsDef may not be null");

      if (dataTypeMap == null)
         throw new IllegalArgumentException("dataTypeMap may not be null");

      if (languageString != null && languageString.trim().length() == 0)
         throw new IllegalArgumentException("languageString may not be empty");

      PSJdbcTableSchema tableSchema = getTableSchema(conn, dbmsDef,
         dataTypeMap);
      PSJdbcTableData tableData = null;
      if (tableSchema != null)
      {
         PSJdbcSelectFilter filter = null;
         if (languageString != null)
         {
            filter = new PSJdbcSelectFilter(COL_LANGUAGE_STRING,
               PSJdbcSelectFilter.EQUALS, languageString, Types.VARCHAR);
         }

         tableData = PSJdbcTableFactory.catalogTableData(conn, dbmsDef,
            tableSchema, null, filter, PSJdbcRowData.ACTION_INSERT);
      }

      return tableData;
   }

   /**
    * Method to know if a given language string is supported by JRE. A language
    * string that is being added to Rhythmyx as part of localization must be
    * supported by JRE for dates to be localized.
    * @param languageString the language string in XML syntax (e.q. fr-ca)
    * must not be <code>null</code> or <code>empty</code>.
    * @return <code>true</code> if supported, <code>false</code> otherwise.
    * @throws IllegalArgumentException if languageString is <code>null</code> or
    * <code>empty</code>
    */
   public synchronized static boolean isLocaleSupported(String languageString)
      throws IllegalArgumentException
   {
      if(languageString == null || languageString.length() < 1)
         throw new IllegalArgumentException(
            "Language string must not be null or empty");
      //List was never built, build it
      if(ms_SupportedLocaleStrings == null)
      {
         ms_SupportedLocaleStrings = new ArrayList<String>();
         Locale locale[] = Locale.getAvailableLocales();
         for(int i=0; i<locale.length; i++)
         {
            //Convert from Java's "en_US" form to XML's "en-us" form
            ms_SupportedLocaleStrings.add(
               locale[i].toString().toLowerCase().replace('_', '-'));
         }
      }
      if(ms_SupportedLocaleStrings.contains(languageString))
         return true;
      return false;
   }

   /**
    * Saves the supplied locale infomation to the database.
    *
    * @param conn SQL connection to the repository, may not be <code>null</code>
    * and must be a valid connection.
    * @param dbmsDef The dbms defintion for the database containing the
    * repository, may not be <code>null</code>.
    * @param dataTypeMap The dbms defintion for the database containing the
    * repository, may not be <code>null</code>.
    * @param languageString The language string used to uniquely identify this
    * locale, may not be <code>null</code> or empty.
    * @param displayName The display name of the locale, may not be
    * <code>null</code> or empty.
    * @param desc An optional description, may be <code>null</code> or empty.
    * @param status The status of the locale.
    * @param overwrite If <code>true</code>, an existing locale will be
    * overwritten.  Otherwise, this method perform a <code>noop</code> if the
    * locale already exists.
    *
    * @return <code>true</code> if the locale is saved, <code>false</code> if
    * <code>overwrite</code> is <code>false</code> and the locale already
    * exists (i.e. the locale is not saved).
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws SQLException If there are any errors generating system ids.
    * @throws PSJdbcTableFactoryException if there are any other errors.
    */
   public boolean saveLocale(Connection conn, PSJdbcDbmsDef dbmsDef,
      PSJdbcDataTypeMap dataTypeMap, String languageString,
      String displayName, String desc, int status, boolean overwrite)
         throws SQLException, PSJdbcTableFactoryException
   {
      if (languageString == null || languageString.trim().length() == 0)
         throw new IllegalArgumentException(
            "languageString may not be null or empty");

      if (displayName == null || displayName.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayName may not be null or empty");

      if (desc == null)
         desc = "";

      boolean saved = false;
      boolean exists = false;
      // see if it already exists
      PSJdbcRowData rowData = null;
      PSJdbcTableData tableData = getTableData(conn, dbmsDef, dataTypeMap,
         languageString);
      if (tableData != null)
      {
         Iterator<?> rows = tableData.getRows();
         if (rows.hasNext())
         {
            rowData = (PSJdbcRowData)rows.next();
            exists = true;
         }
      }

      // if already exists and overwite == false, we're done
      if (exists && !overwrite)
         return saved;

      // if not, generate id and sortorder
      String localeId = "1";
      String sortOrder = "1";
      if (!exists)
      {
         int[] sysCols = generateSystemColumnValues(conn);
         localeId = String.valueOf(sysCols[0]);
         sortOrder = String.valueOf(sysCols[1]);
      }
      else
      {
         // get existing ids
         PSJdbcColumnData col;

         col = rowData.getColumn(COL_LOCALE_ID);
         if (col != null)
            localeId = col.getValue();

         col = rowData.getColumn(COL_SORT_ORDER);
         if (col != null)
            sortOrder = col.getValue();
      }

      // build the new row with replace action
      List<PSJdbcRowData> rowList = new ArrayList<PSJdbcRowData>();
      List<PSJdbcColumnData> colList = new ArrayList<PSJdbcColumnData>();
      colList.add(new PSJdbcColumnData(COL_LOCALE_ID, localeId));
      colList.add(new PSJdbcColumnData(COL_LANGUAGE_STRING, languageString));
      colList.add(new PSJdbcColumnData(COL_DISPLAY_NAME, displayName));
      colList.add(new PSJdbcColumnData(COL_DESCRIPTION, desc));
      colList.add(new PSJdbcColumnData(COL_SORT_ORDER, sortOrder));
      colList.add(new PSJdbcColumnData(COL_STATUS, String.valueOf(status)));
      rowList.add(new PSJdbcRowData(colList.iterator(),
         PSJdbcRowData.ACTION_REPLACE));

      // save it
      PSJdbcTableSchema schema = getTableSchema(conn, dbmsDef, dataTypeMap);
      schema.setTableData(new PSJdbcTableData(schema.getName(),
         rowList.iterator()));
      try
      {
         PSJdbcTableFactory.processTable(conn, dbmsDef, schema, null, false);
         saved = true;
      }
      finally
      {
         // clear the tabledata if we can
         try
         {
            schema.setTableData(null);
         }
         catch (PSJdbcTableFactoryException e){}
      }

      return saved;
   }

   /**
    * Gets the repository properties, loading them from disk if they are not
    * already loaded.
    *
    * @param rxroot Rhythmyx root directory, assumed not <code>null</code>, may
    * be empty.
    *
    * @return The properties, never <code>null</code>.
    *
    * The following exceptions are thrown if an error occurs while getting the
    * repository properties:
    * 
    * @throws FileNotFoundException
    * @throws IOException
    * @throws SAXException 
    * @throws PSMissingApplicationPolicyException 
    * @throws PSInvalidXmlException 
    * @throws com.percussion.utils.container.PSMissingApplicationPolicyException 
    */
   private static Properties getRepositoryProperties(String rxroot)
      throws FileNotFoundException, IOException, PSInvalidXmlException,
       SAXException, com.percussion.utils.container.PSMissingApplicationPolicyException
   {
      if (m_repositoryProperties == null)
         m_repositoryProperties = PSJdbcDbmsDef.loadRxRepositoryProperties(rxroot);
     
      return m_repositoryProperties;
   }

   /**
    * Gets and caches the table schema for the repository table containing the
    * locale defintions.
    *
    * @param conn SQL connection to the repository, assumed not
    * <code>null</code> and to be a valid connection.
    * @param dbmsDef The dbms defintion for the database containing the
    * repository, assumed not <code>null</code>.
    * @param dataTypeMap The dbms defintion for the database containing the
    * repository, assumed not <code>null</code>.
    *
    * @return The schema defintion, may be <code>null</code> if the table cannot
    * be located.
    *
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   private static PSJdbcTableSchema getTableSchema(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap)
        throws PSJdbcTableFactoryException
   {
      if (m_localeSchema == null)
      {
         m_localeSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
         dataTypeMap, LOCALE_TABLE, false);
         m_localeSchema.setAllowSchemaChanges(false);
      }

      return m_localeSchema;
   }

   /**
    * Gets system column values for a new locale.  This includes the values for
    * the following columns:
    * <ol>
    * <li>LOCALEID</li>
    * <li>SORTORDER</li>
    * </ol>
    *
    * @param conn The connection to use, assumed not <code>null</code>.
    *
    * @return An array of columns values, never <code>null</code>.  The number
    * and order corresponds to the above list.
    *
    * @throws SQLException If there are any errors.
    */
   private static int[] generateSystemColumnValues(Connection conn)
      throws SQLException
   {
      Statement stmt = null;
      ResultSet rs = null;

      try
      {
         int localeid = 0;
         int sortorder = 0;
         stmt = PSSQLStatement.getStatement(conn);

         rs = stmt.executeQuery(
         "SELECT MAX(LOCALEID), MAX(SORTORDER) FROM RXLOCALE");
         if(rs!=null && rs.next())
         {
            localeid = rs.getInt(1);
            sortorder = rs.getInt(2);
         }

         localeid += 1;
         sortorder += 1;

         return new int[] {localeid, sortorder};
      }
      finally
      {
         if(rs!=null)
         {
            try
            {
               rs.close();
               rs = null;
            }
            catch(SQLException e){}
         }
         if(stmt!=null)
         {
            try
            {
               stmt.close();
               stmt = null;
            }
            catch(SQLException e){}
         }
      }
   }

   /**
    * XML Language string version of all languages supported by JAVA. When user
    * adds a new language, it is better if the user selects from this list.
    * Otherwise date formatting would not work for that unknown locale and
    * System's default Locale is used to formatting the date. We depend on Java
    * for date formatting. This list is built only once when it is required.
    */
   static private List<String> ms_SupportedLocaleStrings = null;

   /**
    * The repository properties, <code>null</code> until loaded by a call to
    * {@link #getRepositoryProperties}.
    */
   private static Properties m_repositoryProperties = null;

   /**
    * The table schema for the locales table, <code>null</code> until loaded by
    * a call to <code>getTableSchema()</code>.
    */
   private static PSJdbcTableSchema m_localeSchema = null;

   /**
    * Constant for the name of the repository table containing locale
    * definitions.
    */
   public static final String LOCALE_TABLE = "RXLOCALE";

   /**
    * Constant for the name of the LANGUAGESTRING column in the repository table
    * containing locale definitions.
    */
   public static final String COL_LANGUAGE_STRING = "LANGUAGESTRING";

   /**
    * Constant for the name of the DISPLAYNAME column in the repository table
    * containing locale definitions.
    */
   public static final String COL_DISPLAY_NAME = "DISPLAYNAME";

   /**
    * Constant for the name of the DESCRIPTION column in the repository table
    * containing locale definitions.
    */
   public static final String COL_DESCRIPTION = "DESCRIPTION";

   /**
    * Constant for the name of the STATUS column in the repository table
    * containing locale definitions.
    */
   public static final String COL_STATUS = "STATUS";

   /**
    * Constant for the name of the LOCALEID column in the repository table
    * containing locale definitions.
    */
   private static final String COL_LOCALE_ID = "LOCALEID";

   /**
    * Constant for the name of the SORTORDER column in the repository table
    * containing locale definitions.
    */
   private static final String COL_SORT_ORDER = "SORTORDER";

   /*
    * main method for test purpose.
    * @param args
    */
   @SuppressWarnings("unused")
   public static void main(String[] args)
   {
      try
      {
         PSLocaleHandler localeHandler = new PSLocaleHandler();
         System.out.println(PSXmlDocumentBuilder.toString(
            PSLocaleHandler.getLocaleDocument(System.getProperty("rxdeploydir"))));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
