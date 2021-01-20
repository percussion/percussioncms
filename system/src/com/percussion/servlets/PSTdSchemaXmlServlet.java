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
package com.percussion.servlets;

import com.percussion.deploy.server.PSServerJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This servlet is used by the workbench, specifically the tmeplate editor and
 * wizard to generate the database schema needed for database publishing.
 */
public class PSTdSchemaXmlServlet extends HttpServlet
{
   
   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
    *  javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      
      String test = request.getParameter("test");
      if(StringUtils.isNotBlank(test))
      {
        pushResponse(response, "PSTdSchemaXmlService is Alive!!!", "text/plain", 200);
         return;
      }
      String datasource = request.getParameter("datasource");
      String[] tables =request.getParameterValues("tables");      
      boolean allowChanges = 
         StringUtils.defaultString(
            request.getParameter("allowchanges"), "false").equalsIgnoreCase("true")
            ? true : false;
      if(StringUtils.isNotBlank(datasource))
      {
         String xml = createTableSchemaXml(datasource, tables, allowChanges);
         pushResponse(response, xml, "text/xml", 200);
      }
      else
      {
         String resp = "Servlet is not meant to handle the request";
         pushResponse(response, resp, "text/plain", 404);
      }
            
   }
   
   /**
    * Creates an XML file containing the table schema definitions of the
    * selected tables. This XML file conforms to the "sys_Tabledef.dtd" dtd.
    */
   private String createTableSchemaXml(
      String datasource, String[] tables, boolean allowSchemaChanges)
   throws ServletException
   {
      PSConnectionInfo connInfo = new PSConnectionInfo(datasource);      
      if (tables == null || tables.length == 0)
         return null;
      
      Connection conn = null;

      try
      {   

         conn = PSConnectionHelper.getDbConnection(connInfo);
         
         PSJdbcDbmsDef dbmsDef = new PSServerJdbcDbmsDef(connInfo);
         PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(
               dbmsDef.getBackEndDB(), dbmsDef.getDriver(), null);
         
         Document doc = PSXmlDocumentBuilder.createXmlDocument();

        
         // create all tabledef nodes into a map by table names
         final Map<String, Element> tabledefNodes =
               new HashMap<String, Element>();
        for(String tableName : tables)
         {
            PSJdbcTableSchema tableSchema = PSJdbcTableFactory.catalogTable(
               conn, dbmsDef, dataTypeMap, tableName, false);

            if (tableSchema == null)
            {
               String errMsg =  "Failed to create table schema for table: " + tableName;
               throw new ServletException(errMsg);
            }

            tableSchema.setAllowSchemaChanges(allowSchemaChanges);
            Element node = tableSchema.toXml(doc);
            tabledefNodes.put(node.getAttribute("name"), node);
         }
         
         // order the table names, parent tables first, children later
         List<String> orderedTableNames = new ArrayList<String>();
         Iterator<String> tableNames = tabledefNodes.keySet().iterator();
         while (tableNames.hasNext())
         {
            String tableName = tableNames.next();
            orderTableNames(tableName, tabledefNodes, orderedTableNames);
         }

         // produce the output document in the correct order
         Element root = PSXmlDocumentBuilder.createRoot(doc, XML_ROOT_ELEMENT);
         tableNames = orderedTableNames.iterator();
         while (tableNames.hasNext())
         {
            String tableName = tableNames.next();
            
            // the same name may be in the list multiple time, use the first only
            Element node = tabledefNodes.remove(tableName);
            if (node != null)
               root.appendChild(node);
         }

         return transformDocument(doc);            
         
      }
      catch (Exception ex)
      {
         throw new ServletException(ex);
      }
      finally
      {         
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException ignore)
            {
            }
         }
      }
   }
   
   /**
    * Recursivly process the specified table name for references and insert
    * the parent table names to the top and child table names to the end of 
    * the ordered name table.
    * 
    * @param tableName the name of the table for which to process the 
    *    references, assumed not <code>null</code> or empty.
    * @param tabledefNodes a map with all nodes to process, assumed not 
    *    <code>null</code> or empty.
    * @param orderedTableNames a list with all table names in the order as
    *    they should produce the output document, assumed not <code>null</code>, 
    *    may be empty. The returned list may contain duplicates in which
    *    case the first found must be used for the produced output order.
    */
   private void orderTableNames(String tableName, Map tabledefNodes, 
      List<String> orderedTableNames)
   {
      Element node = (Element) tabledefNodes.get(tableName);
      List references = getReferencedTables(node);
      if (!references.isEmpty())
      {
         for (int i=0; i< references.size(); i++)
         {
            String referencedTableName = (String) references.get(i);
            if (tabledefNodes.get(referencedTableName) != null)
               orderTableNames(referencedTableName, tabledefNodes, 
                  orderedTableNames);
         }
      }
      
      if (references.isEmpty())
      {
         // insert parents to the very top
         orderedTableNames.add(0, tableName);
      }
      else
      {
         // append children to the very end
         orderedTableNames.add(tableName);
      }
   }
   
   /**
    * Get all referenced <code>externalTable</code> names from all 
    * <code>foreignkey</code> nodes found in the supplied node.
    * 
    * @param node the node in which to search for referenced external tables,
    *    assumed not <code>null</code>.
    * @return a list with all external table names referenced in the 
    *    supplied node, never <code>null</code>, may be empty.
    */
   private List getReferencedTables(Element node)
   {
      final List<String> references = new ArrayList<String>();
      
      NodeList foreignKeys = node.getElementsByTagName("foreignkey");
      if (foreignKeys != null)
      {
         for (int i=0; i<foreignKeys.getLength(); i++)
         {
            Element foreignKey = (Element) foreignKeys.item(i);
            
            NodeList externalTables = foreignKey.getElementsByTagName(
               "externalTable");
            if (externalTables != null)
            {
               for (int j=0; j<externalTables.getLength(); j++)
               {
                  String externalTableName = PSXmlTreeWalker.getElementData(
                     externalTables.item(j));
                  references.add(externalTableName);
               }
            }
         }
      }
      
      return references;
   }

   /**
    * Transforms XML file based on the "TableDefBuilder.xsl" XSL file
    * using saxon's TransformerFactory and Transformer classes.
    *
    * @param doc the XML document to be transformed, assumed never
    * <code>null</null> or empty
    * @return the document transformed and returned as a string.
    */
   @SuppressWarnings("unused")
   private String transformDocument(Document doc)
      throws TransformerConfigurationException, TransformerException, 
      FileNotFoundException, SAXException, IOException 
   {
     
      String xslFileName = TABLE_BUILDER_XSL_FILE;

      // locate and load the XSL File
      URL xslUrl = this.getClass().getResource(xslFileName);
      if(xslUrl == null)
      {
         String errMsg =  "XSL File not found: " + xslFileName;
         throw new FileNotFoundException(errMsg);
      }

      TransformerFactory tFactory = TransformerFactory.newInstance();
      Transformer transformer = null;
      transformer = tFactory.newTransformer(
         new StreamSource(xslUrl.toString()));
      DOMResult domResult = new DOMResult();
      DOMSource domSource = new DOMSource(doc);
      transformer.transform(domSource, domResult);
      return PSXMLDomUtil.toString(domResult.getNode());
   }
   
   /**
    * Helper method to help with passing back the response.
    * @param httpResponse the response object, assumed not <code>null</code>.
    * @param resp the response data string, 
    * @param ctype the response content type, assumed not <code>null</code>.
    * @param respCode the response code.
    * @throws IOException
    */
   private void pushResponse(HttpServletResponse httpResponse,
      String resp, String ctype, int respCode) throws IOException
   {
      /* Discard if the connection has closed */
      if (httpResponse.isCommitted())
         return;
     
      httpResponse.setContentType(ctype);
      byte[] respBytes = resp.getBytes("UTF-8");
      httpResponse.setContentLength(respBytes.length);
      httpResponse.setStatus(respCode);
      if(respCode == 500)
         httpResponse.sendError(respCode, resp);
      OutputStream os = httpResponse.getOutputStream();
      os.write(respBytes);
      os.flush();      
   }   
   
  

  /**
   * stylesheet file
   */
  private static final String TABLE_BUILDER_XSL_FILE = "TableDefBuilder.xsl";

  /**
   * default value for the table data definition xml root element
   */
  private static final String XML_ROOT_ELEMENT = "tabledataset";





   
   

}
