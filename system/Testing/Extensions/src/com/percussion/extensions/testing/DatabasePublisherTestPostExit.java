/**[ DatabasePublisherTestPostExit.java ]***************************************
 *
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * $Id: DatabasePublisherTestPostExit.java 1.5 2002/10/16 19:27:33Z mattboucher Exp $
 *
 * Version Labels : $Name: $
 *
 * Locked By      : $Locker: $
 *
 * Revision History:
 *   $Log: DatabasePublisherTestPostExit.java $
 *   Revision 1.5  2002/10/16 19:27:33Z  mattboucher
 *   fixed to compile, added new import
 *   Revision 1.4  2002/10/16 13:39:42Z  animeshkumar
 *   Fixed Xerces Wrong Document Error by importing the node.
 *   Revision 1.3  2002/05/13 22:10:46Z  martingenhart
 *   enable trasaction support only if schema changes are NOT allowed
 *
 *   Revision 1.2  2002/05/09 22:04:46Z  martingenhart
 *   transaction support must be enabled for oracle clobs and blobs
 *
 *   Revision 1.1  2002/04/18 21:05:53Z  martingenhart
 *   Initial revision
 *
 ******************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.data.PSConversionException;
import com.percussion.data.PSXslStyleSheetMerger;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.xml.PSXmlDocumentBuilder;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;

/**
 * This post exit processes all rows lookud up from the DatabasePublisherTests
 * table, reads the file from the application directory and executes the test.
 */
public class DatabasePublisherTestPostExit implements IPSResultDocumentProcessor
{
   /**
    * Return false (this extension can not modify the style sheet).
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * No-op
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {}

   /**
    * The input document must provide an element <TESTFILE>. Its text value
    * contains the path of the file that contains the tablefactory test. The
    * test can be provided complete, <tabledefset> and <tabledataset>, or with
    * only the <tabledataset> element. For the latter case we will add the
    * default tabledefset from DatabasePublisherTests/tabledefset.xml.
    *
    * Returns different documents depending on the test result. If the test was
    * o.k. the return document is:
    *    <TableFactoryTest>
    *       <result>successful<result>
    *    </TableFactoryTest>
    * If the test failed, the return document is:
    *    <TableFactoryTest>
    *       <file>DatabasePublisherTests/Test01.xml</file>
    *       <result>ERROR: + error message + statcktrace<result>
    *    </TableFactoryTest>
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document doc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // create empty test result document
      Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element tableFactoryTest = resultDoc.createElement("TableFactoryTest");
      resultDoc.appendChild(tableFactoryTest);

      // create an empty test file element, this will be added to the result on
      // errors.
      Element testFile = resultDoc.createElement("file");

      // create an empty test result element, this will be filled and added
      // to the document later.
      Element testResult = resultDoc.createElement("result");
      try
      {
         // get the test file path
         NodeList files = doc.getElementsByTagName("TESTFILE");
         Element file = (Element) files.item(0);
         String fileStr = ((Text) file.getFirstChild()).getData();

         // add the test file info to the result document
         testFile.appendChild(resultDoc.createTextNode(fileStr));

         // create the test document
         Document testDoc = PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(fileStr), false);
         NodeList tabledefsets = testDoc.getElementsByTagName("tabledefset");
         if (tabledefsets == null || tabledefsets.getLength() == 0)
         {
            Document tabledef = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream("DatabasePublisherTests/tabledefset.xml"), false);

            // add the default tabledefset if none was provided
            Element databasePublisher = testDoc.getDocumentElement();
            Node importNode = testDoc.importNode(
               tabledef.getDocumentElement(), true);
            databasePublisher.insertBefore(importNode,
               (Element) testDoc.getElementsByTagName("tabledataset").item(0));
         }

         // are schema changes allowed
         boolean allowSchemaChanges = false;
         Element tabledef =
            (Element) testDoc.getElementsByTagName("tabledef").item(0);
         if (tabledef != null)
         {
            String asc = tabledef.getAttribute("allowSchemaChanges");
            allowSchemaChanges = asc.equalsIgnoreCase("y");
         }


         // get the table definition document
         Document tableDef = transform(testDoc,
            "DatabasePublisherTests/PSJdbcTableDef.xsl");

         // get the table data document
         Document tableData = transform(testDoc,
            "DatabasePublisherTests/PSJdbcTableData.xsl");

         // perform the actual tablefactory test
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         PSJdbcDbmsDef def = new PSJdbcDbmsDef(getDbConnectionProps());
         PSJdbcTableFactory.processTables(def, null, tableDef, tableData,
            new PrintStream(os), true, !allowSchemaChanges);

         // fill in the test results
         testResult.appendChild(resultDoc.createTextNode("successful"));
      }
      catch (Throwable t)
      {
         StringWriter error = new StringWriter();
         error.write("ERROR: " + t.toString() + "\n");
         t.printStackTrace(new PrintWriter(error));
         error.flush();

         // for errors add the file and error elements
         tableFactoryTest.appendChild(testFile);
         testResult.appendChild(resultDoc.createTextNode(error.toString()));
         t.printStackTrace();
      }
      finally
      {
         // if successful add only the result element
         tableFactoryTest.appendChild(testResult);
         return resultDoc;
      }
   }

   /**
    * Get the database connection properties from the Rhythmyx server mapped
    * to the table factory property names.
    *
    * @return the database connection properties to use for the database
    *    publishing test, never <code>null</code>, might be empty.
    */
   private Properties getDbConnectionProps()
   {
      Properties dbProps = new Properties();

      Properties serverProps = PSServer.getServerProps();
      if (serverProps != null)
      {
         String[] serverPropList =
         {
            "driverType",
            "loggerClassname",
            "serverName",
            "databaseName",
            "schemaName",
            "loginId",
            "loginPw",
         };

         String[] dbPropList =
         {
            PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY,
            PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY,
            PSJdbcDbmsDef.DB_SERVER_PROPERTY,
            PSJdbcDbmsDef.DB_NAME_PROPERTY,
            PSJdbcDbmsDef.DB_SCHEMA_PROPERTY,
            PSJdbcDbmsDef.UID_PROPERTY,
            PSJdbcDbmsDef.PWD_PROPERTY,
         };

         for (int i=0; i<serverPropList.length; i++)
         {
            dbProps.setProperty(dbPropList[i],
               serverProps.getProperty(serverPropList[i]));
         }
      }

      return dbProps;
   }

   /**
    * Transform the porvided document using the supplied stylesheet.
    *
    * @param doc the document to transform, assumed not <code>null</code>.
    * @param stylesheet the stylesheet to use for the transformation, assumed
    *    not <code>null</code>.
    * @return the transformed document, never <code>null</code>.
    * @throws PSConversionException for any conversion errors.
    * @throws MalformedURLException for any URL errors.
    * @throws IOException for any IO errors.
    * @throws SAXException for all parser errors.
    */
   private Document transform(Document doc, String stylesheet)
      throws PSConversionException, MalformedURLException, IOException,
         SAXException
   {
      ByteArrayOutputStream os = null;
      try
      {
         os = new ByteArrayOutputStream();
         URL styleURL = new URL("file", "", stylesheet);
         PSXslStyleSheetMerger merger = new PSXslStyleSheetMerger();
         merger.merge(null, doc, os, styleURL, null);

         InputStream is = new ByteArrayInputStream(os.toByteArray());
         return PSXmlDocumentBuilder.createXmlDocument(is, false);
      }
      finally
      {
         if (os != null)
            try { os.close(); } catch (IOException e) { /* ignore */ }
      }
   }
}

