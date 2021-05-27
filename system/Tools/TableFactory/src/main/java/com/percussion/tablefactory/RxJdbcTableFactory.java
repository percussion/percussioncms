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
package com.percussion.tablefactory;

import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSProperties;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
*  This class is used to install and upgrade Rhythmyx tables.
*/
public class RxJdbcTableFactory
{
  public RxJdbcTableFactory()
  {
       super();
  }

  /**
   *  This method will install the Rhythmyx tables.
   *
   * @param sServer - the server the database is on
   * @param sDatabase - the database the tables should be in.
   * @param sSchema - the schema for the database
   * @throws Exception if an error occurs
   */
  public boolean populateTablesIn(String sServer,
                                 String sDatabase,
                                 String sSchema)
   throws Exception
   {
      try
      {
         DbmsDefinition dbmsDef = new DbmsDefinition(sServer,
                                                      sDatabase,
                                                      sSchema);

         String sStatus = "";
         PSXmlTreeWalker walkerTableDef = null;
         PSXmlTreeWalker walkerTableData = null;
         Element eDef = null ;
         Element eData = null ;
         Element eDataRoot = null ;
         Element eDtdRoot  = null ;

         System.out.println("Install Logger starting ....");
         m_logger.setOutputStream(System.out);

         if(m_docConfig == null || m_docTableData == null)
            return false;

         walkerTableDef = new PSXmlTreeWalker(m_docConfig);
         walkerTableData = new PSXmlTreeWalker(m_docTableData);

         // generate the table definition
         TableDefinition tableDef = null ;
         String sTableName = null;

         cDB.dataDoc =  m_docTableData ;
         cDB.dtdDoc  =  m_docConfig ;

         Document xmlOldDataDoc = (Document)
          Class.forName("com.percussion.xml.PSTXDocument").newInstance();

         Document xmlOldDtdDoc = (Document)
          Class.forName("com.percussion.xml.PSTXDocument").newInstance();

         cDB.oldDataDoc = xmlOldDataDoc ;
         cDB.oldDtdDoc = xmlOldDtdDoc ;

         sStatus = parseTableDefs(cDB.dtdDoc,dbmsDef);
         if(sStatus != null && sStatus.length()>0)
         {
            m_logger.logMessage("Error while parsing the DTD Def file" +
                              " ... Exiting the Installer");
            m_sStatus = "Error while Parsing the DTD Def file" +
                        " ... Exiting the Installer";
            return false;
         }

         // Create a document object for Old Data Def and Data
         try
         {
            xmlOldDataDoc = PSXmlDocumentBuilder.createXmlDocument();
            xmlOldDtdDoc = PSXmlDocumentBuilder.createXmlDocument()
            ;
            // Create the root element
            eDataRoot = xmlOldDataDoc.createElement("tables");
            eDtdRoot = xmlOldDtdDoc.createElement("tables");

            xmlOldDataDoc.appendChild(eDataRoot);
            xmlOldDtdDoc.appendChild(eDtdRoot);
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            m_logger.logMessage("Unable to create XML backup Documents");
            m_sStatus = "Unable to create XML backup Documents" ;
            return false;
         }

         cDB.oldDataDoc = xmlOldDataDoc ;
         cDB.oldDtdDoc  = xmlOldDtdDoc ;
         cDB.eData      = eDataRoot ;
         cDB.eDtd       = eDtdRoot ;

         for(eDef = walkerTableDef.getNextElement("table", getChild);
               eDef!=null;
               eDef = walkerTableDef.getNextElement("table", getSibling))
         {
            sTableName = eDef.getAttribute("name");
            if(null == sTableName)
               continue;

            sTableName = sTableName.trim();
            for(eData = walkerTableData.getNextElement("table", getChild);
                     eData!=null;
                     eData = walkerTableData.getNextElement("table", getSibling))
            {
               if(eData.getAttribute("name").trim().equals(sTableName))
               break;
            }

            RxTables rxTable = null;
            for(int iTable = 0; iTable < cDB.RxTables.size(); ++iTable)
            {
              rxTable = (RxTables)cDB.RxTables.get(iTable);
              if(rxTable != null && rxTable.getTableName().equals(sTableName))
                  break;
            }

            if(rxTable != null)
            {
                tableDef = new TableDefinition(dbmsDef, eDef);
                String sError = "";
                try
                {
                   sError = tableDef.generateTable(dbmsDef, eData,rxTable);
                }
                catch(SQLException e)
                {
                  sError = e.getMessage();
                    log.error(e.getMessage());
                    log.debug(e.getMessage(), e);
                }

                if(sError.length() > 0)
                {
                   // Error Condition = Must Exit.
                   m_logger.logMessage(sError);
                   m_sStatus = sError ;
                   return false;
                }
                if(eData != null)
                   walkerTableData.setCurrent(eData.getOwnerDocument());
            }
         }

         NodeList oldNodeList = cDB.oldDtdDoc.getElementsByTagName("row");
         int oldNodeCount = oldNodeList.getLength();

         if(oldNodeCount > 0)
         {
            System.out.println("No of Transactions failed = " +
                                 Integer.toString(oldNodeCount));

            PSXmlTreeWalker walker1 = new PSXmlTreeWalker(cDB.oldDtdDoc);
            walker1.write(System.out);

            System.out.println("Saving Table definition in file called : " +
                                 RxTableInstallLogic.PS_OLD_DTD_FILENAME);
            sStatus = tableDef.fileSaveXMLDocument(cDB.oldDtdDoc,
                                       RxTableInstallLogic.PS_OLD_DTD_FILENAME,
                                       RxTableInstallLogic.CREATE_NEW_OLD);

            PSXmlTreeWalker walker2 = new PSXmlTreeWalker(cDB.oldDataDoc);
            walker2.write(System.out);

            System.out.println("Saving Table definition in file called : " +
                                 RxTableInstallLogic.PS_OLD_DATA_FILENAME);
            sStatus = tableDef.fileSaveXMLDocument(cDB.oldDataDoc,
                                       RxTableInstallLogic.PS_OLD_DATA_FILENAME,
                                       RxTableInstallLogic.CREATE_NEW_OLD);
         }
         else
         {
            System.out.println("No failed transaction while processing original data");
         }
      }
      finally
      {
         m_logger.shutdown();
      }
      return true;
   }


   /**
    * Checks the for Write permission in the target
    * directory.
    *
    * @param   targetDir   Directory to write to.
    *
    * @return  boolean false or true.
    *          true  = alloed to write
    *          false = Not allowed to write
    */
   public boolean checkWriteSecutrity(String targetDir)
   {
      boolean bRet = false;
      // TODO == do a dummy write.

      return bRet;
   }

   /**
    * Breaks down a workflow DTD document and stores it
    * in glabal DBStruct class.
    *
    * @param  docTableDef well formed DTD document
    * @param dbmsDef well formed database definition
    *
    * @return String : If empty then no errors, otherwise Error
    *
    * @ Other It stores the values in the glbal DBStruct
    *          class. It is broken down into Vector RxTables and
    *          Vector RxColumns within the Vector Columns which
    *          each Vector is part of a RxTables Class.
    */
   public String parseTableDefs(Document docTableDef, DbmsDefinition dbmsDef)
   {
      Vector  locRxTables       = new Vector();
      String   sTableName       = new String();
      String   sCreate          = new String();
      String   sAlter           = new String();
      String   sDelOldData      = new String();
      String   sColumnName      = new String();
      String   sStatus          = new String();

      PSXmlTreeWalker walkerTableDef = null ;
      try
      {
         NodeList nl = docTableDef.getElementsByTagName("tables");
         if(null == nl || nl.getLength() < 1)
         {
          throw new SAXException("No 'tables' element exists in the XML document");
         }

         Element eList = (Element)nl.item(0);
         PSXmlTreeWalker xmlTableDoc = new PSXmlTreeWalker(eList);
         NamedNodeMap atts ;
         eList = xmlTableDoc.getNextElement(false);
         do
         {
            int index = 0 ;
            if(eList.getTagName().equalsIgnoreCase("table"))
            {
               RxTables    locTable  = new RxTables();
               atts = eList.getAttributes();
               for(int i=0;i<atts.getLength();i++)
               {
                  String sAttribName = atts.item(i).getNodeName();
                  if(sAttribName.equalsIgnoreCase("name"))
                  {
                     sTableName = atts.item(i).getNodeValue();
                     locTable.setTableName(sTableName);
                  }
                  else if(sAttribName.equalsIgnoreCase("create"))
                  {
                     sCreate = atts.item(i).getNodeValue();
                     if(sCreate.charAt(0) == 'y' || sCreate.charAt(0) == 'Y')
                     {
                        locTable.setTableAction(RxTableInstallLogic.TABLE_CREATE_YES);
                     }
                     else if(sCreate.charAt(0) == 'n' || sCreate.charAt(0) == 'N')
                     {
                        locTable.setTableAction(RxTableInstallLogic.TABLE_CREATE_NO);
                     }
                  }
                  else if(sAttribName.equalsIgnoreCase("alter"))
                  {
                     sAlter = atts.item(i).getNodeValue();
                     if(sAlter.charAt(0) == 'y' || sAlter.charAt(0) == 'Y')
                     {
                        locTable.setTableAction(RxTableInstallLogic.TABLE_ALTER_YES);
                     }
                     else if(sAlter.charAt(0) == 'n' || sAlter.charAt(0) == 'N')
                     {
                        locTable.setTableAction(RxTableInstallLogic.TABLE_ALTER_NO);
                     }
                  }
                  else if(sAttribName.equalsIgnoreCase("delolddata"))
                  {
                     sDelOldData = atts.item(i).getNodeValue();
                     if(sDelOldData.charAt(0) == 'y' || sDelOldData.charAt(0) == 'Y')
                        locTable.setDataAction(true);
                     else
                        locTable.setDataAction(false);
                  }
               }

               eList = xmlTableDoc.getNextElement(false);
               Vector  locRxColumns = new Vector();

               do
               {
                  if(eList.getTagName().equalsIgnoreCase("row"))
                     continue ;

                  atts = eList.getAttributes();
                  if(eList.getTagName().equalsIgnoreCase("column"))
                  {
                     RxColumns locColumns = new RxColumns();
                     // Set the Key Column
                     if(index == 0)
                        locColumns.setKey(true);
                     String sValue  = atts.item(0).getNodeValue();
                     locColumns.setColName(sValue);

                     eList = xmlTableDoc.getNextElement(false);
                     sValue  = xmlTableDoc.getElementData(eList);
                     locColumns.setJdbcDataType(sValue);
                     locColumns.setNativeType(
                           dbmsDef.getDataTypeMapping(sValue));

                     eList = xmlTableDoc.getNextElement(false);
                     sValue  = xmlTableDoc.getElementData(eList);
                     if(sValue.length() > 0)
                        locColumns.setColLength(Integer.parseInt(sValue));
                     else
                        locColumns.setColLength(0);
                     // NULL
                     eList = xmlTableDoc.getNextElement(false);
                     sValue  = xmlTableDoc.getElementData(eList);
                     if(sValue.equalsIgnoreCase("no"))
                     {
                        locColumns.setAllowNull(false);
                     }
                     else locColumns.setAllowNull(true);

                     if(locRxColumns == null)
                        locRxColumns = new Vector();

                     index++;
                     locColumns.setColNo(index);

                     locRxColumns.add(locColumns);
                  }
               } while ((eList = xmlTableDoc.getNextElement(false)) != null &&
                           eList.getTagName().equalsIgnoreCase("table") == false);

               index = 0 ;
               locTable.vColumns = locRxColumns ;
               locRxColumns = null ;

               // Set dblink ??
               locTable.setDataBase(cDB.getDataBase());
               locTable.setSchema(cDB.getSchema());

               locRxTables.add(locTable);
            }
         } while (xmlTableDoc.getNextElement(false) != null);

         cDB.RxTables = locRxTables ;
      }
      catch(Exception e)
      {
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
         sStatus = e.getMessage();
      }
      return sStatus ;
   } // parseTableDefs()


   public Document populateTablesOut(String sServer,
                                 String sDatabase,
                                 String sSchema)
   {
      Document xmlDoc = null;
      Statement stmt = null;
      ResultSet rs = null;
      try
      {
         DbmsDefinition dbmsDef = new DbmsDefinition(sServer, sDatabase, sSchema);

         PSXmlTreeWalker walkerTableDef = null;
         PSXmlTreeWalker walkerTableData = null;

         walkerTableDef = new PSXmlTreeWalker(m_docConfig);

         DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
         InputSource src = new InputSource( new StringReader("<tables/>"));
         xmlDoc = db.parse(src);

         // generate the table definition
         TableDefinition tableDef = null;
         String sTableName = null;

         Element eRow = null;
         Element eCol = null;
         Element eTable = null;
         Text text = null;
         int nCols = 0;
         int i = 0;

         ResultSetMetaData rsMD = null;
      String sqlStmt = "SELECT * FROM ";
      Connection con = dbmsDef.getConnection();
      stmt = PSSQLStatement.getStatement(con);
      Vector colNames = new Vector();
         for(Element eDef = walkerTableDef.getNextElement("table", getChild);
          eDef!=null;
               eDef = walkerTableDef.getNextElement("table", getSibling))
      {
        sTableName = eDef.getAttribute("name");
        if(null == sTableName)
          continue;
        eTable = xmlDoc.createElement("table");
        eTable.setAttribute("name", sTableName);
        eTable = (Element) xmlDoc.getDocumentElement().appendChild(eTable);
        rs = stmt.executeQuery(sqlStmt+sTableName);
        rsMD = rs.getMetaData();
        nCols = rsMD.getColumnCount();
        colNames.clear();
        for(int j=1; j<=nCols; j++)
          colNames.addElement(rsMD.getColumnName(j));

        while(rs.next())
        {
          eRow = xmlDoc.createElement("row");
          eRow = (Element) eTable.appendChild(eRow);
          for(i=1; i<=nCols; i++)
          {
            eCol = xmlDoc.createElement("column");
            eCol.setAttribute("name", colNames.elementAt(i-1).toString());
            text = xmlDoc.createTextNode(rs.getString(i));
            eCol.appendChild(text);
            eCol = (Element) eRow.appendChild(eCol);
          }
        }
        rs.close();
      }
      }
      catch(Exception e)
      {
         m_pOut.println();
         m_pOut.println("Exception encountered - copy aborted.");
         e.printStackTrace(m_pOut);
      }
      finally
    {
      try
      {
        rs.close();
        stmt.close();
      }
      catch (SQLException fix) {}
     }

      return xmlDoc;
   }



   class DbmsDefinition
   {
      DbmsDefinition(String srv, String db, String schema)
               throws    java.io.IOException,
                      java.lang.ClassNotFoundException,
                              org.xml.sax.SAXException,
                            java.sql.SQLException
      {
         m_sBackend = m_Props.getProperty("DB_BACKEND");
         m_driver = m_Props.getProperty("DB_DRIVER_NAME");
         m_class = m_Props.getProperty("DB_DRIVER_CLASS_NAME");

          cDB.setSqlDb(m_sBackend);

         if (null == srv)
         {
            m_server = m_Props.getProperty("DB_SERVER");
            if (m_server.equals("*"))
               m_server = "";
         }
         else
            m_server = srv;

         if (null == db)
         {
//          m_database = m_Props.getProperty("DB_NAME", "*");
            m_database = m_Props.getProperty("DB_NAME");
            if (m_database !=null && m_database.trim().length() < 1
                  && m_database.equals("*"))
               m_database = null;
         }
         else
            m_database = db;

         m_version = m_Props.getProperty("DB_DRIVER_VERSION");
         m_uid = m_Props.getProperty("UID");
         try {
             m_pw = PSEncryptor.getInstance("AES",
                     PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
             ).decrypt(m_Props.getProperty("PWD"));
         }catch(Exception e){
             m_pw = PSLegacyEncrypter.getInstance(
                     PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
             ).decrypt(m_Props.getProperty("PWD"),
                     PSJdbcDbmsDef.getPartOneKey(),null);
         }
         String sSchema = m_Props.getProperty("DB_SCHEMA");
         cDB.setServer(m_Props.getProperty("DB_SERVER"));

         if(m_database == null)
         {
            cDB.setDataBase(null);
            cDB.setSchema(sSchema.toUpperCase());
         }
         else
         {
            m_database = m_database.trim();
            if(m_database.length()==  0)
            {
               cDB.setDataBase(null);
               cDB.setSchema(sSchema.toUpperCase());
            }
            else if(m_database.charAt(0)=='*')
            {
               cDB.setDataBase(null);
               cDB.setSchema(sSchema.toUpperCase());
            }
            else
            {
               cDB.setDataBase(m_database);
               cDB.setSchema(sSchema);
            }
         }

         try
         {
            PSXmlTreeWalker w = new PSXmlTreeWalker(m_docDataTypeMap);
            Class.forName(m_class);

            Element e = null;
            for(e = w.getNextElement("DataTypeMap", getChild);
               e!=null;
               e = w.getNextElement("DataTypeMap", getSibling))
            {
               if(e.getAttribute("for").trim().equals(m_sBackend))
               {
                  String sDriver = e.getAttribute("driver");
                  if(sDriver == null ||
                     sDriver.length() == 0)
                        break;

                  if(sDriver.equals(m_driver))
                     break;
               }
            }

            for(e = w.getNextElement("DataType", getChild);
                  e != null;
                  e = w.getNextElement("DataType", getSibling) )
            {
               storeDataTypeMapping(
               w.getElementData("@jdbc", false),
               w.getElementData("@native", false));
            }

            // now we can fix the identifier cases (and set schema for that matter)
            m_database = fixIdentifierCase(m_database);
            m_schema = fixIdentifierCase(sSchema);
         }
         finally
         {
         }
   }

   /**
    * Returns the connection stored in the member variable if present.
    * Otherwise, creates a new connection and stores it in the member variable.
    *
    * @returns Connection to the database specified by the properties, or
    *    <code>null</code> if a connection could not be established
    * @throws SQLException if cannot load JDBC driver
    */
   public Connection getConnection() throws SQLException
   {
      if (m_conn == null)
      {
          if (m_class == null)
              throw new SQLException("Cannot load driver of " + m_driver);

          try
          {
              Class.forName(m_class);
          }
          catch (ClassNotFoundException cls)
          {
              throw new SQLException("JDBC driver class not found. " + cls.toString());
          }
          catch (LinkageError link)
          {
              throw new SQLException("JDBC driver could not be loaded. " + link.toString());
          }

          String dbUrl = PSSqlHelper.getJdbcUrl(m_driver, m_server);
          Properties props = PSSqlHelper.makeConnectProperties(dbUrl,
            m_database, m_uid, m_pw);

          //for sql server disable ansi nulls and paddings. Otherwise the
          //data will be padded. This only applies to odbc
          if(m_sBackend.equals("MSSQL") && m_driver.equals("odbc"))
            dbUrl += ";AnsiNPW=no";
          System.out.println("Connecting to: " + dbUrl + "...");

          // try 5 times to establish the connection before we give up
          Connection conn = null;
          for (int i=0; i<5; i++)
          {
           try
           {
              conn = DriverManager.getConnection(dbUrl, props);
              if (conn != null)
              {
                  if (m_database != null)
                      conn.setCatalog(m_database);

                  break;
              }
           }
           catch (SQLException e)
           {
              try
              {
                  Thread.sleep(i * 1000);
              }
              catch (InterruptedException e1)
              {
               // just ignore this
              }
           }
         }

         if (conn == null)
         {
            System.out.println("Failed to connect\n");
            System.out.println("Credentials used are :\n" +
                  "  \nDatabase: " + m_database +
                  "  \nUserID  : " + m_uid +
                  "  \nPassword: " + m_pw);
            throw new SQLException("Failed to connect");
         }
         else
          m_conn = conn;
      }

      return m_conn;
   }

   public DatabaseMetaData getMetaData()
         throws java.sql.SQLException
   {
      if (m_meta == null)
      {
          System.out.println("getMetaData() ");
         Connection cConn = getConnection();
         if(cConn != null)
         {
            m_meta = m_conn.getMetaData();
         }
      }

      return m_meta;
   }

   public String getDataTypeMapping(String jdbcType)
   {
      return (String)m_dtJdbcStr2Native.get(jdbcType);
   }

    public String getJdbcDataTypeMapping(String nativeType)
   {
      String strRet = (String)m_dtNative2JdbcStr.get(nativeType);
      if(strRet == null)
      {
         strRet = (String)m_dtNative2JdbcStr.get(nativeType.toUpperCase());
      }

      return strRet;
   }


   public int getJdbcTypeMapping(String jdbcType)
   {
      Integer type = (Integer)m_dtJdbcStr2JdbcInt.get(jdbcType);
      if (type == null)
      {
         type = (Integer)m_dtJdbcStr2JdbcInt.get(jdbcType.toUpperCase());
      }
      if (type == null)
      {
         throw new IllegalArgumentException("No mapping defined for " +
                                                            jdbcType);
      }
      return type.intValue();
   }

   public String getJdbcTypeMapping(int ijdbcType)
   {
      String type = (String)m_dtJdbcInt2JdbcStr.get(new Integer(ijdbcType));
      if (type == null)
      {
         throw new IllegalArgumentException("No mapping defined for " +
                                                            ijdbcType);
      }
      return type;
   }


   public String fixIdentifierCase(String identifier)
                        throws java.sql.SQLException
   {
      if (identifier == null)
         return null;

      if (m_meta == null)
         getMetaData();

      if(m_meta != null)
      {
         if (m_meta.storesLowerCaseIdentifiers())
            identifier = identifier.toLowerCase();
         else if (m_meta.storesUpperCaseIdentifiers())
            identifier = identifier.toUpperCase();
      }

      return identifier;
   }

   public String getQualifiedIdentifier(String identifier)
                        throws java.sql.SQLException
   {
      if (m_meta == null)
         getMetaData();

      identifier = fixIdentifierCase(identifier);

      StringBuffer buf = new StringBuffer();

      boolean addedCatalog = false;
      String catalog = null;
      if ((m_database != null) && (m_database.length() != 0))
      {
         if (m_meta.supportsCatalogsInDataManipulation())
         {
            if (m_meta.isCatalogAtStart())
            {
               buf.append(m_database);
               buf.append(m_meta.getCatalogSeparator());
               addedCatalog = true;
            }
            else
               catalog = m_meta.getCatalogSeparator() + m_database;
         }
      }

      /* if we have an origin, see if it's permitted
       * if we've already written the catalog info to the front,
       * we then need to add the schema, even if it's empty,
       * to avoid catalog.table from being treated as
       * schema.table.
       */
      String origin = m_schema;
      if (origin == null) origin = "";
      if ((origin.length() != 0) || addedCatalog)
      {
         if (m_meta.supportsSchemasInDataManipulation())
         {
            buf.append(origin);
            buf.append('.');
         }
      }

      buf.append(identifier); // this has to be there

      // if catalog belongs on the end, take care of it now
      if (!addedCatalog && (catalog != null))
         buf.append(catalog);

      return buf.toString();
   }


   private void storeDataTypeMapping(String jdbcType, String nativeType)
                     throws IllegalArgumentException
   {
      try
      {
         Integer jType = new Integer(
         java.sql.Types.class.getField(jdbcType).getInt(null));
         m_dtJdbcStr2Native.put(jdbcType, nativeType);
          m_dtNative2JdbcStr.put(nativeType, jdbcType);
         m_dtJdbcStr2JdbcInt.put(jdbcType, jType);
          m_dtJdbcInt2JdbcStr.put(jType, jdbcType);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(
                        "Failed to load " +
                        jdbcType + " JDBC type: " +
                        e.toString());
      }
   }

   private String m_sBackend;
   private Connection m_conn;
   private DatabaseMetaData m_meta;
   private String m_driver;
   private String m_class;
   private String m_server;
   private String m_database;
   private String m_schema;
   private String m_version;
   private String m_uid;
   private String m_pw;
   private HashMap m_dtJdbcStr2Native = new HashMap();
   private HashMap m_dtNative2JdbcStr = new HashMap();
   private HashMap m_dtJdbcStr2JdbcInt = new HashMap();
   private HashMap m_dtJdbcInt2JdbcStr = new HashMap();

   public String getBackEndDB(){return m_sBackend;}
   public String getDriver(){return m_driver;}
   public String getServer(){return m_server;}
   public String getDataBase(){return m_database;}
   public String getSchema(){return m_schema;}
}


   class TableDefinition
   {
      TableDefinition(DbmsDefinition dbmsDef, Element eTableDef)
            throws   java.io.IOException,
                  java.lang.ClassNotFoundException,
                  org.xml.sax.SAXException,
                  java.lang.IllegalArgumentException,
                  java.sql.SQLException
      {
         m_dbmsDef = dbmsDef;
         PSXmlTreeWalker w = new PSXmlTreeWalker(eTableDef);

         try
         {
            // get the table name
            m_name = dbmsDef.fixIdentifierCase(eTableDef.getAttribute("name"));
            m_qualifiedName = dbmsDef.getQualifiedIdentifier(m_name);

            String sCreateFlag = eTableDef.getAttribute("create");

            if(sCreateFlag.length() >0)
            {
               if(sCreateFlag.charAt(0) == 'y')
               {
                  iCreateFlag = RxTableInstallLogic.CREATE_YES ;
               }
               else if(sCreateFlag.charAt(0) == 'n')
               {
                  iCreateFlag = RxTableInstallLogic.CREATE_NO ;
               }
               else iCreateFlag = RxTableInstallLogic.CREATE_YES ;
            }
            else iCreateFlag = RxTableInstallLogic.CREATE_YES ;

            String sAlterFlag = eTableDef.getAttribute("alter");
            if(sAlterFlag.length() > 0 )
            {
               if(sAlterFlag.charAt(0) == 'y')
               {
                  iAlterFlag = RxTableInstallLogic.ALTER_YES ;
               }
               else if(sAlterFlag.charAt(0) == 'n')
               {
                  iAlterFlag = RxTableInstallLogic.ALTER_NO ;
               }
               else iAlterFlag = RxTableInstallLogic.ALTER_NONE ;
            }
            else iAlterFlag = RxTableInstallLogic.ALTER_NOT ;

            String sDelOldData = eTableDef.getAttribute("delolddata");
            if(sDelOldData.length() > 0 )
            {
//System.out.println("Delete Flag= "+sDelOldData);
               if(sDelOldData.charAt(0) == 'y')
               {
                  iDelOldData = RxTableInstallLogic.DELETE_YES ;
               }
               else if(sDelOldData.charAt(0)== 'n')
               {
                  iDelOldData = RxTableInstallLogic.DELETE_NO ;
               }
               else iDelOldData = RxTableInstallLogic.DELETE_NO ;
            }
            else iDelOldData = RxTableInstallLogic.DELETE_NO ;


            System.out.println("Schema/TableName = " + m_qualifiedName);

            // don't think this needs to be qualified
            // m_qualifiedPkeyName = dbmsDef.getQualifiedIdentifier("pk_" + m_name);
            m_qualifiedPkeyName = dbmsDef.fixIdentifierCase("pk_" + m_name);
            m_qualifiedFkeyName = dbmsDef.fixIdentifierCase("fk_" + m_name);

            // load the column defs
            if (w.getNextElement("row", getChild) == null)
            {
               throw new IllegalArgumentException(
                  "row definition section not found in table definition file.");
            }

            int colNo = 0;
            for(  Element e = w.getNextElement("column", getChild);
                     e != null;
                     e = w.getNextElement("column", getSibling) )
            {
               ColumnDefinition colDef = new ColumnDefinition(dbmsDef, w);
               m_columns.add(colDef);
               m_columnPos.put(colDef.getColumnName(), new Integer(++colNo));
            }

            // load the primary key def (may not have one)
            w.setCurrent(eTableDef);
            if (w.getNextElement("primarykey", getChild) != null)
            {
               for(  Element e = w.getNextElement("name", getChild);
                     e != null;
                     e = w.getNextElement("name", getSibling) )
               {
                  m_pKey.add(w.getElementData(".", false));
               }
            }

            // load the foreign key def (may not have one)
            w.setCurrent(eTableDef);
            if (w.getNextElement("foreignkey", getChild) != null)
            {
               for(  Element e = w.getNextElement("fkColumn", getChild);
                     e != null;
                     e = w.getNextElement("fkColumn", getSibling) )
               {
                  Element fCol = null;
                  Element fExtTable = null;
                  Element fExtCol = null;
                  fCol = w.getNextElement("name", getChild);
                  if (fCol != null)
                     fExtTable = w.getNextElement("externalTable", getSibling);
                  if (fExtTable != null)
                     fExtCol = w.getNextElement("externalColumn", getSibling);

                  if (fExtCol != null)
                  {
                     String[] fkInfo = new String[3];
                     fkInfo[0] = w.getElementData(fCol);
                     fkInfo[1] = w.getElementData(fExtTable);
                     fkInfo[2] = w.getElementData(fExtCol);
                     m_fKey.add(fkInfo);
                  }
               }
            }

            // load the index defs (may not have any)
            w.setCurrent(eTableDef);
            if (w.getNextElement("indexdefinitions", getChild) != null)
            {
               for(  Element e = w.getNextElement("index", getChild);
                        e != null;
                        e = w.getNextElement("index", getSibling) )
               {
                  m_indices.add(new IndexDefinition(dbmsDef, m_name, w));
               }
            }
         }
         finally
         {
         }
      }

      public String getSelectStatement()
      {
         StringBuffer buf = new StringBuffer();
         buf.append("SELECT * FROM ");
         buf.append(m_qualifiedName);
         return buf.toString();
      }

      public String getAddColumnStatement(RxColumns addColumn)
      {
          StringBuffer buf = new StringBuffer();

         buf.append("ALTER TABLE ");
         buf.append(m_qualifiedName);

         buf.append(" ADD ");
          buf.append(addColumn.getColumnDef());

         return buf.toString();
      }


      public String getInsertStatement()
      {
         StringBuffer buf = new StringBuffer();

         buf.append("INSERT INTO ");
         buf.append(m_qualifiedName);

         buf.append(" (");
         for (int i = 0; i < m_columns.size(); i++)
         {
            if (i > 0)
               buf.append(", ");
            buf.append(((ColumnDefinition)m_columns.get(i)).getColumnName());
         }
         buf.append(")");

         buf.append(" VALUES (");
         for (int i = 0; i < m_columns.size(); i++)
         {
            if (i > 0)
               buf.append(", ?");
            else
               buf.append("?");
         }
         buf.append(")");

         return buf.toString();
      }

      public String getCreateTableStatement(boolean includePrimaryKey)
      {
         StringBuffer buf = new StringBuffer();
         String fullDbName = new String();

         buf.append("CREATE TABLE ");
         // DEBUG

         if(cDB.getDataBase()== null && cDB.getSchema()!=null)
         {
            fullDbName = cDB.getSchema()+ "."+m_name ;
         }
         else if(cDB.getDataBase()!= null && cDB.getSchema()!=null)
         {
            fullDbName = cDB.getDataBase()+ "." +
                        cDB.getSchema()+"." + m_name;
         }
         buf.append(fullDbName);
         buf.append(" (");

         for (int i = 0; i < m_columns.size(); i++)
         {
            if (i > 0)
               buf.append(", ");
            buf.append(((ColumnDefinition)m_columns.get(i)).getColumnDef());
         }

         if (includePrimaryKey && (m_pKey.size() > 0))
         {
            buf.append(", CONSTRAINT ");
            buf.append(m_qualifiedPkeyName);
            buf.append(" PRIMARY KEY (");
            for (int i = 0; i < m_pKey.size(); i++)
            {
               if (i > 0)
                  buf.append(", ");
               buf.append((String)m_pKey.get(i));
            }
            buf.append(")");
         }

         buf.append(")");

         return buf.toString();
      }

      public String getCopyTableStatement(RxTables rxTable, String strBackupTable)
      {
         StringBuffer buf = new StringBuffer();
          String strFullSource = new String();
          String strFullTarget = new String();
          if(cDB.getDataBase()== null && cDB.getSchema()!=null)
         {
            strFullSource = cDB.getSchema()+ "."+rxTable.getTableName() ;
            strFullTarget = cDB.getSchema()+ "."+strBackupTable ;
         }
         else if(cDB.getDataBase()!= null && cDB.getSchema()!=null)
         {
            strFullSource = cDB.getDataBase()+ "." +
                        cDB.getSchema()+"." + rxTable.getTableName();
           strFullTarget = cDB.getDataBase()+ "." +
                        cDB.getSchema()+"." + strBackupTable;
         }

         buf.append("INSERT INTO ");
         buf.append(strFullTarget);
         buf.append(" (");
         for (int i = 0; i < rxTable.vColumns.size(); i++)
         {
            if (i > 0)
               buf.append(", ");

              buf.append(((RxColumns)rxTable.vColumns.get(i)).getColName());
         }

         buf.append(") ");
         buf.append("SELECT ");

         for (int i = 0; i < rxTable.vColumns.size(); i++)
         {
            if (i > 0)
               buf.append(", ");

              buf.append(((RxColumns)rxTable.vColumns.get(i)).getColName());
         }

         buf.append(" FROM ");
         buf.append(strFullSource);

         return buf.toString();
      }

      public String getCreateTableStatement(RxTables rxTable)
      {
         StringBuffer buf = new StringBuffer();
         String fullDbName = new String();

         buf.append("CREATE TABLE ");
         // DEBUG

         if(cDB.getDataBase()== null && cDB.getSchema()!=null)
         {
            fullDbName = cDB.getSchema()+ "."+rxTable.getTableName() ;
         }
         else if(cDB.getDataBase()!= null && cDB.getSchema()!=null)
         {
            fullDbName = cDB.getDataBase()+ "." +
                        cDB.getSchema()+"." + rxTable.getTableName();
         }
         buf.append(fullDbName);
         buf.append(" (");

          for (int i = 0; i < rxTable.vColumns.size(); i++)
         {
            if (i > 0)
               buf.append(", ");

              buf.append(((RxColumns)rxTable.vColumns.get(i)).getColumnDef());
         }

         buf.append(")");

         return buf.toString();
      }

      public String getAddPrimaryKeyStatement()
      {
         StringBuffer buf = new StringBuffer();

         buf.append("ALTER TABLE ");
         buf.append(m_qualifiedName);
         buf.append(" ADD CONSTRAINT ");
         buf.append(m_qualifiedPkeyName);
         buf.append(" PRIMARY KEY (");
         for (int i = 0; i < m_pKey.size(); i++)
         {
            if (i > 0)
               buf.append(", ");
            buf.append((String)m_pKey.get(i));
         }
         buf.append(")");

         return buf.toString();
      }

      public String getAddPrimaryKeyStatement(Vector vKeys, RxTables rxTable)
         throws SQLException
      {
         StringBuffer buf = new StringBuffer();

         String qualifiedName = "";
          if(cDB.getDataBase()== null && cDB.getSchema()!=null)
         {
           qualifiedName = cDB.getSchema()+ "."+rxTable.getTableName() ;
         }
         else if(cDB.getDataBase()!= null && cDB.getSchema()!=null)
         {
           qualifiedName = cDB.getDataBase()+ "." +
                       cDB.getSchema()+"." + rxTable.getTableName();
         }

         String qualifiedPkeyName =
            m_dbmsDef.fixIdentifierCase("pk_" + rxTable.getTableName());

          buf.append("ALTER TABLE ");
         buf.append(qualifiedName);
         buf.append(" ADD CONSTRAINT ");
         buf.append(qualifiedPkeyName);
         buf.append(" PRIMARY KEY (");

         for (int i = 0; i < vKeys.size(); i++)
         {
            if (i > 0)
               buf.append(", ");
            buf.append((String)vKeys.get(i));
         }
         buf.append(")");

         return buf.toString();
      }

      /**
       * Creates an array of statements for each foreign key constraint.
       *
       * @return The array of statements, never <code>null</code>, may be
       * empty.
       */
      public String[] getAddForeignKeyStatements()
      {
         int size = m_fKey.size();
         String[] stmts = new String[size];
         StringBuffer buf = new StringBuffer();

         for (int i = 0; i < size; i++)
         {
            String[] fkeyInfo = (String[])m_fKey.get(i);

            buf.setLength(0);
            buf.append("ALTER TABLE ");
            buf.append(m_qualifiedName);
            buf.append(" ADD CONSTRAINT ");
            buf.append(m_qualifiedFkeyName);
            buf.append("_");
            buf.append(String.valueOf(i+1));
            buf.append(" FOREIGN KEY (");
            buf.append(fkeyInfo[0]);
            buf.append(") REFERENCES ");
            buf.append(fkeyInfo[1]);
            buf.append(" (");
            buf.append(fkeyInfo[2]);
            buf.append(")");

            stmts[i] = buf.toString();
         }

         return stmts;
      }

      public String getGrantAccesStatement()
      {
         return "GRANT SELECT, INSERT, UPDATE, DELETE ON "
            + m_qualifiedName + " TO public";
      }

      public String getGrantAccesStatement(RxTables rxTable)
      {
         String qualifiedName = "";
          if(cDB.getDataBase()== null && cDB.getSchema()!=null)
         {
            qualifiedName = cDB.getSchema()+ "."+rxTable.getTableName() ;
         }
         else if(cDB.getDataBase()!= null && cDB.getSchema()!=null)
         {
            qualifiedName = cDB.getDataBase()+ "." +
                        cDB.getSchema()+"." + rxTable.getTableName();
         }

          return "GRANT SELECT, INSERT, UPDATE, DELETE ON "
            + qualifiedName + " TO public";
      }


      public String getDropTableStatement()
      {
//       System.out.println("Executing: " + "DROP TABLE " + m_qualifiedName);

         return "DROP TABLE " + m_qualifiedName;
      }

      public String[] getCreateIndexStatements()
      {
         String[] ret = new String[m_indices.size()];

         for (int i = 0; i < m_indices.size(); i++)
         {
            ret[i]
               = ((IndexDefinition)m_indices.get(i)).getCreateIndexStatement();
         }

         return ret;
      }

      private String processTableData(DbmsDefinition dbmsDef,
                                      RxTables rxTable,
                                      Document docTableData,
                                      Element eList)
      {
         Vector locRxTables = new Vector();
         Vector vtSqlStmt = new Vector();
         Vector vtSqlList = new Vector();
         Vector vtColumns = new Vector();

         WfSqlCreate locColumns = new WfSqlCreate();
         String sTableName = new String();
         String sColumnName = new String();
         String sAction = new String();
         String pLast = new String();
         boolean actionFlag = false;
         int eIndex = 0;
         String sStatus = new String();
         RxTables locTable = new RxTables();

         PSXmlTreeWalker walkerTableData = null;
         locRxTables = cDB.RxTables;
         boolean bDelete = false;

         try
         {
            PSXmlTreeWalker xmlDataDoc = new PSXmlTreeWalker(docTableData);

            eList = xmlDataDoc.getNextElement(false);
            NamedNodeMap atts = null;

            if (eList.getTagName().equalsIgnoreCase("tables"))
            {
               eList = xmlDataDoc.getNextElement(false);
            }

            if (eList.getTagName().equalsIgnoreCase("table"))
            {
               String sCurrentTable = eList.getAttribute("name");
               //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
               sCurrentTable = sCurrentTable.trim();
               while (eList != null &&
                     sCurrentTable.equalsIgnoreCase(m_name) == false)
               {
                  eList = xmlDataDoc.getNextElement("table", getSibling);
                  sCurrentTable = eList.getAttribute("name");
               }
               sTableName = sCurrentTable;

               for (eIndex = 0; eIndex < locRxTables.size(); eIndex++)
               {
                  if (locTable == null)
                     locTable = new RxTables();
                  locTable = (RxTables) locRxTables.elementAt(eIndex);
                  if (locTable.getTableName().equalsIgnoreCase(sTableName))
                     break;
               }

               if (eIndex >= locRxTables.size())
               {
                  sStatus = "Error : Table " + sTableName + " not found in column chain";
                  System.out.println(sStatus);
               }

               eList = xmlDataDoc.getNextElement(false);
               if (eList == null)
               {
                  sStatus = "";
                  return sStatus;
               }
               for (atts = eList.getAttributes();
                    eList != null &&
                     eList.getTagName().equalsIgnoreCase("table") == false;)
               {
                  atts = eList.getAttributes();
                  String sTagName = eList.getTagName().trim();
                  sTagName = sTagName.toLowerCase();
                  if (sTagName.equals("row") == true)
                  {
                     if (atts.getLength() > 0)
                     {
                        sAction = atts.item(0).getNodeValue();
                     }
                     else
                        sAction = "N"; // Update if not Insert
                     eList = xmlDataDoc.getNextElement(false);
                     pLast = "row";

                     actionFlag = true;
                  }
                  else if (sTagName.startsWith("del"))
                  {
                     // delete that block
                     bDelete = true;
                     sAction = "delete";
                     eList = xmlDataDoc.getNextElement(false);
                     while (eList.getTagName().equalsIgnoreCase("row") == true)
                     {
                        eList = xmlDataDoc.getNextElement(false);
                     }
                     do
                     {
                        if (vtColumns == null)
                           vtColumns = new Vector();
                        while (eList != null &&
                              eList.getTagName().equalsIgnoreCase("column") == true &&
                              (eList.getTagName().equalsIgnoreCase("row") == false ||
                              eList.getTagName().equalsIgnoreCase("table") == false))
                        {
                           atts = eList.getAttributes();
                           String attName = atts.getNamedItem("name").getNodeValue();
                           String attValue = xmlDataDoc.getElementData(eList);
                           if (locColumns == null)
                              locColumns = new WfSqlCreate();
                           locColumns.setColValue(attValue);
                           locColumns.setColName(attName);
                           vtColumns.add(locColumns);
                           locColumns = null;
                           pLast = "column";
                           eList = xmlDataDoc.getNextElement(false);
                        }
                        sStatus = processSqlStmt(locTable,
                              vtColumns,
                              sAction,
                              dbmsDef);
                        vtColumns = null;
                        String stmpTagParent = eList.getParentNode().getNodeName();
                        if (stmpTagParent.toLowerCase().equals("delete") == false)
                           break;
                        String sTmpTagName = eList.getTagName();
                        if (sTmpTagName.toLowerCase().equals("row") == true)
                           eList = xmlDataDoc.getNextElement(false);
                     }
                     while (eList != null &&
                           eList.getTagName().equalsIgnoreCase("table") == false);

//                     return sStatus ;
                  }

                  if (eList != null &&
                        (eList.getTagName().equalsIgnoreCase("column")) == true &&
                        sAction.equalsIgnoreCase("delete") == false)
                  {
                     if (vtColumns == null)
                        vtColumns = new Vector();
                     while (eList != null &&
                           (eList.getTagName().equalsIgnoreCase("table") == false) &&
                           (eList.getTagName().equalsIgnoreCase("row") == false) &&
                           (eList.getTagName().equalsIgnoreCase("delete") == false))
                     {
                        atts = eList.getAttributes();
                        String sName = atts.item(0).getNodeValue();
                        String sValue = xmlDataDoc.getElementData(eList);
                        if (locColumns == null)
                           locColumns = new WfSqlCreate();
                        locColumns.setColValue(sValue);
                        locColumns.setColName(sName);
                        vtColumns.add(locColumns);
                        locColumns = null;
                        pLast = "column";
                        eList = xmlDataDoc.getNextElement(false);
                     }

                     if (pLast.equalsIgnoreCase("column") == true)
                     {
                        if (sAction.charAt(0) == 'N' ||
                              sAction.charAt(0) == 'n' ||
                              sAction.charAt(0) == 'U' ||
                              sAction.charAt(0) == 'u' ||
                              sAction.charAt(0) == 'r' ||
                              sAction.charAt(0) == 'R')
                        {
                           sStatus = processSqlStmt(locTable,
                                 vtColumns,
                                 sAction,
                                 dbmsDef);
                           vtColumns = new Vector();
                           if (sStatus != null &&
                                 sStatus.length() > 0)
                           {
                              sStatus = "Error Processing SQL Statement on table " +
                                    locTable.getTableName() + "\n";
                              String sDataError = new String();
                              for (int i = 0; i < vtColumns.size(); i++)
                              {
                                 RxColumns wfErrorColumn = (RxColumns) vtColumns.elementAt(i);
                                 sDataError = sDataError +
                                       wfErrorColumn.getColName() +
                                       "\t" +
                                       wfErrorColumn.getColValue() +
                                       "\n";
                              }
                              sStatus = sStatus + sDataError;
                              m_logger.logMessage(sStatus);
                              System.out.println(sStatus);
                           }
                        }
                        else
                        {
                           sStatus = "Unknow Action Flag " + sAction +
                                 "\n\tPossible Actions are :\n" +
                                 "\t\tN for New or Insert\n" +
                                 "\t\tU for Update\n" +
                                 "\t\tD for Delete or\n" +
                                 "\t\tR for Update Check then if no " +
                                 "Records found then it will Insert\n";
                           System.out.println(sStatus);
                           m_logger.logMessage(sStatus);
                        }
                     }
                  }
               }
               if (eIndex >= locRxTables.size())
               {
                  sStatus = m_name + "Table not found in Table Chain\n";
                  m_logger.logMessage(sStatus);
                  System.out.println(sStatus);
               }
            }
         } catch (Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }
         return sStatus;
      } // parseTableData()


      private String processSqlStmt(RxTables wfLocalTable,
                                    Vector vtColumns,
                                    String sAction,
                                    DbmsDefinition dbmsDef)
      {
         System.out.println("processSqlStmt");
         String sSqlStmt = new String();
         WfSqlCreate newColumns = new WfSqlCreate();
         Connection cConn = null;
         String sTime = new String();
         String sDate = new String();
         String sTimeStamp = new String();
         String sPrefix = new String();
         String sStatus = new String();

         String sLocalDb = wfLocalTable.getDataBase();
         String sLocalSchema = wfLocalTable.getSchema();
         if (sLocalDb != null && sLocalDb.length() > 0 &&
               sLocalSchema != null && sLocalSchema.length() > 0)
         {
            sPrefix = wfLocalTable.getDataBase() + "." +
                  wfLocalTable.getSchema() + "." +
                  wfLocalTable.getTableName();
         }
         else if (wfLocalTable.getSchema() != null &&
               wfLocalTable.getSchema().length() > 0)
         {
            sPrefix = wfLocalTable.getSchema() + "." +
                  wfLocalTable.getTableName();
         }
         else
         {
            sPrefix = wfLocalTable.getTableName();
         }

         Vector vtNewColumns = reOrderColumns(wfLocalTable,
               vtColumns);
         try
         {
            cConn = dbmsDef.getConnection();
         } catch (SQLException e)
         {
            System.out.println("\nConnection Error\n");
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            sAction = "";
            sStatus = "Connection Error; failed to insert";
         }

         int i = 0;
         sAction = sAction.toUpperCase();
         if (sAction.length() > 0)
         {
            switch (sAction.charAt(0))
            {
               case 'N':
                  // System.out.println("Inserting Record into "+wfLocalTable.getTableName());
                  sStatus = wfInsertRecord(wfLocalTable, vtColumns,
                        sAction, dbmsDef);
                  try
                  {
                     int iRet = Integer.parseInt(sStatus);
                     if (iRet > 0)
                     {
                        // System.out.println("\t" + sStatus + " Records Inserted");
                        sStatus = "";
                        return sStatus;
                     }
                     else if (iRet == 0)
                     {
                        System.out.println("No record inserted in " + wfLocalTable.getTableName());
                        sStatus = "";
                        return sStatus;
                     }
                  } catch (Exception e)
                  {
                      log.error(e.getMessage());
                      log.debug(e.getMessage(), e);
                     sStatus = e.getMessage();
                     return sStatus;
                  }
                  break;
               case 'U':
                  System.out.println("Updating Record in " + wfLocalTable.getTableName());
                  sStatus = wfUpdateRecord(wfLocalTable, vtColumns,
                        sAction, dbmsDef);
                  try
                  {
                     int iRet = Integer.parseInt(sStatus);
                     if (iRet > 0)
                     {
                        System.out.println("\t" + sStatus + " Record Updated in " + wfLocalTable.getTableName());
                        sStatus = "";
                        return sStatus;
                     }
                     else if (iRet == 0)
                     {
                        System.out.println("No record updated in " + wfLocalTable.getTableName());
                        sStatus = "";
                        return sStatus;
                     }
                  } catch (Exception e)
                  {
                      log.error(e.getMessage());
                      log.debug(e.getMessage(), e);
                     sStatus = e.getMessage();
                     return sStatus;
                  }
                  break;
               case 'D':
                  System.out.println("Deleting Record for " + wfLocalTable.getTableName());
                  sStatus = wfDeleteRecord(wfLocalTable, vtColumns,
                        sAction, dbmsDef);
                  try
                  {
                     int iRet = Integer.parseInt(sStatus);
                     if (iRet > 0)
                     {
                        System.out.println("\t" + sStatus + " Record(s) Deleted in table " + wfLocalTable.getTableName());
                        sStatus = "";
                        return sStatus;
                     }
                     else if (iRet == 0)
                     {
                        System.out.println("No record deleted in " + wfLocalTable.getTableName());
                        sStatus = "";
                        return sStatus;
                     }
                  } catch (Exception e)
                  {
                      log.error(e.getMessage());
                      log.debug(e.getMessage(), e);
                     sStatus = e.getMessage();
                     return sStatus;
                  }
                  break;
               case 'R':
                  sStatus = "";
                  System.out.println("Update/Insert Record in " + wfLocalTable.getTableName());
                  sStatus = wfUpdateRecord(wfLocalTable, vtColumns,
                        sAction, dbmsDef);
                  int iRet = 0;
                  try
                  {
                     iRet = Integer.parseInt(sStatus);
                     if (iRet > 0)
                     {
                        System.out.println("\t" + sStatus + " Record(s) Updated");
                        sStatus = "";
                        return sStatus;
                     }
                     else if (iRet == 0)
                     {
                        sStatus = "";
                        sStatus = wfInsertRecord(wfLocalTable, vtColumns,
                              sAction, dbmsDef);
                        iRet = Integer.parseInt(sStatus);
                        if (iRet > 0)
                        {
                           System.out.println("\t" + sStatus + " Record Inserted");
                           sStatus = "";
                           return sStatus;
                        }
                        else if (iRet == 0)
                        {
                           System.out.println("\t" + sStatus + " Record Inserted");
                           sStatus = "";
                           return sStatus;
                        }
                     }
                  } catch (Exception e)
                  {
                      log.error(e.getMessage());
                      log.debug(e.getMessage(), e);
                     System.out.println("Error:" + "\n\t" + sStatus);
                  }
                  break;
               default:
                  break;
            } // Switch
         } // If Action
         return sStatus;
      }

      // Insertion of new data
      String wfInsertRecord(RxTables wfLocalTable,
                            Vector vtColumns,
                            String sAction,
                            DbmsDefinition dbmsDef)
      {
         String sSqlStmt = new String();
         WfSqlCreate newColumns = new WfSqlCreate();
         PreparedStatement pStmt = null;
         Connection cConn = null;
         String sTime = "";
         String sDate = "";
         String sTimeStamp = "";
         String sPrefix = "";
         String sStatus = "";

         String sInsertColumns = new String();
         String sInsertValues = new String();

         sSqlStmt = "";
         sInsertColumns = "";
         sInsertValues = "";

         if (wfLocalTable.getDataBase() != null &&
               wfLocalTable.getDataBase().length() > 0 &&
               wfLocalTable.getSchema() != null &&
               wfLocalTable.getSchema().length() > 0)
         {
            sPrefix = wfLocalTable.getDataBase() + "." +
                  wfLocalTable.getSchema() + "." +
                  wfLocalTable.getTableName();
         }
         else if (wfLocalTable.getSchema() != null &&
               wfLocalTable.getSchema().length() > 0)
         {
            sPrefix = wfLocalTable.getSchema() + "." +
                  wfLocalTable.getTableName();
         }
         else
         {
            sPrefix = wfLocalTable.getTableName();
         }

         Vector vtNewColumns = reOrderColumns(wfLocalTable,
               vtColumns);

         for (int i = 0; i < vtNewColumns.size(); i++)
         {
            if (newColumns == null)
               newColumns = new WfSqlCreate();
            newColumns = (WfSqlCreate) vtNewColumns.elementAt(i);
            sInsertColumns = sInsertColumns + sPrefix + "." +
                  newColumns.getColName() + ",";
            sInsertValues = sInsertValues + "?,";
            newColumns = null;
         }

         sInsertColumns = sInsertColumns.substring(0, (sInsertColumns.length()) - 1);
         sInsertValues = sInsertValues.substring(0, (sInsertValues.length()) - 1);
         sSqlStmt = "INSERT INTO " + sPrefix + " (" +
               sInsertColumns + ") " + "VALUES(" +
               sInsertValues + ")";
//System.out.println("\nInserts Statement :\n" + sSqlStmt);
         try
         {
            cConn = dbmsDef.getConnection();
            pStmt = PSPreparedStatement.getPreparedStatement(cConn, sSqlStmt);
            /*
             * TODO: It is inefficient to use a prepareStatement for only
             * one row of data.  Should be re-designed to reuse this statement
             * for all rows
             */

            // bind columns
            for (int i = 1; i <= vtNewColumns.size(); i++)
            {
               newColumns = null;
               newColumns = new WfSqlCreate();
               newColumns = (WfSqlCreate) vtNewColumns.elementAt(i - 1);
               pStmt = setSqlStmtData(pStmt, newColumns, i, dbmsDef);
            }

            int intResult = pStmt.executeUpdate();
            sStatus = Integer.toString(intResult);

         } catch (SQLException e)
         {
            sStatus = e.getMessage();
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            System.out.println("Error Inserting:\n" + e.getMessage());
            return sStatus;
         } finally
         {
            // fix too many open cursors Oracle bug
            try
            {
               if (null != pStmt) pStmt.close();
            } catch (SQLException e)
            {
               // ignore it
            }
         }

         return sStatus;
      }

      String wfUpdateRecord(RxTables wfLocalTable,
                           Vector   vtColumns,
                           String   sAction,
                           DbmsDefinition dbmsDef)
      {
         String   sSqlStmt = new String();
         WfSqlCreate  newColumns = new WfSqlCreate();
         String sUpdateColumns   = new String();
         String sUpdateValues    = new String();
         String sKeyColName      = new String();
         String sKeyColData      = new String();
         String sKeyColValue     = new String();
         String sKeyColDataType  = new String();
         PreparedStatement pStmt = null ;
         Connection        cConn = null ;
         String            sTime = new String();
         String            sDate = new String();
         String       sTimeStamp = new String();
         String          sPrefix = new String();
         String          sStatus = new String();

         String sLocalDb = wfLocalTable.getDataBase();
         String sLocalSchema = wfLocalTable.getSchema();
         if(sLocalDb != null && sLocalDb.length()>0 &&
               sLocalSchema != null && sLocalSchema.length() > 0)
         {
            sPrefix = wfLocalTable.getDataBase() + "." +
                     wfLocalTable.getSchema() + "." +
                     wfLocalTable.getTableName() ;
         }
         else if(sLocalSchema != null && sLocalSchema.length() > 0)
         {
            sPrefix = wfLocalTable.getSchema()+"."+
                     wfLocalTable.getTableName() ;
         }
         else
         {
            sPrefix = wfLocalTable.getTableName() ;
         }

         Vector vtNewColumns = reOrderColumns(wfLocalTable,
                                             vtColumns);

         try
         {
            cConn = dbmsDef.getConnection();
         }
         catch (SQLException e)
         {
            System.out.println("\nConnection Error\n");
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }

         newColumns = null ;
         newColumns = new WfSqlCreate();
         newColumns = (WfSqlCreate)vtColumns.elementAt(0);
         sKeyColName = newColumns.getColName();
         sKeyColValue = newColumns.getColValue();
         sKeyColDataType = newColumns.getColDataType().toUpperCase();

         int i = 0 ;
         for(i = 1 ; i < vtColumns.size() ; i++)
         {
            newColumns = null ;
            newColumns = new WfSqlCreate();
            newColumns = (WfSqlCreate)vtNewColumns.elementAt(i);
            sUpdateColumns = sUpdateColumns + sPrefix + "." +
                              newColumns.getColName()+ "=?," ;
            newColumns = null ;
         }
         if(vtColumns.size() == 1)
         {
            newColumns = null ;
            newColumns = new WfSqlCreate();
            newColumns = (WfSqlCreate)vtColumns.elementAt(0);
            sUpdateColumns = sUpdateColumns + sPrefix + "." +
                              newColumns.getColName() + "=?,";
            newColumns = null ;
         }
         sUpdateColumns =
                  sUpdateColumns.substring(0,sUpdateColumns.length()-1);
         sUpdateColumns = " SET " + sUpdateColumns ;
         newColumns = null ;
         newColumns = new WfSqlCreate();
         newColumns = (WfSqlCreate)vtColumns.elementAt(0);
         sSqlStmt = "UPDATE " + sPrefix + sUpdateColumns + " WHERE " +
                        sPrefix + "." + sKeyColName +"=?" ;

//System.out.println("\nUpdate Statement:\n" + sSqlStmt);
         try
         {
            pStmt = PSPreparedStatement.getPreparedStatement(cConn, sSqlStmt);
            for(i=1 ; i<vtNewColumns.size() ; i++)
            {
               WfSqlCreate valColumn = new WfSqlCreate();
               valColumn = (WfSqlCreate)vtNewColumns.elementAt(i);
               pStmt = setSqlStmtData(pStmt,valColumn,i, dbmsDef);

            }
            if( vtNewColumns.size() == 1)
            {
               WfSqlCreate valColumn = new WfSqlCreate();
               valColumn = (WfSqlCreate)vtNewColumns.elementAt(0);
               pStmt = setSqlStmtData(pStmt,valColumn,1, dbmsDef);
            }
            // Set Key Column
            if(sKeyColValue !=null && sKeyColValue.length() > 0)
            {
               if(vtNewColumns.size() == 1)
               {
                  i = 2 ;
               }
               if(sKeyColDataType.equalsIgnoreCase("INTEGER"))
               {
                  pStmt.setInt(i , Integer.parseInt(sKeyColValue));
               }
               else if(sKeyColDataType.equalsIgnoreCase("VARCHAR")||
                        sKeyColDataType.equalsIgnoreCase("CHAR")||
                        sKeyColDataType.equalsIgnoreCase("LONGVARCHAR"))
               {
                  pStmt.setString(i ,sKeyColValue);
               }
               else if(sKeyColDataType.equalsIgnoreCase("TINYINT"))
               {
                  pStmt.setByte(i,(byte)(Integer.parseInt(sKeyColValue)));
               }
               else if(sKeyColDataType.equalsIgnoreCase("SMALLINT"))
               {
                  pStmt.setShort(i,(short)(Integer.parseInt(sKeyColValue)));
               }
               else if(sKeyColDataType.equalsIgnoreCase("BIGINT"))
               {
                  pStmt.setLong(i,(Long.getLong(sKeyColValue)).longValue());
               }
               else if(sKeyColDataType.equalsIgnoreCase("FLOAT"))
               {
                  pStmt.setFloat(i,Float.parseFloat(sKeyColValue));
               }
               else if(sKeyColDataType.equalsIgnoreCase("DOUBLE"))
               {
                  pStmt.setDouble(i,Double.parseDouble(sKeyColValue));
               }
            }
            else
            {
               // TODO == Error Condition
System.out.println("NULL Values not allowed as Update Key");
            }
         }
         catch(SQLException e)
         {
            sStatus = e.getMessage();
            System.out.println("Error setting Update SQL Statement");
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            try
            {
              pStmt.close();
            }
            catch (SQLException fix) {}
            return sStatus ;
         }

         int intResult = 0 ;
         try
         {
            intResult = pStmt.executeUpdate();
         }
         catch(SQLException e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            System.out.println("Error Executing SQL Statement:\n" + e.getMessage());
            sStatus = e.getMessage();
            try
            {
              pStmt.close();
            }
            catch (SQLException fix) {}
            return sStatus ;
         }

         try
         {
           pStmt.close();
         }
         catch (SQLException fix) {}
         sStatus = Integer.toString(intResult);
         return sStatus ;
      }

      // Delete the Record
      String wfDeleteRecord(RxTables wfLocalTable,
                           Vector   vtColumns,
                           String   sAction,
                           DbmsDefinition dbmsDef)
      {
         String   sSqlStmt   = new String();
         String sKeyColName  = new String();
         String sKeyColValue = new String();
         WfSqlCreate  newColumns = new WfSqlCreate();
         PreparedStatement pStmt = null ;
         Connection        cConn = null ;
         String            sTime = new String();
         String            sDate = new String();
         String       sTimeStamp = new String();
         String          sPrefix = new String();
         String          sStatus = new String();

         if(wfLocalTable.getDataBase()!= null &&
                wfLocalTable.getDataBase().length() > 0 &&
                wfLocalTable.getSchema()!= null &&
                wfLocalTable.getSchema().length() > 0)
         {
            sPrefix = wfLocalTable.getDataBase() + "." +
                     wfLocalTable.getSchema() + "." +
                     wfLocalTable.getTableName() ;
         }
         else if(wfLocalTable.getSchema()!=null &&
                  wfLocalTable.getSchema().length() > 0)
         {
            sPrefix = wfLocalTable.getSchema()+"."+
                     wfLocalTable.getTableName() ;
         }
         else
         {
            sPrefix = wfLocalTable.getTableName() ;
         }

         Vector vtNewColumns = reOrderColumns(wfLocalTable,
                                             vtColumns);

         try
         {
            cConn = dbmsDef.getConnection();
         }
         catch (SQLException e)
         {
            System.out.println("\nConnection Error\n");
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }

         int i = 0 ;

         try
         {
            sSqlStmt = "" ;
            sKeyColName = "";
            sKeyColValue = "" ;
            newColumns = new WfSqlCreate();
            newColumns = (WfSqlCreate)vtNewColumns.elementAt(0);
            sKeyColName = newColumns.getColName();
            sKeyColValue = newColumns.getColValue();
            sSqlStmt = "DELETE FROM " + sPrefix + " WHERE " +
                           sPrefix + "." + sKeyColName + "=?" ;
//System.out.println(sSqlStmt);
            pStmt = PSPreparedStatement.getPreparedStatement(cConn, sSqlStmt);

            if(newColumns.getColDataType().equalsIgnoreCase("INTEGER"))
            {
               pStmt.setInt(1,Integer.parseInt(sKeyColValue));
            }
            else if(newColumns.getColDataType().equalsIgnoreCase("VARCHAR") ||
                     newColumns.getColDataType().equalsIgnoreCase("CHAR") ||
                     newColumns.getColDataType().equalsIgnoreCase("LONGVARCHAR"))
            {
               pStmt.setString(1,sKeyColValue);
            }
         }
         catch(SQLException e)
         {
            sStatus = e.getMessage();
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            System.out.println("\nError on Delete Option:\n" +e.getMessage());
            try
            {
              pStmt.close();
            }
            catch (SQLException fix) {}
            return sStatus ;
         }

         int intResult = 0 ;
         try
         {
            intResult = pStmt.executeUpdate();
         }
         catch(SQLException e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            System.out.println("Error Executing SQL Statement:\n" + e.getMessage());
            sStatus = e.getMessage();
            try
            {
              pStmt.close();
            }
            catch (SQLException fix) {}
            return sStatus ;
         }
         sStatus = Integer.toString(intResult);
         try
         {
           pStmt.close();
         }
         catch (SQLException fix) {}
         return sStatus ;
      }

      private PreparedStatement setSqlStmtData(PreparedStatement pStmt,
                                                WfSqlCreate wfColumn,
                                                int wfIndex,
                                                DbmsDefinition dbmsDef)
      {
         try
         {
            if(wfColumn.getColDataType().equalsIgnoreCase("VARCHAR"))
            {
               if(wfColumn.getColValue().length() > 0)
                  pStmt.setString((wfIndex),wfColumn.getColValue());
               else pStmt.setNull((wfIndex),Types.VARCHAR);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("CHAR"))
            {
               if(wfColumn.getColValue().length() > 0)
                  pStmt.setString((wfIndex),wfColumn.getColValue());
               else pStmt.setNull((wfIndex),Types.CHAR);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("CLOB"))
            {
               if(wfColumn.getColValue().length() > 0)
                  pStmt.setString((wfIndex),wfColumn.getColValue());
               else pStmt.setNull((wfIndex),Types.CLOB);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("LONGVARCHAR"))
            {
               //in sql server we are getting right truncation errors
               if(dbmsDef.m_sBackend.equals("MSSQL"))
               {
                  System.out.println("using unicode stream for LONGVARCHAR");
                  StringBufferInputStream in = new StringBufferInputStream(wfColumn.getColValue());

                  if(wfColumn.getColValue().length() > 0)
                      pStmt.setAsciiStream(wfIndex,
                          in,
                          in.available());

                   else pStmt.setNull(wfIndex,Types.LONGVARCHAR);
               }
               else
               {
                   if(wfColumn.getColValue().length() > 0)
                      pStmt.setString(wfIndex,wfColumn.getColValue());
                   else pStmt.setNull(wfIndex,Types.LONGVARCHAR);
               }
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("INTEGER"))
            {
               if(wfColumn.getColValue().length() > 0)
                  pStmt.setInt(wfIndex,
                        Integer.parseInt(wfColumn.getColValue()));
               else pStmt.setNull((wfIndex),Types.INTEGER);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("DATE"))
            {
               if(wfColumn.getColValue().length() > 0)
               {
                  String sDate = wfColumn.getColValue();

                  //mssql not liking the date binding
                  if(dbmsDef.m_sBackend.equals("MSSQL"))
                  {
                      pStmt.setString(wfIndex,sDate);
                  }
                  else
                  {
                      StringTokenizer sDateToken = new StringTokenizer(sDate);
                      sDate = sDateToken.nextToken(" ");
                      java.sql.Date dDate = java.sql.Date.valueOf(sDate);
                      pStmt.setDate(wfIndex,dDate);
                  }
               }
               else

               {
                  //mssql not liking the date binding
                  if(dbmsDef.m_sBackend.equals("MSSQL"))
                  {
                      pStmt.setString(wfIndex,"");
                  }
                  else
                    pStmt.setNull(wfIndex,Types.DATE);
               }
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("TIME"))
            {
               if(wfColumn.getColValue().length() > 0)
               {
                  String sTime = wfColumn.getColValue();
                  java.sql.Time dTime = java.sql.Time.valueOf(sTime);
                  pStmt.setTime(wfIndex,dTime);
               }
               else pStmt.setNull(wfIndex,Types.TIME) ;
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("TIMESTAMP"))
            {
               if(wfColumn.getColValue().length() > 0)
               {
                  String sTimeStamp = wfColumn.getColValue();
                  StringTokenizer sTimeStampToken = new StringTokenizer(sTimeStamp);
                  String sDate = sTimeStampToken.nextToken(" ");
                  String sTime = sTimeStampToken.nextToken();
                  sTimeStamp = sDate + " " + sTime + "0000000";
                  Timestamp tStamp = Timestamp.valueOf(sTimeStamp);
                  pStmt.setTimestamp(wfIndex,tStamp) ;
               }
               else pStmt.setNull(wfIndex,Types.TIMESTAMP);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("LONGVARBINARY"))
            {
               if(wfColumn.getColValue().length() > 0)
               {
                  String lvarValue = wfColumn.getColValue() ;
                  byte[] obValue = getBinaryFromBase64(lvarValue);
                  pStmt.setObject(wfIndex , obValue , Types.LONGVARBINARY);
               }
               else pStmt.setNull(wfIndex,Types.LONGVARBINARY);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("VARBINARY"))
            {
               if(wfColumn.getColValue().length() > 0)
               {
                  String lvarValue = wfColumn.getColValue() ;
                  byte[] obValue = getBinaryFromBase64(lvarValue);
                  pStmt.setObject(wfIndex , obValue , Types.VARBINARY);
               }
               else pStmt.setNull(wfIndex,Types.VARBINARY);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("BINARY"))
            {
               if(wfColumn.getColValue().length() > 0)
               {
                  String lvarValue = wfColumn.getColValue() ;
                  byte[] obValue = getBinaryFromBase64(lvarValue);
                  pStmt.setObject(wfIndex , obValue , Types.BINARY);
               }
               else pStmt.setNull(wfIndex,Types.BINARY);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("BIT"))
            {
               String bValue =  wfColumn.getColValue();
               pStmt.setBoolean(wfIndex,
                  (bValue.equals("1") || bValue.equalsIgnoreCase("true")) );
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("DOUBLE") ||
               wfColumn.getColDataType().equalsIgnoreCase("DECIMAL") ||
               wfColumn.getColDataType().equalsIgnoreCase("NUMERIC"))
            {
               pStmt.setDouble(wfIndex, Double.parseDouble(
                  wfColumn.getColValue()));
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("FLOAT"))
            {
               pStmt.setObject(wfIndex, Float.valueOf(wfColumn.getColValue()),
                  Types.FLOAT);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("REAL"))
            {
               pStmt.setObject(wfIndex, Float.valueOf(wfColumn.getColValue()),
                  Types.REAL);
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("TINYINT"))
            {
               pStmt.setByte(wfIndex, Byte.parseByte(wfColumn.getColValue()));
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("SMALLINT"))
            {
               pStmt.setShort(wfIndex, Short.parseShort(wfColumn.getColValue()));
            }
            else if(wfColumn.getColDataType().equalsIgnoreCase("BIGINT"))
            {
               pStmt.setLong(wfIndex, Long.parseLong(wfColumn.getColValue()));
            }
            else
            {
               // try to get the type and set it
               try
               {
                  Integer jType = new Integer(
                     java.sql.Types.class.getField(
                        wfColumn.getColDataType()).getInt(null));

                  pStmt.setObject(wfIndex, wfColumn.getColValue(),
                     jType.intValue());
               }
               catch(java.lang.NoSuchFieldException e)
               {
                  System.out.println("Unsupported data type: " +
                     wfColumn.getColDataType());
               }
               catch(java.lang.IllegalAccessException e)
               {
                  System.out.println("Unsupported data type: " +
                     wfColumn.getColDataType());
               }
            }

         }
         catch(SQLException e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }

         return pStmt ;
      } // SetSqlStemData

      private Vector reOrderColumns(RxTables wfLocalTable,
                                    Vector  vtColumns)
      {
         Vector  vtOrderedColumns    = new Vector();  // Return Param
         Vector  vtTabColumns        = new Vector();

         vtTabColumns = wfLocalTable.vColumns;

         for(int i=0; i< vtTabColumns.size() ; i++)
         {
            RxColumns RxColumns = new RxColumns();
            RxColumns = (RxColumns)vtTabColumns.elementAt(i);
            String colName = RxColumns.getColName();
            for(int j =0 ; j< vtColumns.size() ; j++)
            {
               WfSqlCreate locColumns = new WfSqlCreate();
               locColumns = (WfSqlCreate)vtColumns.elementAt(j);
               if(locColumns.getColName().equalsIgnoreCase(colName) == true)
               {
                  locColumns.setColDataType(RxColumns.getJdbcDataType());
                  vtOrderedColumns.add(locColumns);
                  break;
               }
               locColumns = null ;
            }
         }

         return vtOrderedColumns ;
      }


      //////////// Dropping, Creating the tables  /////////
      public String generateTable(DbmsDefinition dbmsDef,
                                  Element elementData,
                                  RxTables rxTable)
            throws SQLException
      {
         Connection conn = dbmsDef.getConnection();
         Statement stmt = PSSQLStatement.getStatement(conn);
         String s = null;
         String sStatus = new String();

         try
         {
            String oldDataFileName = new String();
            RxTables orgTable = new RxTables();
            boolean bExist = false;
            sqlTransactLog sqlLog = new sqlTransactLog();
            String targetDir = new String();

            // 1. Check for table existance
            orgTable = checkUserTable(dbmsDef, m_qualifiedName, m_name);
            if (orgTable == null)
               bExist = false;
            else
               bExist = true;

            if (orgTable != null && orgTable.sError.length() > 0)
            {
               sStatus = orgTable.sError;
               stmt.close();
               return sStatus;
            }

            int iRowCount = 0;

            Document xmlDoc = cDB.dataDoc;


            ////////////////////////////////
            // Table works as followed :
            //          Yes  No  No Action
            //          ===  ==  =========
            // CREATE    1    2     0
            // ALTER     1    2     0
            // DELETE    1    2     0
            ////////////////////////////////
            if (iCreateFlag == RxTableInstallLogic.CREATE_YES ||
                  iCreateFlag == RxTableInstallLogic.CREATE_NODROP)
            {
               // Table Exists
               if (bExist == true &&
                     iCreateFlag != RxTableInstallLogic.CREATE_NODROP)
               {
                  iRowCount = getRowCount(dbmsDef, m_qualifiedName);
                  if (iRowCount > 0 && iDelOldData == RxTableInstallLogic.DELETE_YES)
                  {
                     // Drop Old Table, Delete Data, Create new table
                     try
                     {
                        s = getDropTableStatement();
                        stmt.execute(s);

                        s = getCreateTableStatement(false);
                        System.out.println("Executing: " + s);
                        stmt.close();
                        stmt = PSSQLStatement.getStatement(conn);
                        stmt.execute(s);

                        s = getGrantAccesStatement();
                        // System.out.println("Executing: " + s);
                        stmt.close();
                        stmt = PSSQLStatement.getStatement(conn);
                        stmt.execute(s);

                        s = createPrimaryKey(m_pKey, stmt);
                        if (s != null && s.length() > 0)
                        {
                           stmt.close();
                           stmt = PSSQLStatement.getStatement(conn);
                           stmt.execute(s);
                        }

                        createForeignKeys(conn, stmt);

                     } catch (SQLException e)
                     {
                         log.error(e.getMessage());
                         log.debug(e.getMessage(), e);
                     }
                     sStatus = processTableData(dbmsDef, rxTable,
                           cDB.dataDoc,
                           elementData);
                  }
                  else if (iRowCount > 0 && iDelOldData == RxTableInstallLogic.DELETE_NO)
                  {
                     //has the table changed?
                     //check rxTable against orgTable
                     //rxtable is from the xml def
                     //orgTable is from the sql db
                     Vector vColumnActions = rxTable.compareTables(orgTable);
                     boolean bAnyNonUser = RxJdbcTableFactory.anyNonUser(vColumnActions);
                     if (bAnyNonUser)
                     {
                        //we have table changes, but do we have only adds?
                        boolean bOnlyAdds = RxJdbcTableFactory.onlyAdds(vColumnActions);
                        if (bOnlyAdds)
                        {
                           System.out.println("found only column adds for " +
                                 rxTable.getTableName());

                           for (int iAddCol = 0; iAddCol < vColumnActions.size();
                                ++iAddCol)
                           {
                              RxColumns addCol = (RxColumns)
                                    vColumnActions.get(iAddCol);

                              s = getAddColumnStatement(addCol);
                              System.out.println("Executing: " + s);
                              stmt.execute(s);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);
                           }
                        }
                        else
                        {
                           System.out.println("recreating table " +
                                 rxTable.getTableName());

                           //first backup the table
                           String strBackupTable = rxTable.getTableName() + "_BACKUP";

                           //drop the backup if exits
                           if (tableExists(dbmsDef, strBackupTable))
                           {
                              stmt.execute("DROP TABLE " + strBackupTable);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);
                           }

                           //create the backup table
                           String strOrigTableName =
                                 rxTable.getTableName();

                           String strQualBackupName = new String();
                           if (cDB.getDataBase() == null && cDB.getSchema() != null)
                           {
                              strQualBackupName = cDB.getSchema() + "." + strBackupTable;
                           }
                           else if (cDB.getDataBase() != null && cDB.getSchema() != null)
                           {
                              strQualBackupName = cDB.getDataBase() + "." +
                                    cDB.getSchema() + "." + strBackupTable;
                           }

                           //orgTable is created from the existing table
                           //in the SQL database.
                           orgTable.setTableName(strBackupTable);
                           s = getCreateTableStatement(orgTable);
                           System.out.println("Executing " + s);
                           stmt.execute(s);
                           s = getGrantAccesStatement(orgTable);
                           System.out.println("Executing " + s);
                           stmt.close();
                           stmt = PSSQLStatement.getStatement(conn);
                           System.out.println("Executing " + s);
                           stmt.execute(s);
                           s = createPrimaryKey(orgTable, stmt);
                           System.out.println("Executing " + s);
                           if (s != null && s.length() > 0)
                           {
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);
                           }

                           orgTable.setTableName(strOrigTableName);

                           //copy the data into the backup table
                           s = getCopyTableStatement(orgTable, strBackupTable);
                           System.out.println("Executing " + s);
                           stmt.execute(s);
                           stmt.close();
                           stmt = PSSQLStatement.getStatement(conn);

                           //verify the copy
                           RxTables rxBackupTable =
                                 checkUserTable(dbmsDef,
                                       strQualBackupName,
                                       strBackupTable);

                           //check the table design
                           Vector vDiffs = orgTable.compareTables(rxBackupTable);
                           if (vDiffs.size() > 0)
                           {
                              return "Table upgrade aborted. Copying of backup failed to copy table design.";
                           }

                           //check the data
                           int iBackupRows = getRowCount(dbmsDef, strQualBackupName);
                           int iOrigRowCount = getRowCount(dbmsDef, m_qualifiedName);
                           if (iBackupRows != iOrigRowCount)
                           {
                              return "Table upgrade aborted. Copying of backup failed to copy rows. " +
                                    "Original table had " + new Integer(iOrigRowCount).toString() +
                                    " rows but the backup row count is " +
                                    new Integer(iBackupRows).toString();
                           }

                           boolean bUpgradeOk = true;
                           try
                           {
                              //if we made it this far we can drop the original table
                              s = getDropTableStatement();
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);

                              //now create the new table
                              //will create the new table based on our xml def
                              s = getCreateTableStatement(false);
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              s = getGrantAccesStatement();
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);
                              stmt.execute(s);
                              s = createPrimaryKey(m_pKey, stmt);
                              if (s != null && s.length() > 0)
                              {
                                 stmt.close();
                                 stmt = PSSQLStatement.getStatement(conn);
                                 stmt.execute(s);
                              }
                              createForeignKeys(conn, stmt);

                              //now add the user defined columns that we do not know
                              //about
                              for (int iColAction = 0; iColAction
                                    < vColumnActions.size();
                                   ++iColAction)
                              {
                                 RxColumns col = (RxColumns)
                                       vColumnActions.get(iColAction);

                                 if (col.getColAction() ==
                                       RxColumns.USER_COLUMN)
                                 {
                                    s = getAddColumnStatement(col);
                                    System.out.println("Executing: " + s);
                                    stmt.execute(s);
                                    stmt.close();
                                    stmt = PSSQLStatement.getStatement(conn);
                                 }
                              }

                              //now move the data from the backup

                              /* we should not fail the table creation if
                               * the data from the backup can not be moved
                               * back.  This can happen if we add keys to a
                               * table.
                               */
                              try
                              {
                                 s = getCopyTableStatement(rxBackupTable, rxTable.getTableName());
                                 System.out.println("Executing " + s);
                                 stmt.execute(s);
                                 stmt.close();

                                 stmt = PSSQLStatement.getStatement(conn);

                                 //verify the data
                                 iBackupRows = getRowCount(dbmsDef, strQualBackupName);
                                 iOrigRowCount = getRowCount(dbmsDef, m_qualifiedName);
                                 if (iBackupRows != iOrigRowCount)
                                 {
                                    sStatus = "Table upgrade aborted. Copying of backup failed to copy rows. " +
                                          "Original table had " + new Integer(iOrigRowCount).toString() +
                                          " rows but the backup row count is " +
                                          new Integer(iBackupRows).toString();
                                    bUpgradeOk = false;
                                 }
                              }
                              catch(SQLException sqle)
                              {
                                  log.error(sqle.getMessage());
                                  log.debug(sqle.getMessage(), sqle);
                                 bUpgradeOk = true;
                              }
                           }
                           catch (SQLException e)
                           {
                               log.error(e.getMessage());
                               log.debug(e.getMessage(), e);
                              sStatus = e.getMessage();
                              bUpgradeOk = false;
                           }

                           if (!bUpgradeOk)
                           {
                              System.out.println("Restoring backup.");

                              //drop whats there
                              s = getDropTableStatement();
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);

                              //create the table again
                              s = getCreateTableStatement(orgTable);
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              s = getGrantAccesStatement(orgTable);
                              System.out.println("Executing " + s);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              s = createPrimaryKey(orgTable, stmt);
                              if (s != null && s.length() > 0)
                              {
                                 stmt.close();
                                 stmt = PSSQLStatement.getStatement(conn);
                                 System.out.println("Executing " + s);
                                 stmt.execute(s);
                                 stmt.close();
                                 stmt = PSSQLStatement.getStatement(conn);
                              }

                              //copy the data from the backup table
                              s = getCopyTableStatement(rxBackupTable, orgTable.getTableName());
                              System.out.println("Executing " + s);
                              stmt.execute(s);
                              stmt.close();
                              stmt = PSSQLStatement.getStatement(conn);

                              //verify the copy
                              //check the table design
                              vDiffs = orgTable.compareTables(rxBackupTable);
                              if (vDiffs.size() > 0)
                              {
                                 return "Table backup restoration aborted. Copying of backup failed to copy table design.";
                              }

                              //check the data
                              iBackupRows = getRowCount(dbmsDef, strQualBackupName);
                              iOrigRowCount = getRowCount(dbmsDef, m_qualifiedName);
                              if (iBackupRows != iOrigRowCount)
                              {
                                 return "Table backup restoration aborted. Copying of backup failed to copy rows. " +
                                       "Original table had " + new Integer(iOrigRowCount).toString() +
                                       " rows but the backup row count is " +
                                       new Integer(iBackupRows).toString();
                              }
                           }

                           //TODO: option to delete backup

                        }
                     }
                     else
                        System.out.println("no table changes needed for " +
                              rxTable.getTableName());

                     /*
                     // Save old Data in an XML Document
                     sqlLog = storeOldData(dbmsDef,orgTable);
                     if(sqlLog.errorMessage.length() == 0)
                     {
                        try
                        {
                           s = getDropTableStatement();
                           stmt.execute(s);
                           s = getCreateTableStatement(false);
   //                      System.out.println("Executing: " + s);
                           stmt.execute(s);
                           s = getGrantAccesStatement();
   //                      System.out.println("Executing: " + s);
                           stmt.execute(s);
                           s = createPrimaryKey(m_pKey,stmt);
                           if(s != null && s.length() > 0)
                           {
                              stmt.execute(s);
                           }
                           sqlLog = insertOldData(dbmsDef,m_name,iRowCount);
                        }
                        catch(Exception e)
                        {
                           // One of the SQL Statements failed.
                           // Store Data in the XML Doc.
                           System.out.println("Exception encountered: "+
                                             "Storing old data in XML File\n");
                           log.error(e.getMessage());
                           log.debug(e.getMessage(), e);
                        }
                        // TODO == This is other way == change later
                        if(sqlLog.errorMessage.length() == 0)
                        {
                           sStatus = deleteFile(oldDataFileName,targetDir);
                        }
                        sStatus = processTableData(dbmsDef,tableDef,
                                                   cDB.dataDoc,
                                                   elementData);
                     }*/
                  }
                  else if (iRowCount == 0)
                  {
                     s = getDropTableStatement();
                     stmt.execute(s);
                     stmt.close();
                     stmt = PSSQLStatement.getStatement(conn);

                     s = getCreateTableStatement(false);
                     stmt.execute(s);

                     s = getGrantAccesStatement();
                     stmt.close();
                     stmt = PSSQLStatement.getStatement(conn);
                     stmt.execute(s);

                     s = createPrimaryKey(m_pKey, stmt);
                     if (s != null && s.length() > 0)
                     {
                        stmt.close();
                        stmt = PSSQLStatement.getStatement(conn);
                        stmt.execute(s);
                     }
                     createForeignKeys(conn, stmt);

                     sStatus = processTableData(dbmsDef, rxTable,
                           cDB.dataDoc,
                           elementData);
                  }
               } // if bExist == true
               else if (!bExist)
               {
                  // If the table doesn't exist
                  s = getCreateTableStatement(false);
                  System.out.println("Executing " + s);
                  stmt.execute(s);

                  s = getGrantAccesStatement();
                  stmt.close();
                  stmt = PSSQLStatement.getStatement(conn);
                  stmt.execute(s);

                  s = createPrimaryKey(m_pKey, stmt);
                  if (s != null && s.length() > 0)
                  {
                     stmt.close();
                     stmt = PSSQLStatement.getStatement(conn);
                     stmt.execute(s);
                  }

                  createForeignKeys(conn, stmt);

                  sStatus = processTableData(dbmsDef, rxTable,
                        cDB.dataDoc,
                        elementData);
               }
            }
            else if (iCreateFlag == RxTableInstallLogic.CREATE_NO)
            {
               if (bExist == true)
               {
                  try
                  {
                     sStatus = processTableData(dbmsDef, rxTable,
                           cDB.dataDoc,
                           elementData);
                  } catch (Exception e)
                  {
                      log.error(e.getMessage());
                      log.debug(e.getMessage(), e);
                  }
               } // if bExist
               else if (bExist == false) // TODO : Should we create and insert ?
               {
                  // user has given wrong info. We won't or insert Data create
                  System.out.println("Table does not exist. Please check the XML tags." +
                        "\nSkipping Table");
               }
            } // else if CraeteFlag == 2

            String[] indices = getCreateIndexStatements();
            for (int i = 0; i < indices.length; i++)
            {
               System.out.println("Executing: " + indices[i]);
               stmt.execute(indices[i]);
            }

         }
         finally
         {
            if (stmt != null)
               try{stmt.close();} catch(Exception c){}
         }

         return sStatus;
      } // generateTable


      // Creation of Primary Keys
      String createPrimaryKey(List key,Statement stStmt)
      {
         String sStmt = new String();

         if (key.size() > 0)
         {
            try
            {
               sStmt = getAddPrimaryKeyStatement();
//System.out.println("Executing: " + sStmt);
//             stStmt.execute(sStmt);
            }
            catch (Exception e)
            {
              log.error(e.getMessage());
              log.debug(e.getMessage(), e);
            }
         }

         return sStmt ;
      }

// Craetion of Primary Keys
      String createPrimaryKey(RxTables rxTable,Statement stStmt)
      {
         String sStmt = new String();

         Vector vKeys = new Vector();
         for(int iCol = 0; iCol < rxTable.vColumns.size();
               ++iCol)
         {
            RxColumns col = (RxColumns)
               rxTable.vColumns.get(iCol);
              if(col.getKey())
               vKeys.add(col.getColName());
         }

         if (vKeys.size() > 0)
         {
            try
            {
               sStmt = getAddPrimaryKeyStatement(vKeys, rxTable);
//System.out.println("Executing: " + sStmt);
//             stStmt.execute(sStmt);
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
            }
         }

         return sStmt ;
      }

      /**
       * Executes statements required to create foreign key constraints
       * defined for this table.
       *
       * @param conn A valid connection to the database.
       *
       * @param stmt The statement to use.  Assumed not <code>null</code>.
       * Statement is not closed after this method returns (to be consistent
       * with calling logic).
       *
       * @throws SQLException if an error occurs.  No cleanup is performed by
       * this method.
       */
      private void createForeignKeys(Connection conn, Statement stmt)
         throws SQLException
      {
         String[] fKeys = getAddForeignKeyStatements();
         for (int i=0; i < fKeys.length; i++)
         {
            stmt.close();
            stmt = PSSQLStatement.getStatement(conn);
            stmt.execute(fKeys[i]);
         }
      }

      // Checking for table existance
      private RxTables checkUserTable(DbmsDefinition dbmsDef,
                              String qualifiedTableName,
                              String tableName)
      {
         Vector vtColumnList  = new Vector() ; // To make sure we don't delete by mistake
         RxColumns orgColumns = new RxColumns();
         RxTables  orgTable   = new RxTables();
         String sqlStmt       = new String();
         String doubleQuote   = new String();
         String singleQuote   = new String();
         Connection cConn     = null;
         ResultSet rsColumns  = null ;
         ResultSet rsKeys     = null;
         int rowCount = 0 ;
         Vector vKeys = new Vector();

         orgTable.setDataBase(cDB.getDataBase());
         orgTable.setSchema(cDB.getSchema());
         try
         {
            cConn = dbmsDef.getConnection();
            DatabaseMetaData dbMetaData = cConn.getMetaData();
            String sDataBase = new String();
            sDataBase = dbmsDef.getDataBase();
            if(sDataBase != null)
               if(sDataBase.length() == 0)
                  sDataBase = null ;

            String sSchema = orgTable.getSchema();
            if(sSchema != null && sSchema.length() == 0 )
               sSchema = null ;

            try
            {
               if(sDataBase == null && sSchema != null)
               {
                  String sPreferedCatTerm = dbMetaData.getCatalogTerm();
                  orgTable.setSchema(orgTable.getSchema().toUpperCase());
                  rsKeys = dbMetaData.getPrimaryKeys(sPreferedCatTerm,
                                             orgTable.getSchema(),
                                             tableName);


               }
               else if(sDataBase != null && sSchema != null)
               {
                rsKeys = dbMetaData.getPrimaryKeys(orgTable.getDataBase(),
                                                   orgTable.getSchema(),
                                                   tableName);
               }
               else if(sDataBase == null && sSchema == null)
               {
                  rsKeys = dbMetaData.getImportedKeys(sDataBase,sSchema,tableName);
               }
               else
               {
                  orgTable.sError = "Unknown DB Configuration : Don't know how to get MetaData." ;
                  return orgTable ;
               }

               while(rsKeys.next())
               {
                  String sColName = rsKeys.getString("COLUMN_NAME");
                  if(sColName != null)
                     vKeys.add(sColName);
               }
            }
            catch (SQLException sqle)//if the meta data fails the table does not exist
            {
               return null;
            }
            finally
            {
               if (rsKeys != null)
                  try{rsKeys.close();} catch(Exception c){}
            }

            try
            {
               if(sDataBase == null && sSchema != null)
               {
                  String sPreferedCatTerm = dbMetaData.getCatalogTerm();
                  orgTable.setSchema(orgTable.getSchema().toUpperCase());
                  rsColumns = dbMetaData.getColumns(sPreferedCatTerm,
                                             orgTable.getSchema(),
                                             tableName,"%");
               }
               else if(sDataBase != null && sSchema != null)
               {
                  rsColumns = dbMetaData.getColumns(orgTable.getDataBase(),
                                                   orgTable.getSchema(),
                                                   tableName,
                                                   "%");
               }
               else if(sDataBase == null && sSchema == null)
               {
                  rsColumns = dbMetaData.getColumns(sDataBase,sSchema,tableName,"%");
               }
               else
               {
                  orgTable.sError = "Unknown DB Configuration : Don't know how to get MetaData." ;
                  return orgTable ;
               }
            }
            catch (SQLException sqle)//if the meta data fails the table does not exist
            {
               if (rsColumns != null)
                  try{rsColumns.close();} catch(Exception c){}

               return null;
            }

            while(rsColumns.next())
            {
               rowCount++ ;
               String sColName = rsColumns.getString("COLUMN_NAME");
               if(orgColumns == null)
                  orgColumns = new RxColumns() ;
               orgColumns.setColName(sColName);
               String sColType = rsColumns.getString("TYPE_NAME");
               orgColumns.setColNo(rowCount);
               orgColumns.setNativeType(sColType);

               if(vKeys.indexOf(sColName) != -1)
                  orgColumns.setKey(true);

               //int dataType = Integer.parseInt(sColType);

               String strType = m_dbmsDef.getJdbcDataTypeMapping(sColType);
               if(strType != null)
                  orgColumns.setJdbcDataType(strType);

               if(!orgColumns.getJdbcDataType().equalsIgnoreCase("INTEGER") &&
                  !orgColumns.getJdbcDataType().equalsIgnoreCase("DATE")&&
                  !orgColumns.getJdbcDataType().equalsIgnoreCase("TIME")&&
                  !orgColumns.getJdbcDataType().equalsIgnoreCase("TIMESTAMP")&&
                  !orgColumns.getJdbcDataType().equalsIgnoreCase("LONGVARCHAR")&&
                  !orgColumns.getJdbcDataType().equalsIgnoreCase("CLOB")&&
                  !orgColumns.getJdbcDataType().equalsIgnoreCase("BLOB"))
               {
                  String sColSize = rsColumns.getString("COLUMN_SIZE") ;
                  orgColumns.setColLength(Integer.parseInt(sColSize));
               }

               orgColumns.setOrdinalPosition(Integer.parseInt(rsColumns.getString("ORDINAL_POSITION")));
//               int colOrdPosition = orgColumns.getOrdinalPosition();

               String sColNullable = rsColumns.getString("IS_NULLABLE").trim();
               if(sColNullable.equalsIgnoreCase("NO") == true)
               {
                  orgColumns.setAllowNull(false);
               }
               else
               {
                  orgColumns.setAllowNull(true);
               }
               vtColumnList.add(orgColumns);
               orgColumns = null ;
            }
            rsColumns.close();
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            orgTable.sError = "Error retrieving Meta Data on Row " + rowCount ;
            return orgTable;
         }

         if(rowCount == 0)
         {
            orgTable = null ;
         }
         else
         {
            orgTable.columnCount = rowCount ;
            orgTable.setTableName(tableName);
            orgTable.vColumns = vtColumnList ;
         }

         return orgTable ;
      } // checkUserTable

      // Checking for table existance
      private boolean tableExists(DbmsDefinition dbmsDef,
                              String tableName)
      {
         boolean bRet = false;
         Vector vtColumnList  = new Vector() ; // To make sure we don't delete by mistake
         RxColumns orgColumns = new RxColumns();
         RxTables  orgTable   = new RxTables();
         String sqlStmt       = new String();
         String doubleQuote   = new String();
         String singleQuote   = new String();
         Connection cConn     = null;
         ResultSet rsColumns  = null ;
         int rowCount = 0 ;

         orgTable.setDataBase(cDB.getDataBase());
         orgTable.setSchema(cDB.getSchema());
         try
         {
            cConn = dbmsDef.getConnection();
            DatabaseMetaData dbMetaData = cConn.getMetaData();
            String sDataBase = new String();
            sDataBase = dbmsDef.getDataBase();
            if(sDataBase != null)
               if(sDataBase.length() == 0)
                  sDataBase = null ;

            String sSchema = orgTable.getSchema();
            if(sSchema != null && sSchema.length() == 0 )
               sSchema = null ;

            if(sDataBase == null && sSchema != null)
            {
               String sPreferedCatTerm = dbMetaData.getCatalogTerm();
               orgTable.setSchema(orgTable.getSchema().toUpperCase());
               rsColumns = dbMetaData.getColumns(sPreferedCatTerm,
                                          orgTable.getSchema(),
                                          tableName,"%");
            }
            else if(sDataBase != null && sSchema != null)
            {
               rsColumns = dbMetaData.getColumns(orgTable.getDataBase(),
                                                orgTable.getSchema(),
                                                tableName,
                                                "%");
            }
            else if(sDataBase == null && sSchema == null)
            {
               rsColumns = dbMetaData.getColumns(sDataBase,sSchema,tableName,"%");
            }
            else
            {
               orgTable.sError = "Unknown DB Configuration : Don't know how to get MetaData." ;
               return false ;
            }
            if(rsColumns.next())
            {
               bRet = true;
            }
            rsColumns.close();
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            orgTable.sError = "Error retrieving Meta Data on Row " + rowCount ;
            return false ;
         }


         return bRet ;
      } // checkUserTable


      private int getRowCount(DbmsDefinition dbmsDef, String qualifiedName)
      {
         int count = 0 ;

         Connection cConn = null ;
         String sqlStmt = new String();

         sqlStmt = "SELECT COUNT(*) FROM " + qualifiedName ;
         try
         {
            cConn = dbmsDef.getConnection() ;
            Statement stmt = PSSQLStatement.getStatement(cConn);
            ResultSet rsCount = stmt.executeQuery(sqlStmt);

            while(rsCount.next())
            {
               count = rsCount.getInt(1);
               System.out.println(Integer.toString(count) +
                                 " Rows found in " + qualifiedName);
            }
            rsCount.close();
            stmt.close();
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }
         return count ;
      }

      private sqlTransactLog storeOldData(DbmsDefinition dbmsDef,
                                    RxTables orgTable)
      {
         Connection cConn = null ;
         String sqlStmt = new String();
         String prefix  = new String();
         boolean bDataFileExist = false ;
         boolean bDtdFileExist  = false ;
         ResultSet rsResults = null ;
         Statement stmt = null ;
         sqlTransactLog sqlLog = new sqlTransactLog();

         if(orgTable.getDataBase() != null &&
                  orgTable.getDataBase().length() > 0 &&
                  orgTable.getSchema() != null &&
                  orgTable.getSchema().length()>0)
         {
            prefix = orgTable.getDataBase() + "." +
                        orgTable.getSchema() + "." ;
         }
         else if(orgTable.getSchema()!= null &&
                  orgTable.getSchema().length() > 0)
         {
            prefix = orgTable.getSchema() + "." ;
         }
         else prefix = "" ;

         try
         {
            cConn = dbmsDef.getConnection();
            stmt = PSSQLStatement.getStatement(cConn);
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }

         String sColumn = new String();
         Vector vtColumns = new Vector();
         vtColumns = orgTable.vColumns ;
         for(int i = 0 ; i < orgTable.vColumns.size(); i++)
         {
            RxColumns orgColumns = new RxColumns();
            orgColumns = (RxColumns)vtColumns.elementAt(i);
            sColumn = sColumn +"," + prefix +
                           orgTable.getTableName() + "." +
                           orgColumns.getColName();
         }
         sColumn = sColumn.substring(1);
         sqlStmt = "SELECT " + sColumn + " FROM " +
                        prefix + orgTable.getTableName();
//System.out.println(sqlStmt);
         try
         {
            rsResults = stmt.executeQuery(sqlStmt);
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }

         sqlLog = setColumnData(orgTable,rsResults);
         try
         {
           rsResults.close();
           stmt.close();
         }
         catch (SQLException fix) {}
         return sqlLog ;
      } // storeOldData


      private sqlTransactLog setColumnData(RxTables orgTable,
                                          ResultSet rsResults)
      {
         Vector vtOldTables = cDB.wfOldTables ;
         Vector vtOrgColumns = orgTable.vColumns ;
         Vector vtStoredColumns = new Vector();
         RxTables oldTables = new RxTables();
         Vector vtValues = new Vector();
         sqlTransactLog sqlLog = new sqlTransactLog();
         int rowCount = 0 ;

         try
         {
            while(rsResults.next())
            {
               rowCount++ ;
               RxColumns oldColumn = new RxColumns();
               for(int i=0 ;i < vtOrgColumns.size() ; i++)
               {
                  oldColumn = (RxColumns)vtOrgColumns.elementAt(i);
                  String sColValue =
                              rsResults.getString(oldColumn.getColName());
                  if(sColValue == null)
                     sColValue = "" ;
//                  int dataType = rsResults.getMetaData();
                  oldColumn.vtOldValues.add(sColValue);
                  vtOrgColumns.set(i,oldColumn);
               }
               oldColumn = null ;
            }
            rsResults.close();
         }
         catch(Exception e)
         {
            sqlLog.bStatus = false ;
            sqlLog.stmtType = "SELECT : METADATA : Failed to get Old Data\n" +
                              e.getMessage()+"\n"+
                              e.getLocalizedMessage();
            sqlLog.rowCount = rowCount;
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            return sqlLog;
         }
         vtOldTables.add(orgTable);
         cDB.wfOldTables = vtOldTables ;
         sqlLog.bStatus = true ;
         sqlLog.errorMessage = "" ;
         sqlLog.rowCount = rowCount ;
         return sqlLog;
      }


      /// create an Element from a result set ///
      Document createDataElement(Document dataDoc,
                                 RxTables wfTable)
      {
         Element  eData    = null ;
         Element  table    = null ;
         Element  row      = null ;
         Document xmlDoc  = null ;
         Vector   vtColumns = new Vector();
         PSXmlTreeWalker walkerTableDef = null;

         xmlDoc = dataDoc ;
         walkerTableDef = new PSXmlTreeWalker(xmlDoc);

         Element eRow   = null ;
         Element eCol   = null ;
         Element eTable = null ;
         Text text      = null ;
         int i          = 0 ;
         int rowCount   = 0 ;

         Vector vtOldColumns = wfTable.vColumns ;
         RxColumns tmpRxColumns = (RxColumns)vtOldColumns.elementAt(0);
         rowCount = tmpRxColumns.vtOldValues.size();
         try
         {
            Element eDataRoot = xmlDoc.getDocumentElement() ;
            eTable = xmlDoc.createElement("table");
            eTable.setAttribute("name",wfTable.getTableName());
            for(int j=0 ; j < rowCount ; j++)
            {
               eRow = xmlDoc.createElement("row");
               Element eColumn = null ;
               for(i=0 ; i<vtOldColumns.size();i++)
               {
                  RxColumns wfColumn  = (RxColumns)vtOldColumns.elementAt(i);
                  eColumn = xmlDoc.createElement("column");
                  eColumn.setAttribute("name",wfColumn.getColName());
                  String sColValue = (String)wfColumn.vtOldValues.elementAt(j);
                  if(wfColumn.getJdbcDataType().equalsIgnoreCase("LONGVARBINARY") &&
                     sColValue != null && sColValue.length() > 0)
                  { // TODO == Ram ????
                     sColValue = new String(Base64.getMimeDecoder().decode(sColValue.getBytes(StandardCharsets.UTF_8)),StandardCharsets.UTF_8);
                  }
                  eColumn.appendChild(xmlDoc.createTextNode(sColValue));
                  eRow.appendChild(eColumn);
                  eColumn = null ;
                  wfColumn = null ;
               }
               eTable.appendChild(eRow);
            }
            eDataRoot.appendChild(eTable);
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }

         return dataDoc ;
      }

      // Create an Element from a Table Name and Column Vector
      Document createDtdElement(Document dtdDoc,
                                 String tableName,
                                 Vector vtColumns)
      {
         Element eDtd = null ;
         Document xmlDoc = dtdDoc ;

         try
         {
            Element eDtdRoot = xmlDoc.getDocumentElement() ;
            Element eTable = xmlDoc.createElement("table");
            eTable.setAttribute("name",tableName);
            Element eRow = xmlDoc.createElement("row");

            Element eColumn = null ;
            for(int i=0 ; i<vtColumns.size();i++)
            {
               RxColumns column  = (RxColumns)vtColumns.elementAt(i);
               eColumn = xmlDoc.createElement("column");
               eColumn.setAttribute("name",column.getColName());
               Element eDataType = xmlDoc.createElement("jdbctype");
               eDataType.appendChild(xmlDoc.createTextNode(column.getJdbcDataType()));
               Element eParams   = xmlDoc.createElement("params");
               if(column.getJdbcDataType().equalsIgnoreCase("INTEGER") == false)
                  eParams.appendChild(xmlDoc.createTextNode(Integer.toString(column.getColLength())));
               Element eAllowNull= xmlDoc.createElement("allownull");
               if(column.getAllowNull() == false)
                  eAllowNull.appendChild(xmlDoc.createTextNode("no"));
               else
                  eAllowNull.appendChild(xmlDoc.createTextNode("yes"));
               eColumn.appendChild(eDataType);
               eColumn.appendChild(eParams);
               eColumn.appendChild(eAllowNull);
               eRow.appendChild(eColumn);
               column = null ;
            }
            eTable.appendChild(eRow);
            eDtdRoot.appendChild(eTable);
//            xmlDoc.appendChild(eDtdRoot);
//            cDB.eDtd = eDtdRoot ;
//            cDB.oldDtdDoc = xmlDoc ;
         }
         catch(Exception e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
         }
         return xmlDoc ;  // TODO  == Change this later - not required
      }

      /////// Insert old Data into table ////
      private sqlTransactLog insertOldData(DbmsDefinition dbmsDef,
                                    String sTableName, int iRowCount)
      {
         sqlTransactLog sqlLog = new sqlTransactLog();
         String sSQLInsert = new String();
         RxTables oldTable = new RxTables();
         Connection cConn  = null ;
         String sPrefix  = new String();
         Vector vtColumns = new Vector();

         Vector vtOldTables  = cDB.wfOldTables ;

         // TODO ==  Replace TableName with NewTableName
         Vector vtTables     = cDB.RxTables ;

         for(int i=0 ; i<vtOldTables.size() ; i++)
         {
            oldTable = (RxTables)vtOldTables.elementAt(i);
            if(oldTable.getTableName().equalsIgnoreCase(sTableName) == true)
            {
               vtColumns = oldTable.vColumns ;
               String sColumns = new String();
               if(oldTable.getDataBase() !=null &&
                        oldTable.getDataBase().length() > 0 &&
                        oldTable.getSchema() != null &&
                        oldTable.getSchema().length() > 0)
               {
                  sPrefix = oldTable.getDataBase() + "." +
                              oldTable.getSchema() + "." +
                                 oldTable.getTableName();
               }
               else if(oldTable.getSchema() != null &&
                        oldTable.getSchema().length() > 0)
               {
                  sPrefix = oldTable.getSchema()+"."+
                              oldTable.getTableName() ;
               }
               else
               {
                  sPrefix = oldTable.getTableName() ;
               }
               String qMarks = new String();
               for(int j = 0 ; j < vtColumns.size() ; j++)
               {
                  RxColumns oldColumn = new RxColumns();
                  oldColumn = (RxColumns)vtColumns.elementAt(j);
                  sColumns = sColumns + "," + sPrefix + "." +
                                       oldColumn.getColName();
                  qMarks = qMarks + ",?" ;
                  oldColumn = null ;
               }
               sColumns = sColumns.substring(1);
               qMarks = qMarks.substring(1);

               sSQLInsert = "INSERT INTO " + sPrefix +
                                 " (" + sColumns +
                                 ") VALUES(" + qMarks + ")" ;
//System.out.println(sSQLInsert);
               break;
            }
         }

         Vector vtOldColumns = oldTable.vColumns ;
         try
         {
            cConn = dbmsDef.getConnection();
            cConn.setAutoCommit(false);  // Trun off Auto Commit
            for(int i=0 ; i < iRowCount ; i++)
            {
               PreparedStatement pStmt = PSPreparedStatement
                     .getPreparedStatement(cConn, sSQLInsert);
               for(int j =0 ; j<oldTable.vColumns.size();j++)
               {
                  RxColumns oldDataColumn = (RxColumns)vtOldColumns.elementAt(j);
                  String sOldColValue = (String)oldDataColumn.vtOldValues.elementAt(i);
//System.out.println(oldDataColumn.getColName()+
//                  "[" + oldDataColumn.getColNo() +
//                  "]= "+sOldColValue);

                  pStmt = setOldStmtData(pStmt,oldDataColumn ,
                                       sOldColValue,
                                       j+1);
                                 //    oldDataColumn.getColNo());
                  if(pStmt == null)
                     break;
               }
               if(pStmt == null)
                  break;
               else
               {
                  pStmt.executeUpdate();
                  pStmt.close();
               }
            }
            cConn.commit();
            cConn.setAutoCommit(true);
         }
         catch(SQLException e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            sqlLog.errorMessage =
                  "Error Inserting Data into " + oldTable.getTableName()+
                                    "\n\tStoring DATA and DTD in XML file";
            System.out.println(sqlLog.errorMessage);
            try
            {
               cConn.rollback();
               cConn.setAutoCommit(true);
               Document dtdXMLDoc  = cDB.oldDtdDoc ;
               Document dataXMLDoc = cDB.oldDataDoc ;
               dtdXMLDoc = createDtdElement(dtdXMLDoc,sTableName,vtColumns);
               dataXMLDoc = createDataElement(dataXMLDoc,oldTable);
            }
            catch(Exception en){
                log.error(en.getMessage());
                log.debug(en.getMessage(), e);
            }
            return sqlLog ;
         }

         return sqlLog ;
      } // InsertOldData


      // Return Stmt according to column
      private PreparedStatement setOldStmtData(PreparedStatement pStmt,
                                                RxColumns wfColumn,
                                                String sValue,
                                                int wfIndex)
      {
         try
         {
            if(wfColumn.getJdbcDataType().equalsIgnoreCase("VARCHAR"))
            {
               if(sValue != null  && sValue.length() > 0)
               {
                  pStmt.setString((wfIndex),sValue);
               }
               else pStmt.setNull((wfIndex),Types.VARCHAR);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("CHAR"))
            {
               if(sValue != null  && sValue.length() > 0)
                  pStmt.setString((wfIndex),sValue);
               else pStmt.setNull((wfIndex),Types.CHAR);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("LONGVARCHAR"))
            {
               if(sValue != null  && sValue.length() > 0)
                  pStmt.setString(wfIndex,sValue);
               else pStmt.setNull(wfIndex,Types.LONGVARCHAR);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("INTEGER"))
            {
               if(sValue != null  && sValue.length() > 0)
                  pStmt.setInt(wfIndex, Integer.parseInt(sValue));
               else pStmt.setNull((wfIndex),Types.INTEGER);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("DECIMAL"))
            {
               if(sValue != null  && sValue.length() > 0)
                  pStmt.setInt(wfIndex, Integer.parseInt(sValue));
               else pStmt.setNull((wfIndex),Types.DECIMAL);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("NUMERIC"))
            {
               if(sValue != null  && sValue.length() > 0)
                  pStmt.setInt(wfIndex, Integer.parseInt(sValue));
               else pStmt.setNull((wfIndex),Types.NUMERIC);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("DATE"))
            {
               if(sValue != null  && sValue.length() > 0)
               {
                  String sDate = sValue;
                  StringTokenizer sDateToken = new StringTokenizer(sDate);
                  sDate = sDateToken.nextToken(" ");
                  java.sql.Date dDate = java.sql.Date.valueOf(sDate);
                  pStmt.setDate(wfIndex,dDate);
               }
               else pStmt.setNull(wfIndex,Types.DATE);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("TIMESTAMP"))
            {
               if(sValue != null  && sValue.length() > 0)
               {
                  String sTimeStamp = sValue;
                  StringTokenizer sTimeStampToken = new StringTokenizer(sTimeStamp);
                  String sDate = sTimeStampToken.nextToken(" ");
                  String sTime = sTimeStampToken.nextToken();
                  sTimeStamp = sDate + " " + sTime + "0000000";
                  Timestamp tStamp = Timestamp.valueOf(sTimeStamp);
                  pStmt.setTimestamp(wfIndex,tStamp) ;
               }
               else pStmt.setNull(wfIndex,Types.TIMESTAMP);
            }
            else if(wfColumn.getJdbcDataType().equalsIgnoreCase("LONGVARBINARY"))
            {
               if(sValue != null  && sValue.length() > 0)
               {
                  String lvarValue = sValue ;
                  byte[] obValue = getBinaryFromBase64(lvarValue);
                  pStmt.setObject(wfIndex , obValue , Types.LONGVARBINARY);
               }
               else pStmt.setNull(wfIndex,Types.LONGVARBINARY);
            }
         }
         catch(SQLException e)
         {
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
            pStmt = null ;
         }

         return pStmt ;
      } // SetOldStmtData

      /** Deletes a file in the target Directory.
      */
      String deleteFile(String dataFileName,String targetDir)
      {
         String sStatus = new String();

         return sStatus;
      }

      /**
      * Writes a Docuemnt to the execution directory
      * or if the full path name is provided, it will
      * write ir to that directory. Various flags can
      * be passed which indicate what to do if the file
      * name already exist in the target directory.
      * CREATE_NEW_OVERWRITE = Create new file.
      *                       Overwrite the old file.
      * CREATE_NEW_OLD = Create new, rename old file
      *                   with the next index of the file
      *                   name available.
      */
      public String fileSaveXMLDocument(Document dDataDoc,
                                       String sFileName,int writeFlag)
      {
         // These are redefined so we are not dependent
         // on the RxTableInstallLogic
         final int CREATE_NEW_OVERWRITE = 0 ;
         final int CREATE_NEW_OLD       = 1 ;

         String sStatus = new String();
         PSXmlDocumentBuilder fileDoc = new PSXmlDocumentBuilder();
         File fOldFile = new File(sFileName);

         // Check for write security
         if(!fOldFile.canRead())
         {
            System.out.println(sFileName +
                           " can not be read or does not exist\n" +
                           "This is either a new installation in this\n" +
                           "directory or you do not have permission\n" +
                           "to read  ... Try to create file\n");
         }

         switch(writeFlag)
         {
            case CREATE_NEW_OVERWRITE :
               try
               {
                  FileOutputStream fOutDataFile =
                                          new FileOutputStream(sFileName);
                  fileDoc.write(dDataDoc,fOutDataFile);
                  fOutDataFile.close();
               }
               catch(Exception e)
               {
                   log.error(e.getMessage());
                   log.debug(e.getMessage(), e);
               }
               break;
            case CREATE_NEW_OLD :
               if(fOldFile.exists())
               { // rename the file
                  String oldIndexFileName = reNameFileExt(sFileName);
                  if(oldIndexFileName == null)
                  {
                     System.out.println("Error renaming the " + sFileName);
                  }
                  else
                  {
                     File fOldIndexFile = new File(oldIndexFileName);
                     fOldFile.renameTo(fOldIndexFile);
                  }
               }
               try
               {
                  FileOutputStream fOutDataFile =
                                       new FileOutputStream(sFileName);
                  fileDoc.write(dDataDoc,fOutDataFile);
                  fOutDataFile.close();
               }
               catch(Exception e)
               {
                   log.error(e.getMessage());
                   log.debug(e.getMessage(), e);
               }
               break;
            default :
               // TODO == Error Condition.
               sStatus = "Error : Creating " + sFileName + "file\n";
               break;
         }
         return sStatus ;
      }

      /**
      * Cretaes a new file which is a copy of the
      * file name passed and changes the extension
      * with the next vailable index number. eg the
      * filename "ABCD.xml" will be copied to ABCD.n
      * where n is a value from 0 to 9. The index is
      * decided by the availabilty of the file if
      * ABCD.0 exists then the name will be ABCD.1 and so on.
      */
      String reNameFileExt(String sFileName)
      {
         StringTokenizer sNameToken = new StringTokenizer(sFileName);
         String sName = sNameToken.nextToken(".");
         String sRenamedFile = new String();
         int i = 0 ;
         for (i = 0 ; i< 99 ; i++)
         {
            File fNewFile = new File(sName + "." + Integer.toString(i));
            if(fNewFile.exists()) continue;
            else break ;
         }

         if(i >= 99)
         {
            sRenamedFile = null;
         }
         else
         {
            sRenamedFile = sName + "." + Integer.toString(i);
         }
         return sRenamedFile ;
      }


      // Write a Docuemnt to target Dir. If Target Dir is length 0
      // or NULL then it will write to the Execution Directory.
      // CREATE_NEW_OVERWRITE = Create new file. Overwrite the old file.
      // CREATE_NEW_OLD = Create new, rename old file with ".old" ext if exists
      // APPEND_CREATE = Append to file. Create file if doesn't exist
      // APPEND = Append to existing file. Fail if doen't exist.
      private int writeTODisk(Document dXMLDoc,String fileName,
                              String targetDir, int iFlag)
      {
         int iRet = 0 ;

         return iRet;
      }


   public void loadData(DbmsDefinition dbmsDef, Element elementData)
         throws java.sql.SQLException, java.lang.IllegalArgumentException
   {
      if (null == elementData)
         return;

      System.out.println("Loading data...");

      Connection conn = dbmsDef.getConnection();
      System.out.println("autocommit = " + conn.getAutoCommit());
      conn.setAutoCommit(false);

      PreparedStatement stmt = PSPreparedStatement.getPreparedStatement(
               conn, getInsertStatement());

      int rowCount = 0;
      PSXmlTreeWalker w = new PSXmlTreeWalker(elementData);
      for(  Element e = w.getNextElement("row", getChild);
               e != null;
               e = w.getNextElement("row", getSibling) )
      {
         for(  Element c = w.getNextElement(getChild);
                  c != null;
                  c = w.getNextElement(getSibling) )
         {
            String name = dbmsDef.fixIdentifierCase(c.getAttribute("name"));
            String value = w.getElementData(".", false);

            // get this as a 1-based index
            Integer colNo = (Integer)m_columnPos.get(name);
            if (colNo == null)
            {
               try
               {
                  stmt.close();
                  conn.setAutoCommit(true);
               }
               catch(Exception ex)
               {
               }
               throw new IllegalArgumentException(
                     "Failed to load data. " + name
                     + " is not a defined column name.");
            }
            storeColumnData(
                  stmt, (ColumnDefinition)m_columns.get(colNo.intValue()-1),
                  value, colNo.intValue());
         }

         rowCount += stmt.executeUpdate();

         w.setCurrent(e);
      }
      conn.commit();
      conn.setAutoCommit(true);
      try
      {
         stmt.close();
      }
      catch(Exception e)
      {
      }

      System.out.println(rowCount + " row(s) inserted.");

      // for sanity check, select the values back out and compare them
         sanityCheck(dbmsDef, elementData, rowCount);
   }

      private void sanityCheck(DbmsDefinition dbmsDef, Element elementData, int expectedRows)
         throws SQLException
      {
         if (m_dataNode == null)
            return;

         System.out.println("Sanity checking (expect " + expectedRows + " rows)...");

         Connection conn = dbmsDef.getConnection();
         conn.setAutoCommit(false);

         PreparedStatement selectStmt = PSPreparedStatement
               .getPreparedStatement(conn, getSelectStatement());

         try
         {
            ResultSet rs = selectStmt.executeQuery();

            int rowCount = 0;

            PSXmlTreeWalker w = new PSXmlTreeWalker(elementData);
            for(  Element e = w.getNextElement("Row", getChild);
                  e != null;
                  e = w.getNextElement("Row", getSibling) )
            {
               if (rs.next())
               {
                  for(  Element c = w.getNextElement(getChild);
                        c != null;
                        c = w.getNextElement(getSibling) )
                  {
                     String name = dbmsDef.fixIdentifierCase(c.getTagName());
                     String value = w.getElementData(".", false);

                     // get this as a 1-based index
                     Integer colNo = (Integer)m_columnPos.get(name);
                     if (colNo == null)
                     {
                        conn.setAutoCommit(false);
                        throw new IllegalArgumentException(
                           "Failed to sanity check. " + name
                           + " is not a defined column name.");
                     }

                     compareColumnData(
                        rs, (ColumnDefinition)m_columns.get(colNo.intValue()-1),
                        value, colNo.intValue());
                  }
                  rowCount++;
               }

               w.setCurrent(e);
            }
            rs.close();
            conn.setAutoCommit(true);

            if (rowCount != expectedRows)
            {
               System.out.println("Warning: Expected " + expectedRows + " rows but got " + rowCount);
            }
         } finally
         {
            try
            {
               selectStmt.close();
            }
            catch(SQLException e)
            {
            }
         }
         System.out.println("Done sanity checking.");
   }

      private boolean objectsEqual(Object a, Object b)
      {
         boolean eq = false;
         if (a == null || b == null)
         {
            eq = (a == null && b == null);
         }
         else
         {
            eq = a.equals(b);
         }

         return eq;
      }

      private void compareColumnData(
         ResultSet rs,
         ColumnDefinition cDef,
         String value,
         int colNo )
         throws SQLException
      {
         int dt = cDef.getJdbcType();

         if (value != null && value.length() == 0)
            value = null;

         switch (dt)
         {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
               {
               String realVal = rs.getString(colNo);
               String val = value;
               if (!objectsEqual(val, realVal))
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               }
               break;

            case Types.INTEGER:
               {
               int realVal = rs.getInt(colNo);
          if( value == null )
              break;
               int val = Integer.parseInt(value);
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.SMALLINT:
               {
               short realVal = rs.getShort(colNo);
               short val = Short.parseShort(value);
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.BIT:
               {
               boolean realVal = rs.getBoolean(colNo);
               boolean val = (value.equals("1") || value.equalsIgnoreCase("true"));
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.TINYINT:
               {
               byte realVal = rs.getByte(colNo);
               byte val = Byte.parseByte(value);
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.BIGINT:
               {
               long realVal = rs.getLong(colNo);
               long val = Long.parseLong(value);
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
               /* this was my first choice to avoid precision problems,
                * but various drivers choked on it. as such, we've down-graded
                * to doubles.
                *
                * rs.setObject(colNo, new java.math.BigDecimal(value), dt);
                */
               {
               double realVal = rs.getDouble(colNo);
               double val = Double.parseDouble(value);
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.FLOAT:
            case Types.REAL:
               {
               float realVal = rs.getFloat(colNo);
               float val = Float.parseFloat(value);
               if (realVal != val)
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               else if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;

            case Types.DATE:
               {
               java.sql.Date realVal = rs.getDate(colNo);
               java.sql.Date val = java.sql.Date.valueOf(value);
               if (!objectsEqual(realVal, val))
               {
                  System.out.println("Warning: [" + val + "] != [" + realVal + "]");
               }
               else if (!objectsEqual("" + val, "" + realVal))
               {
                  System.out.println("Warning: \"" + val + "\" != \"" + realVal + "\"");
               }
               }
               break;
/*
            case Types.TIME:
               java.sql.Time time = java.sql.Time.valueOf(value);
               rs.setTime(colNo, time);
               break;

            case Types.TIMESTAMP:
               java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(value);
               rs.setTimestamp(colNo, timestamp);
               break;

            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
               // binary data must be in Base64 format (our rule)
               byte[] binData = getBinaryFromBase64(value);
               rs.setBytes(colNo, binData);
               break;

            default: // hope this can be done
               rs.setObject(colNo, value, dt);
               break;
*/          }
      }

      private void storeColumnData(
         PreparedStatement stmt, ColumnDefinition cDef,
         String value, int colNo)
         throws java.sql.SQLException
      {
         int dt = cDef.getJdbcType();

         if (value.length() == 0)
            stmt.setNull(colNo, dt);
         else
         {
            switch (dt)
            {
               case Types.CHAR:
               case Types.VARCHAR:
               case Types.LONGVARCHAR:
                  stmt.setObject(colNo, value, dt);
                  break;

               case Types.INTEGER:
                  stmt.setInt(colNo, Integer.parseInt(value));
                  break;

               case Types.SMALLINT:
                  stmt.setShort(colNo, Short.parseShort(value));
                  break;

               case Types.BIT:
                  stmt.setBoolean(colNo,
                     (value.equals("1") || value.equalsIgnoreCase("true")) );
                  break;

               case Types.TINYINT:
                  stmt.setByte(colNo, Byte.parseByte(value));
                  break;

               case Types.BIGINT:
                  stmt.setLong(colNo, Long.parseLong(value));
                  break;

               case Types.DOUBLE:
               case Types.NUMERIC:
               case Types.DECIMAL:
                  /* this was my first choice to avoid precision problems,
                   * but various drivers choked on it. as such, we've down-graded
                   * to doubles.
                   *
                   * stmt.setObject(colNo, new java.math.BigDecimal(value), dt);
                   */
                  stmt.setDouble(colNo, Double.valueOf(value).doubleValue());
                  System.out.println("Inserting " + Double.valueOf(value).doubleValue());
                  break;

               case Types.FLOAT:
               case Types.REAL:
                  stmt.setObject(colNo, Float.valueOf(value), dt);
                  break;

               case Types.DATE:
                  java.sql.Date date = java.sql.Date.valueOf(value);
                  stmt.setDate(colNo, date);
                  break;

               case Types.TIME:
                  java.sql.Time time = java.sql.Time.valueOf(value);
                  stmt.setTime(colNo, time);
                  break;

               case Types.TIMESTAMP:
                  System.out.println("Creating timestamp with value [" + value + "]");
                  java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(value);
                  System.out.println("Timestamp created: value is [" + timestamp.toString() + "]");

                  /* in order to fix bug id Rx-99-10-0209, we had to put in
                   * this kludge. The basic problem is that MS SQL Server
                   * is truncating the time component as the JDBC-ODBC Bridge
                   * is sending in a fraction size beyond SQL Server's
                   * capabilities. Unfortunately, SQL Server truncates too
                   * much of the time, going all the way down to the seconds
                   * component. As such, we've decided to take the timestamp
                   * and convert it to a string. This appears to fix
                   * SQL Server. For performance reasons, we need to revisit
                   * this and only use setString when we're talking to
                   * SQL Server.
                   */
                  stmt.setString(colNo, timestamp.toString());
                  // stmt.setTimestamp(colNo, timestamp);

                  break;

               case Types.BINARY:
               case Types.VARBINARY:
               case Types.LONGVARBINARY:
                  // binary data must be in Base64 format (our rule)
                  byte[] binData = getBinaryFromBase64(value);
                  stmt.setObject(colNo, binData, dt);
                  break;

               default: // hope this can be done
                  stmt.setObject(colNo, value, dt);
                  break;
            }
         }
      }

      private byte[] getBinaryFromBase64(String value)
         throws IllegalArgumentException
      {
         System.out.println("Converting base64 data...");
         java.io.ByteArrayInputStream iBuf
            = new java.io.ByteArrayInputStream(value.getBytes());
         java.io.ByteArrayOutputStream oBuf = new java.io.ByteArrayOutputStream();

         try {
            com.percussion.util.PSBase64Decoder.decode(iBuf, oBuf);
            return oBuf.toByteArray();
         } catch (java.io.IOException e) {
            throw new IllegalArgumentException(
               "Base64 decoding error: " + e.toString());
         }
      }

      private String m_name;
      private String m_qualifiedName;
      private String m_qualifiedPkeyName;

      /**
       * Fully qualified base name used to create foreign key constraints.  Each
       * constraint is added using this name with "_n" appended on the end where
       * n is a number monotinically incremented for each constraint added.
       * Initialized in the ctor, never <code>null</code>, empty, or modified
       * after that.
       */
      private String m_qualifiedFkeyName;
      private int iCreateFlag = 3 ;   // 0 = Do nothing , 1=create, 2= Do not Create, 3=create, never drop
      private int iAlterFlag  = 0 ;   // 0 = Do nothing , 1=alter , 2= Do not alter
      private int iDelOldData = 2 ;   // 0 = Do nothing , 1=Delete, 2= Do not Delete

      private List m_columns = new ArrayList();
      private List m_pKey = new ArrayList();

      /**
       * List of foreign key constraints.  Each entry is String array containing
       * 3 entries, where the first entry is the name of the column, the second
       * is the name of the foreign table, and the third is the name of the
       * foreign column.  Never <code>null</code>,  may be empty. Entries are
       * added in the ctor, and entries are never <code>null</code>.  List or
       * its entries are never modified after it is initialized in the ctor.
       */
      private List m_fKey = new ArrayList();

      private List m_indices = new ArrayList();

      private HashMap m_columnPos = new HashMap(); // col name to 1-based pos

      private Element m_dataNode;
      private DbmsDefinition m_dbmsDef = null;

      List getColumns(){return m_columns ;}
      HashMap getColumnPos(){return m_columnPos;}
   }

   class DBStruct
   {
      DBStruct(){}
      private String sqlDb        = new String();
      private String sServer      = new String();
      private String sDataBase    = new String();
      private String sSchema      = new String();
      public  Vector RxTables     = new Vector();
      public  Vector wfOldTables  = new Vector();
      public  Document dataDoc    = null ;
      public  Document dtdDoc     = null ;
      public  Document oldDataDoc = null ;
      public  Document oldDtdDoc  = null ;
      public  Element  eDtd       = null ;
      public  Element  eData      = null ;
      public boolean   bLogFlag   = false ;
      public  String   sLogDir    = new String();
      public  String   sLogFile   = new String();

      void setSqlDb(String psqlDb){sqlDb = psqlDb;}
      void setServer(String serverName){sServer=serverName;}
      void setDataBase(String dataBaseName){sDataBase=dataBaseName;}
      void setSchema(String schemaName){sSchema=schemaName;}
      void setDataDocument(Document eDocument){dataDoc = eDocument;}
      void setDtdDocument(Document dDocument){dtdDoc = dDocument;}

      String getSqlDb(){return sqlDb;}
      String getServer(){return sServer;}
      String getDataBase(){return sDataBase;}
      String getSchema(){return sSchema;}
      Document getDataDocument(){return dataDoc;}
      Document getDtdDocument(){return dtdDoc;}
   }

   public class WfSqlCreate
   {
      WfSqlCreate(){}
      private String  colName     = new String();
      private String  colValue    = new String();
      private String  colDataType = new String();

      void setColName(String pColName){colName = pColName;}
      void setColValue(String pColValue){colValue = pColValue;}
      void setColDataType(String pColDataType){colDataType = pColDataType;}

      String getColName(){return colName;}
      String getColValue(){return colValue;}
      String getColDataType(){return colDataType;}
   }

   class sqlTransactLog
   {
      public int rowCount = 0 ;
      public String errorMessage = new String();
      public boolean bStatus = false ;
      public String stmtType = new String();
   }

   class ColumnDefinition
   {
      ColumnDefinition(DbmsDefinition dbmsDef, PSXmlTreeWalker w)
         throws java.sql.SQLException
      {
         m_name = dbmsDef.fixIdentifierCase(w.getElementData("@name", false));
         m_jdbcType = w.getElementData("jdbctype", false);
         m_nativeType = (String)dbmsDef.getDataTypeMapping(m_jdbcType);
         if (m_nativeType == null)
            m_nativeType = m_jdbcType; // hope this works
         m_jdbcIntType = dbmsDef.getJdbcTypeMapping(m_jdbcType);

         m_params = w.getElementData("params", false);
         if ((m_params != null) && (m_params.length() == 0))
            m_params = null;

         m_nullClause = "";
         DatabaseMetaData meta = dbmsDef.getMetaData();
         if (meta.supportsNonNullableColumns())
         {
            String temp = w.getElementData("allowsnull", false);
            if ((temp != null) && temp.equalsIgnoreCase("no"))
               m_nullClause = " NOT NULL";
            else
               m_nullClause = " NULL";
         }
      }

      public int getJdbcType()
      {
         return m_jdbcIntType;
      }

      public String getColumnName()
      {
         return m_name;
      }

      public String getColumnDef()
      {
         StringBuffer buf = new StringBuffer();

         buf.append(m_name);
         buf.append(" ");
         buf.append(m_nativeType);
         if (m_params != null)
         {
            buf.append("(");
            buf.append(m_params);
            buf.append(")");
         }
         buf.append(m_nullClause);

         return buf.toString();
      }

      public boolean equals(Object o)
      {
         if (o == null)
            return false;

         if (o instanceof ColumnDefinition)
         {
            ColumnDefinition other = (ColumnDefinition)o;
            return this.m_name.equals(other.m_name);
         }

         return false;
      }
      
      @Override
      public int hashCode()
      {
         return m_name == null ? 0 : m_name.hashCode();
      }

      private String m_name;
      private String m_jdbcType;
      private String m_nativeType;
      private String m_params;
      private String m_nullClause;
      private int m_jdbcIntType;
   }


   class IndexDefinition
   {
      IndexDefinition(
         DbmsDefinition dbmsDef, String tableName, PSXmlTreeWalker w)
         throws java.sql.SQLException
      {
         super();

         m_tableName = dbmsDef.fixIdentifierCase(tableName);
         m_qualifiedTableName = dbmsDef.getQualifiedIdentifier(m_tableName);

         m_name = dbmsDef.fixIdentifierCase(w.getElementData("@name", false));
         // don't think this needs to be qualified
         // m_qualifiedName = dbmsDef.getQualifiedIdentifier(m_name);
         m_qualifiedName = m_name;

         for(  Element e = w.getNextElement("column", getChild);
               e != null;
               e = w.getNextElement("column", getSibling) )
         {
            m_columns.add(w.getElementData("name", false));
            m_sortOrders.add(w.getElementData("sort", false));
         }
      }

      public String getCreateIndexStatement()
      {
         StringBuffer buf = new StringBuffer();

         buf.append("CREATE INDEX ");
         buf.append(m_qualifiedName);
         buf.append(" ON ");
         buf.append(m_qualifiedTableName);
         buf.append(" (");

         for (int i = 0; i < m_columns.size(); i++)
         {
            if (i > 0)
               buf.append(", ");
            buf.append(m_columns.get(i));
         }

         buf.append(")");

         return buf.toString();
      }

      private String m_tableName;
      private String m_qualifiedTableName;

      private String m_name;
      private String m_qualifiedName;

      private List m_columns = new ArrayList();
      private List m_sortOrders = new ArrayList();
   }

  public void setLogFileName(String sFileName)
  {
    FileOutputStream out = null;
    try
    {
      out = new FileOutputStream(sFileName);
      m_sLogFileName = sFileName;
      m_pOut = new PrintStream(out, true);
    }
    catch(Exception e)
    {
      System.out.println(e.getMessage());
      System.out.println("Log will be written to console.");
    }
  }

  public void setPropFileName(String sFileName) throws IOException
  {
    m_Props.load(new FileInputStream(sFileName));
  }

  public void setPropFile(Properties props)
  {
    m_Props = props;
  }

  public void setBackEndDB(String sType)
  {
    m_sBackend = sType;
  }

  public void setDataTypeMapFileName(String sDataTypeMapFileName) throws IOException, SAXException
  {
    setDataTypeMapDocument(loadXMLDocument(sDataTypeMapFileName));
  }

  public void setDataTypeMapDocument(Document docDataTypeMap)
  {
    m_docDataTypeMap = docDataTypeMap;
  }

  public void setDataTypeMapDocument(InputStream in)  throws IOException, SAXException
  {
   setDataTypeMapDocument(loadXMLDocument(in));
  }

  public void setTableDescFileName(String sTableDescFileName) throws IOException, SAXException
  {
    setConfigDocument(loadXMLDocument(sTableDescFileName));
  }

  public void setConfigDocument(Document docConfig)
  {
    m_docConfig = docConfig;
  }

  public void setConfigDocument(InputStream in) throws IOException, SAXException
  {
   setConfigDocument(loadXMLDocument(in));
  }

  public void setTableDataFileName(String sTableDataFileName) throws IOException, SAXException
  {
    setTableDataDocument(loadXMLDocument(sTableDataFileName));
  }

  public void setTableDataDocument(Document docTableData)
  {
    m_docTableData = docTableData;
  }

  public void setTableDataDocument(InputStream in) throws IOException, SAXException
  {
   setTableDataDocument(loadXMLDocument(in));
  }

  private Document loadXMLDocument(InputStream in) throws IOException, SAXException
  {
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      return db.parse(new InputSource(in));
  }

  private Document loadXMLDocument(String sFileName) throws IOException, SAXException
  {
      DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);
      return db.parse(sFileName);
  }

   /**
  *  Worker function to look at column changes and determine if only column
  *  adds are needed.
  *
  * @param vColumnChanges - Vector of RxColumns to check.
  */
  public static final boolean onlyAdds(Vector vColumnChanges)
  {
      boolean bOnlyAdds = true;

      if(vColumnChanges != null)
      {
          for(int iCol = 0; iCol < vColumnChanges.size(); ++iCol)
          {
              RxColumns column = (RxColumns)
                  vColumnChanges.get(iCol);

              if(column != null)
              {
                  if(column.getColAction() != RxColumns.ADD_COLUMN)
                    return(false);
              }
          }
      }


      return(bOnlyAdds);
  }

 /**
  *  Worker function to look at column changes and determine if any column
  *  changes exist that are not because the user adding a column.
  *
  * @param vColumnChanges - Vector of RxColumns to check.
  */
  public static final boolean anyNonUser(Vector vColumnChanges)
  {
      boolean bAnyNonUser = false;

      if(vColumnChanges != null)
      {
          for(int iCol = 0; iCol < vColumnChanges.size(); ++iCol)
          {
              RxColumns column = (RxColumns)
                  vColumnChanges.get(iCol);

              if(column != null)
              {
                  if(column.getColAction() != RxColumns.USER_COLUMN)
                  {
//                   System.out.println(" found non user action for "
  //                          + column.getColName() + " action: " + new Integer(column.getColAction()).toString());
                    return(true);
                  }
              }
          }
      }


      return(bAnyNonUser);
  }

   public static void main(String args[])
   {
      try
      {
         if(args.length < 4)
         {
            System.err.println("Usage: java RxJdbcTableFactory <properties_file_name> <data_type_map_file> <table-desc-file> <table-data-file> [SRV=<server-override>] [DB=<database-override>] [SCHEMA=<schema-override>]");
            System.exit(1);
         }

         if (args[0].equalsIgnoreCase("-dbprops"))
         {
            main2(args); //execute in a new mode
            return;
         }

         String srv = null;
         String db = null;
         String schema = null;
         RxJdbcTableFactory me = new RxJdbcTableFactory();
         me.setPropFileName(args[0]);
         me.setDataTypeMapFileName(args[1]);
         me.setTableDescFileName(args[2]);
         me.setTableDataFileName(args[3]);

         me.populateTablesIn(srv, db, schema);

      }
      catch(Exception e)
      {
         System.out.println();
         System.out.println("Exception encountered - copy aborted.");
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
      }
      finally
      {
      }
   }

   /**
    * Enhanced version of the main that takes multiple defs and data files.
    * Each additional def and data file must be prepended with -def or -deta res-
    * pectively. ie:
    * -dbprops <propsFile> -typemap <typeMapFile> -def <defFile> -data <dataFile>
    * @param args see description.
    */
   private static void main2(String args[])
   {
      if(args.length < 8)
      {
         System.err.println("Usage: java RxJdbcTableFactory -dbprops <propsFile> -typemap <typeMapFile> -def <defFile> -data <dataFile>");
         System.exit(1);
      }

      String propsFile = null;
      String typemapFile = null;

      Collection defs = new ArrayList();
      Collection datas = new ArrayList();

      boolean nextDbProps = false;
      boolean nextDbTypeMap = false;
      boolean nextDef = false;
      boolean nextData = false;
      for (int i = 0; i < args.length; i++)
      {
         String tmp = args[i];

         if (tmp.equalsIgnoreCase("-dbprops"))
            nextDbProps = true;
         else if (tmp.equalsIgnoreCase("-typemap"))
            nextDbTypeMap = true;
         else if (tmp.equalsIgnoreCase("-def"))
            nextDef = true;
         else if (tmp.equalsIgnoreCase("-data"))
            nextData = true;
         else
         {
            if (nextDbProps)
               propsFile = tmp;
            else if (nextDbTypeMap)
               typemapFile = tmp;
            else if (nextDef)
               defs.add(tmp);
            else if (nextData)
               datas.add(tmp);

            nextDbProps = false;
            nextDbTypeMap = false;
            nextDef = false;
            nextData = false;
         }
      }

      Document schemaDoc = null;
      Document dataDoc = null;
      PSJdbcTableSchemaCollection schemaColl = null;
      PSJdbcTableDataCollection dataColl = null;
      PSJdbcTableSchema schema = null;
      PSJdbcTableData data = null;
      PrintStream ps = System.out;
      Connection conn = null;
      PSProperties props = null;
      PSJdbcDbmsDef dbmsDef = null;
      PSJdbcDataTypeMap dataTypeMap = null;

      try
      {
         props = new PSProperties(propsFile);
         props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");

         dbmsDef = new PSJdbcDbmsDef(props);
         dataTypeMap = new PSJdbcDataTypeMap(
                  props.getProperty("DB_BACKEND"),
                  props.getProperty("DB_DRIVER_NAME"), null);

         conn = PSJdbcTableFactory.getConnection(dbmsDef);

         //get table def files
         Iterator itdefs = defs.iterator();

         while (itdefs.hasNext())
         {
            String filePath = (String) itdefs.next();

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
         Iterator itdatas = datas.iterator();

         while (itdatas.hasNext())
         {
            String filePath = (String) itdatas.next();

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

         int schemaObjectsLen = schemaColl.size();
         Iterator it = schemaColl.iterator();
         int percentCompleted = 0;
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
                  throw ex;
            }
            index++;
         }
      }
      catch(Exception e)
      {
         System.out.println();
         System.out.println("Exception encountered - copy aborted.");
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
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

  String m_sBackend = null;

  Properties m_Props = new Properties();
  Document m_docDataTypeMap = null;
  Document m_docConfig = null;
  Document m_docTableData = null;
  String m_sStatus = ""; // Error Status Message

  String m_sLogFileName = null;
  int test = 0 ;

  PrintStream m_pOut = new PrintStream(System.out);
  DBStruct cDB = new DBStruct();
  Logger m_logger = Logger.getLogger();

   static final int getChild = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
   static final int getSibling = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;

   private static org.apache.logging.log4j.Logger log = LogManager.getLogger(RxJdbcTableFactory.class);
}
