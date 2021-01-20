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
package com.percussion.converters.variantconverter;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.inlinelinkconverter.PSInlineLinkConverter;
import com.percussion.tablefactory.install.RxLogTables;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSRemoteRequester;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is a converter tool class for converting variant ids from old to new in
 * the inline content and in relationships. This is intended for clients moving
 * from xsl variants to velocity templates. The input for this class comes from
 * variantsconverter.properties file. It needs server details, workflow details
 * and variant map. Clients can also supply optional information such as content
 * type ids and content ids. The conversion will be limited to the supplied
 * content type ids and content ids. If the content is in checked out state then
 * it will not be converted.
 */
public class PSVariantConverter
{

   /**
    * Updates the regular slot relationships by replacing the old variant id
    * with the new template id. If contentIds exist in the properties file, then
    * converts the variants to templates for the supplied content ids. Skips the
    * content items that are in checked out state.
    */
   private static void updateRelationShips()
   {
      if(StringUtils.isBlank(ms_slotContentIds))
      {
         writeToLog("No items found for slot relationship conversion.");
         return;
      }
      Connection conn = null;
      PSJdbcDbmsDef dbmsDef = null;
      writeToLogAndConsole("Conversion started");
      try
      {
         conn = getJdbcConnection();
         dbmsDef = new PSJdbcDbmsDef(PSJdbcDbmsDef
               .loadRxRepositoryProperties(".."));
         final String qualRelTableName = PSSqlHelper.qualifyTableName(
               "PSX_OBJECTRELATIONSHIP", dbmsDef.getDataBase(), dbmsDef
                     .getSchema(), dbmsDef.getDriver());
         Iterator iter = ms_variantMap.keySet().iterator();
         while (iter.hasNext())
         {
            String oldVarintId = (String) iter.next();
            String newVarintId = (String) ms_variantMap.get(oldVarintId);
            // Break the content id list into 500 items per array as oracle has
            // a limit of 1000 items per inclause and SQL performs better with
            // 500 item per inclause.
            String[] cids = ms_slotContentIds.split(",");
            List<Object[]> lstCids = new ArrayList<Object[]>();
            for (int i=0;i<cids.length;i=i+500)
            {
               lstCids.add(ArrayUtils.subarray(cids, i, i+500));
            }
            for (Object[] objects : lstCids)
            {
               if(objects.length<1)
                  continue;
               String newCids = ArrayUtils.toString(objects, "()");
               newCids = StringUtils.replace(newCids, "{", "(");
               newCids = StringUtils.replace(newCids, "}", ")");

               String upSqlSt = "UPDATE " + qualRelTableName + " SET "
               + qualRelTableName + ".VARIANT_ID = " + newVarintId
               + " WHERE " + qualRelTableName + ".VARIANT_ID = "
               + oldVarintId + " AND " + qualRelTableName + ".OWNER_ID IN "
               + newCids + " AND " + qualRelTableName
               + ".INLINE_RELATIONSHIP IS NULL";
               writeToLogAndConsole("Update SQL statement \n" + upSqlSt);
               final Statement updStmt = conn.createStatement();
               updStmt.executeUpdate(upSqlSt);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
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
      writeToLogAndConsole("Conversion finished");
   }

   /**
    * Builds a variant map from the property string variantMap. The expected
    * format is old and new variant ids are seperated by pipe (|) character and
    * pairs are seperated by comma (,).
    * 
    * @param variantMap The variant map property string assumed not
    *           <code>null</code>.
    */
   private static void buildVariantMap(String variantMap)
   {
      ms_variantMap.clear();
      List variantsList = PSStringOperation.getSplittedList(variantMap, ',');
      for (int i = 0; i < variantsList.size(); i++)
      {
         String temp = variantsList.get(i).toString();
         List oldNewVars = PSStringOperation.getSplittedList(temp, '|');
         String msg = "Skipping Invalid variantMap entry :"
               + oldNewVars.toString();
         if (oldNewVars.size() != 2)
         {
            System.out.println(msg);
            writeToLog(msg);
            continue;
         }
         String oldVar = oldNewVars.get(0).toString();
         String newVar = oldNewVars.get(1).toString();
         if (oldVar.trim().length() < 1)
         {
            System.out.println(msg);
            writeToLog(msg);
            continue;
         }
         ms_variantMap.put(oldVar, newVar);
      }
   }
   
   /**
    * A convenient method to add the variant map to xsl doc. Finds a
    * xsl:varianble element with name attribute as variantMap and adds a
    * variantmap element with variant child elements for each variant that needs
    * to be converted. The old variant is added as old attribute and new variant
    * is added as new attribute.
    * 
    * @param xslDoc The XSL document for which the variant map needs to be
    *           added. Assumed not <code>null</code>.
    */
   private static void addVariantMapToXslDoc(Document xslDoc)
   {
      Element root = xslDoc.getDocumentElement();
      NodeList nl = root.getElementsByTagName("xsl:variable");
      Element varMapElem = null;
      for (int i = 0; i < nl.getLength(); i++)
      {
         Element elem = (Element) nl.item(i);
         if (elem.getAttribute("name").equals("variantMap"))
         {
            varMapElem = elem;
            break;
         }
      }
      if (varMapElem == null)
      {
         // This should not happen as we are creating the xsl with this variable
         // in it.
         // If it happens just show error and exit.
         exitProgram("Conversion xsl is missing a required variable element. Skipping the conversion.");
      }
      Iterator iter = ms_variantMap.keySet().iterator();
      while (iter.hasNext())
      {
         String oldVarintId = (String) iter.next();
         String newVarintId = (String) ms_variantMap.get(oldVarintId);
         Element varElem = PSXmlDocumentBuilder.addEmptyElement(xslDoc,
               varMapElem, "variant");
         varElem.setAttribute("old", oldVarintId);
         varElem.setAttribute("new", newVarintId);
      }
   }

   /**
    * Convenient method to exit the program.
    * 
    * @param msg If not blank will be printed to the console and logged.
    */
   private static void exitProgram(String msg)
   {
      if (!StringUtils.isBlank(msg))
      {
         writeToLogAndConsole(msg);
      }
      System.out.println("Press enter to exit.");
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      try
      {
         in.readLine();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      System.exit(-1);
   }

   /**
    * The connection to the backend repository. It uses the connection
    * information from {@link PSJdbcDbmsDef#loadRxRepositoryProperties(String)}.
    * 
    * @return the connection to the backend repository. Note, it is caller's
    *         resposibility to close this connection.
    * 
    * @throws Exception if an error occurs while creating the connection.
    */
   public static Connection getJdbcConnection() throws Exception
   {
      Properties repprops = PSJdbcDbmsDef.loadRxRepositoryProperties("..");
      return RxLogTables.createConnection(repprops);
   }

   /**
    * Prints the command line usage of this class to the console.
    */
   private static void printUsage()
   {
      System.out.println("Usage: ");
      System.out.println("");
      System.out
            .println("java com.percussion.converters.variantconverter.PSVariantConverter");
      System.out
            .println("See the PSVariantConverter documentation for details on "
                  + "the properties file.");
   }

   /**
    * Write a message to the log file.
    * 
    * @param msg the message to write
    */
   private static void writeToLog(String msg)
   {
      try
      {
         m_logger.write(msg);
         m_logger.newLine();
         m_logger.flush();
      }
      catch (Exception ex)
      { /* ignore */
      }
   }

   /**
    * Write a message to the log file.
    * 
    * @param msg the message to write
    */
   private static void writeToLogAndConsole(String msg)
   {
      writeToLog(msg);
      System.out.println(msg);
   }

   /**
    * Main method for this converter application.
    * 
    * @param args - This converter does not need any arguments.
    * @see usage
    */
   @SuppressWarnings({"unchecked"})
   public static void main(String[] args)
   {
      try
      {
         m_logger = new BufferedWriter(new FileWriter(LOG_ALL));
      }
      catch (IOException e1)
      {
         exitProgram("Failed to create log file skipping conversion.");
      }
      writeToLogAndConsole("********** Started conversion process ***********");
      System.out
            .println("The process may take long time based on your content and relationships");
      // load the properties file
      FileInputStream in = null;
      Properties props = new Properties();
      try
      {
         in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
         props.load(in);
         String variantMap = props.getProperty(VARIANTS_MAP);
         if (variantMap == null || variantMap.trim().length() < 1)
         {
            exitProgram("Skipping conversion as properties file is missing "
                  + VARIANTS_MAP + " property");
         }
         buildVariantMap(variantMap);
         if (ms_variantMap.isEmpty())
         {
            exitProgram("Skipping conversion as no valid variant map is provided.");
         }

         //Get the contentId property
         String contentIds = props.getProperty("contentId");
         List<String> contentIdList = new ArrayList<String>();
         if (contentIds != null)
         {
            //If it exists check whether it is valid list or not
            contentIds = contentIds.trim();
            contentIdList = PSStringOperation.getSplittedList(
                  contentIds, ',');
            for (String cid : contentIdList)
            {
               if (StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))
               {
                  exitProgram("Invalid list of contentIds supplied, skipping conversion.\n"
                        + contentIds);
               }
            }
            ms_contentIds = contentIds;
         }

         PSRemoteRequester requester = new PSRemoteRequester(props);
         m_rtAgent = new PSRemoteAgent(requester);

         //Set content type
         String contentType = StringUtils.defaultString(props.getProperty("contentType"));
         if(StringUtils.isNotBlank(contentType))
         {
            PSItemDefinition itemDef = m_rtAgent.getTypeDef(contentType);
            if (itemDef == null)
            {
               exitProgram("Invalid contentType supplied, skipping conversion.\n"
                     + contentType);
            }
            ms_contentType = itemDef.getTypeId() + "";
         }
         
         //Generate the content ids
         generateContentIds();
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
         exitProgram("Unable to locate file: " + DEFAULT_PROPERTIES_FILE);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         exitProgram("Error loading properties from file ("
               + DEFAULT_PROPERTIES_FILE + "): " + e.toString());
      }
      catch (PSRemoteException e)
      {
         e.printStackTrace();
         exitProgram("Error loading the content type for the given " +
                "property contentType");
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception e)
            { /* ignore */
            }
         }
      }
      FileInputStream cvXSL = null;
      Document xslDoc = null;
      try
      {
         cvXSL = new FileInputStream(VARIANTS_CONVERTER_XSL);
         xslDoc = PSXmlDocumentBuilder.createXmlDocument(cvXSL, false);
         addVariantMapToXslDoc(xslDoc);
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
         exitProgram("Unable to locate file: " + VARIANTS_CONVERTER_XSL);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         exitProgram("Error loading xsl file (" + VARIANTS_CONVERTER_XSL
               + "): " + e.toString());
      }
      catch (SAXException e)
      {
         e.printStackTrace();
         exitProgram("Error parsing xsl file (" + VARIANTS_CONVERTER_XSL
               + "): " + e.toString());
      }
      finally
      {
         if (cvXSL != null)
         {
            try
            {
               cvXSL.close();
            }
            catch (Exception e)
            { /* ignore */
            }
         }
      }

      //Convert inline relationships if we have valid contet ids
      if(StringUtils.isBlank(ms_inlineContentIds))
      {
         writeToLogAndConsole("No inline relationships found for conversion.");
      }
      else
      {
         try
         {
            //Set the contentId property to the valid inline content ids.
            props.setProperty("contentId", ms_inlineContentIds);
            PSInlineLinkConverter ilc = new PSInlineLinkConverter(props, xslDoc);
            writeToLogAndConsole("Inline relationships");
            writeToLogAndConsole("See convert.log file for inline relationships conversion log.");
            ilc.doConvert();
            // Convert the relationships
            writeToLogAndConsole("Regular slot relationships");
         }
         catch (Exception e)
         {
            e.printStackTrace();
            exitProgram("Error - caught unknown exception: " + e.getMessage());
         }
         
      }
      
      //Convert slot relationships if we have valid slot contentids to convert
      if(StringUtils.isBlank(ms_slotContentIds))
      {
         writeToLogAndConsole("No slot relationships found for conversion.");
      }
      else
      {
         updateRelationShips();
      }
      exitProgram("********** Finished conversion process ***********");
   }
   
   /**
    * Generates the content ids for slot relationship modifications and inline
    * relationship modifications. Gets the checked in item's content ids from
    * contentstatus table filtered by supplied content type id (if exists) and
    * content ids (if exists). Get the distinct ownerids from relationship table
    * for the each variant id in the map from the above content ids. Applies
    * inline_relationship is null condition for slot relationship items and is
    * not null condition for inline relationships.
    */
   private static void generateContentIds()
   {
      //Get all checked in content ids for the given content id and content type.
      //Build a list of 500 contentids
      //Build another list by filtering above content is
      try
      {
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(PSJdbcDbmsDef
               .loadRxRepositoryProperties(".."));
         final String qualRelTableName = PSSqlHelper.qualifyTableName(
               "PSX_OBJECTRELATIONSHIP", dbmsDef.getDataBase(), dbmsDef
                     .getSchema(), dbmsDef.getDriver());
         final String qualCsTableName = PSSqlHelper.qualifyTableName(
               "CONTENTSTATUS", dbmsDef.getDataBase(), dbmsDef.getSchema(),
               dbmsDef.getDriver());
         String sqlStmt = "SELECT " + qualCsTableName + ".CONTENTID FROM "
         + qualCsTableName + " WHERE (" + qualCsTableName
         + ".CONTENTCHECKOUTUSERNAME IS NULL OR " + qualCsTableName
         + ".CONTENTCHECKOUTUSERNAME = '')";
         if(StringUtils.isNotBlank(ms_contentType))
         {
            //Add content type condition
            sqlStmt += " AND " + qualCsTableName
            + ".CONTENTTYPEID = " + ms_contentType;
         }
         if(StringUtils.isNotBlank(ms_contentIds))
         {
            sqlStmt += " AND " + qualCsTableName
            + ".CONTENTID IN (" + ms_contentIds + ")";
            
         }
         writeToLog("Checked in items SQL statement: " + sqlStmt);
         String cidString = getContentIds(sqlStmt,"CONTENTID");
         if(StringUtils.isBlank(cidString))
               exitProgram("Could not find any valid content ids for variant conversion.");
         
         String[] cidAr = cidString.split(",");
         Iterator iter = ms_variantMap.keySet().iterator();
         while (iter.hasNext())
         {
            String oldVarintId = (String) iter.next();
            List<Object[]> lstCids = new ArrayList<Object[]>();
            for (int i=0;i<cidAr.length;i=i+500)
            {
               lstCids.add(ArrayUtils.subarray(cidAr, i, i+500));
            }
            for (Object[] cids : lstCids)
            {
               String newCids = ArrayUtils.toString(cids, "()");
               newCids = StringUtils.replace(newCids, "{", "(");
               newCids = StringUtils.replace(newCids, "}", ")");
               
               String baseSql = "SELECT DISTINCT(" + qualRelTableName
                     + ".OWNER_ID) FROM " + qualRelTableName + " WHERE "
                     + qualRelTableName + ".VARIANT_ID = " + oldVarintId
                     + " AND " + qualRelTableName + ".OWNER_ID IN " + newCids;

               String slotSql = baseSql + 
                  " AND " + qualRelTableName + ".INLINE_RELATIONSHIP IS NULL";
               
               writeToLog("Slot relationship owner id SQL statement:\n"
                     + slotSql);
               if (StringUtils.isNotBlank(ms_slotContentIds)
                     && !ms_slotContentIds.endsWith(","))
                  ms_slotContentIds += ",";

               ms_slotContentIds += getContentIds(slotSql,"OWNER_ID");
               writeToLog("Inline relationship owner ids:\n"
                     + ms_slotContentIds);

               String inlineSql = baseSql + 
               " AND " + qualRelTableName + ".INLINE_RELATIONSHIP IS NOT NULL";

               writeToLog("Inline relationship owner id SQL statement:\n"
                     + inlineSql);
               
               if (StringUtils.isNotBlank(ms_inlineContentIds)
                     && !ms_inlineContentIds.endsWith(","))
                  ms_inlineContentIds += ",";
               
               ms_inlineContentIds += getContentIds(inlineSql,"OWNER_ID");
               writeToLog("Inline relationship owner ids:\n"
                     + ms_inlineContentIds);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Helper method to get the comma seperated list of Integer ids by executing
    * the supplied sql statement. Assumes the SQL statement is a select
    * statement with an Integer column exists with supplied colName.
    * 
    * @return String never <code>null</code> may be empty.
    */
   private static String getContentIds(String sqlStmt, String colName)
   {
      String cidString = "";
      Connection conn = null;
      try
      {
         conn = getJdbcConnection();
      
         Statement selStmt = conn.createStatement();
         ResultSet resultSet = null;
         resultSet = selStmt.executeQuery(sqlStmt);
         while (resultSet.next())
         {
            cidString+=(Integer.toString(resultSet
                  .getInt(colName)) + ",");
         }
         if(StringUtils.isNotBlank(cidString))
            cidString = cidString.substring(0, cidString
                  .length() - 1);
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
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
      return cidString;
   }

   /**
    * This is used to communicate with Rhythmyx server, init by ctor, never
    * <code>null</code> after that.
    */
   private static PSRemoteAgent m_rtAgent = null;

   /**
    * The properties from the property file. Init by ctor, never
    * <code>null</code>.
    */
   private static final String DEFAULT_PROPERTIES_FILE = "variantsconverter.properties";

   /**
    * The xsl file that converts the old variants to new variants in inline
    * content.
    */
   private static final String VARIANTS_CONVERTER_XSL = "variantsconverter.xsl";

   /**
    * Name of the property for old and new variant mapping.
    */
   private static final String VARIANTS_MAP = "variantMap";

   /**
    * Variable to hold the variant map. Filled in main method.
    */
   private static Map ms_variantMap = new HashMap();

   /**
    * Comma seperated list of content ids. Filled in main method
    * from the property conentId. Never null may be empty.
    */
   private static String ms_contentIds = "";

   /**
    * Comma seperated list of contentids whose inline relationships needs to be 
    * converted. Set in {@link #generateContentIds()} method.
    */
   private static String ms_inlineContentIds = "";

   /**
    * Comma seperated list of contentids whose slot relationships needs to be 
    * converted. Set in {@link #generateContentIds()} method.
    */
   private static String ms_slotContentIds = "";
   
   /**
    * Id of content type for which the relationships needs to be converted. Set
    * in {@link #main(String[])} method.
    */
   private static String ms_contentType = "";

   /**
    * The writer for all logging data, init by ctor, never <code>null</code>
    * after that.
    */
   private static BufferedWriter m_logger = null;

   /**
    * The log file name, which contains all logged information
    */
   private static final String LOG_ALL = "variantconvert.log";
   
}
