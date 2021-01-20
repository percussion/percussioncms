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

import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.i18n.tmxdom.IPSTmxTranslationUnit;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableDataCollection;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class implements the interface {@link #IPSSectionHandler} and processes
 * the CMS tables part of the Rhythmyx Content Manager. The logic of building
 * the resource keys is completely built into the XSL Stylesheet
 * {@link #CMSTABLEDATA_TRANSFORM_XSL}. This stylesheet has a list of all tables
 * and the XSL templates to build the keys from the input XML document. This
 * input XML document is built by the TableFactory classes. Thse list of tables
 * is first read from the stylesheet. Table factory classes use this list of
 * tables to generate a single XML document for all the CMS tables listed in the
 * XSL stylesheet. The templates in the style sheets generate keys in the form
 * of another simple XML dcument that is used to construct the TMX Document.
 */

public class PSCmsTablesSectionHandler extends PSIdleDotter
   implements IPSSectionHandler
{
   /**
    * Default constructor. Constructs the XSL document and parses list of tables
    * int a list.
    * @throws PSSectionProcessingException in case it cannot parse the XSL file.
    * This should not happen since the stylesheet file is packaged into the JAR
    * file as we shipped.
    */
   public PSCmsTablesSectionHandler()
      throws PSSectionProcessingException
   {
      //Load the XSL stylesheet only once.
      if(ms_XslDoc != null)
         return;

      //This block is executed only once (like a static one).
      try
      {
         ms_XslDoc = PSXmlDocumentBuilder.createXmlDocument(getClass()
            .getResourceAsStream(CMSTABLEDATA_TRANSFORM_XSL), false);
         NodeList nl = ms_XslDoc.getElementsByTagName("psx:table");
         Element elem = null;
         String table = null;
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            elem = (Element)nl.item(i);
            table = elem.getAttribute(PSRxltConfigUtils.ATTR_NAME);
            if(table.trim().length() > 0)
               ms_Tables.add(table);
         }
      }
      //catch any exception and wrap into PSSectionProcessingException
      catch(Exception e)
      {
         throw new PSSectionProcessingException(e.getMessage());
      }
      //end of static block
   }
   /*
    * Implementation of the method defined in the interface.
    * See {@link IPSSectionHandler#process(Element)} for
    * details about this method.
    */
   public IPSTmxDocument process(Element cfgData)
      throws PSActionProcessingException

   {
      if(cfgData == null)
      {
         throw new IllegalArgumentException("cfgdata must not be null");
      }
      String rxroot = cfgData.getOwnerDocument().getDocumentElement().
         getAttribute(PSRxltConfigUtils.ATTR_RXROOT);

      //override the default section name with that defined in the config document.
      ms_SectionName = cfgData.getAttribute(PSRxltConfigUtils.ATTR_NAME);

      PSCommandLineProcessor.logMessage("blankLine", "");
      PSCommandLineProcessor.logMessage("processingSection", ms_SectionName);
      PSCommandLineProcessor.logMessage("blankLine", "");

      PSTmxDocument tmxDoc = null;
      try
      {
         PSCommandLineProcessor.logMessage("connectingToDB", "");

         Properties props = PSJdbcDbmsDef.loadRxRepositoryProperties(rxroot);
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
         PSJdbcDataTypeMap dataTypeMap =
            new PSJdbcDataTypeMap(props.getProperty("DB_BACKEND"),
            props.getProperty("DB_DRIVER_NAME"), null);
         Connection conn = PSJdbcTableFactory.getConnection(dbmsDef);
         Document dataDoc = getTableDataDoc(conn, dbmsDef, dataTypeMap);

         processRxConfigSets(dataDoc);

         PSCommandLineProcessor.logMessage("extractingKeyWords", "");

         //show idle dots
         showDots(true);

         dataDoc = transformXML(dataDoc, ms_XslDoc);
         //create an empty TMX Document
         tmxDoc = new PSTmxDocument();
         NodeList nl = dataDoc.getElementsByTagName("key");
         Node node = null;
         Element key = null;
         String name, desc;
         IPSTmxTranslationUnit tu = null;
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            key = (Element)nl.item(i);
            name = key.getAttribute(PSRxltConfigUtils.ATTR_NAME);
            node = key.getFirstChild();
            desc = "";
            if(node instanceof Text)
               desc = ((Text)node).getData();
            tu = tmxDoc.createTranslationUnit(name, desc);
            tu.addProperty(tmxDoc.createProperty(
                  IPSTmxDtdConstants.ATTR_VAL_SECTIONNAME,
                  PSI18nUtils.DEFAULT_LANG, ms_SectionName));
            //add translation unit for each key in the XML document
            tmxDoc.merge(tu);
         }
         //stop showing idle dots
         showDots(false);
      }
      catch(Exception e)//catch any exception
      {
         PSCommandLineProcessor.logMessage("processFailedError", e.getMessage() +" Stack: "+ ExceptionUtils.getFullStackTrace(e));
         throw new PSSectionProcessingException(e.getMessage(),e);
      }
      finally
      {
         endDotSession();
      }
      return tmxDoc;
   }

   /**
    * Method that is specific to extract the configuration related keys from the
    * table PSX_RXCONFIGURATIONS. This table has the the configuration document
    * (XML clob) as one of the columns. This method extracts that parses into an
    * XML document expands that table row element with the root element of the
    * config document. The table processing XSL has a template that processes
    * this table to extract the config related key names.
    * @param dataDoc assumed not <code>null</code> and in the format generated
    * by {@link #getTableDataDoc(Connection, PSJdbcDbmsDef, PSJdbcDataTypeMap)}
    */
   private void processRxConfigSets(Document dataDoc)
   {
      NodeList nl = dataDoc.getElementsByTagName("table");
      Element elem = null;
      Element elemConfigTable = null;
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         if(elem.getAttribute("name").equals("PSX_RXCONFIGURATIONS"))
         {
            elemConfigTable = elem;
            break;
         }
      }
      if(elemConfigTable == null)
         return; //no such table, do nothing

      nl = elemConfigTable.getElementsByTagName("row");

      Element elemConfigRow = null;
      int count = nl.getLength();
      for(int i=0; i<count; i++)
      {
         elemConfigRow = (Element)nl.item(0);
         NodeList nlCols = elemConfigRow.getElementsByTagName("column");
         Element elemCol = null;
         String name = "";
         String type = "";
         for(int ii=0; ii<nlCols.getLength(); ii++)
         {
            elem = (Element)nlCols.item(ii);
            String attr = elem.getAttribute("name");
            if(attr.equals("NAME"))
               name = ((Text)elem.getFirstChild()).getData();
            else if(attr.equals("TYPE"))
               type = ((Text)elem.getFirstChild()).getData();
            else if(attr.equals("CONFIGURATION"))
               elemCol = elem;
         }
         if(!type.equals("XML"))
            continue;
         PSCommandLineProcessor.logMessage("processingSection", name);
         if(elemCol == null)
            continue;
         else
         {
            try
            {
               String configData = ((Text)elemCol.getFirstChild()).getData();
               Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  new StringReader(configData), false);
               Element root = doc.getDocumentElement();
               root.setAttribute("name", name);
               //replace the row element with the root element of the config doc
               elemConfigRow.getParentNode().replaceChild(
                  elemConfigRow.getOwnerDocument().importNode(root, true),
                  elemConfigRow);
            }
            catch(Exception e)
            {
               PSCommandLineProcessor.logMessage(e.getLocalizedMessage());
            }
         }
      }
   }

   /**
    * Helper function to extract the schemaDoc and dataDoc from the database.
    * @param conn a valid database connection, must not be <code>null</code>
    * @param dbmsDef Used to connect to the database and provides correct
    * schema/origin, must not be <code>null</code>.
    * @param dataTypeMap The dataType map to use for this table's columns, must
    * not be <code>null</code>
    * @return dataDoc table data document never <code>null</code>
    * @throws PSJdbcTableFactoryException if data could not be extracted for
    * some reason.
    */
   private Document getTableDataDoc(Connection conn,
      PSJdbcDbmsDef dbmsDef, PSJdbcDataTypeMap dataTypeMap)
      throws PSJdbcTableFactoryException
   {
      PSCommandLineProcessor.logMessage("gettingBackendData", "");
      Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();
      Document schemaDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSJdbcTableDataCollection collData = new PSJdbcTableDataCollection();
      PSJdbcTableSchemaCollection collSchema =
         new PSJdbcTableSchemaCollection();
      PSJdbcTableSchema tableSchema = null;
      PSJdbcTableData tableData = null;
      for(int i=0;i<ms_Tables.size();i++)
      {
         PSCommandLineProcessor.logMessage("processingTable",
            ms_Tables.get(i).toString());

         tableSchema = PSJdbcTableFactory.catalogTable(conn, dbmsDef,
            dataTypeMap, ms_Tables.get(i).toString(), true);
         if(tableSchema == null)
            continue;
         collSchema.add(tableSchema);

         tableData = tableSchema.getTableData();
         if(tableData == null)
            continue;
         collData.add(tableData);
      }
      schemaDoc.appendChild(
         schemaDoc.importNode(collSchema.toXml(schemaDoc), true));
      dataDoc.appendChild(
         dataDoc.importNode(collData.toXml(dataDoc), true));
      return dataDoc;
   }

   /**
    * Helper method to transform and XML document to another XML Document using
    * the given stylesheet document.
    *
    * @param srcDoc source document, must not be <code>null</code>.
    * @param xslDoc xsl Document for transforming, must not be <code>null</code>.
    * @return the transformed XML document, never <code>null</code>.
    * @throws IllegalArgumentException if srcDoc or xslDoc is <code>null</code>
    * @throws SAXException if result XML document cannot be created for any
    * reason
    * @throws TransformerException if XSL transformation fails for any reason
    */
   public static Document transformXML(Document srcDoc, Document xslDoc)
      throws SAXException, TransformerException
   {
      if(srcDoc==null)
      {
         throw new IllegalArgumentException("srcDoc must not be null.");
      }
      if(xslDoc == null)
      {
         throw new IllegalArgumentException("xslDoc must not be null.");
      }
      TransformerFactory tfactory = TransformerFactory.newInstance();
      if(!tfactory.getFeature(DOMSource.FEATURE))
      {
         throw new org.xml.sax.SAXNotSupportedException(
                "DOM node processing not supported!");
      }

      Templates templates;
      Document outNode = PSXmlDocumentBuilder.createXmlDocument();
      DOMSource dsource = new DOMSource(xslDoc);
      templates = tfactory.newTemplates(dsource);

      templates.newTransformer().transform(
         new DOMSource(srcDoc), new DOMResult(outNode));

      return outNode;
   }

   /**
    * Name of the stylesheet file that is used to transform the table fatcory
    * generated XML doc to another XML doc that has just keyset. The DTD for this
    * document produces and XML document of the form:
    * &lt;keys&gt;
    * &lt;key name='psx.workflow.1.3@Draft'&gt;Name of the first state in the
    * workflow&lt;/key&gt;
    * &lt;key name='psx.keyword.101.3@Business'&gt;Name of the keyword for field
    * categories used in content editor&lt;/key&gt;
    * ...
    * &lt;/keys&gt;
    */
   private static final String CMSTABLEDATA_TRANSFORM_XSL = "cmstabledatatransform.xsl";

   /**
    * Default name of section that is implemented by this class. Overridden during
    * processing by the name specified in the config element.
    * @see #process
    */
   private static String ms_SectionName = "CMS Tables";

   /**
    * DOM Document for the XSL stylesheet that is run to produce required keys
    * of the XML document produced by table factory. Never <code>null</code>
    * after this class object is initialized.
    */
   private static Document ms_XslDoc = null;
   /**
    * List of table to be process for key generation. Never <code>null</code>,
    * initially <code>empty</code>, filled during construction of this class
    * object.
    */
   private static List ms_Tables = new ArrayList();
}
