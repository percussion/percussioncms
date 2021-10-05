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
package com.percussion.data.jdbc;

import com.percussion.cms.IPSConstants;
import com.percussion.data.vfs.PSVirtualApplicationDirectory;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSUserSession;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *   Unit tests for the PSXmlDatabaseMetaDataTest class
 */

public class PSXmlDatabaseMetaDataTest
{
   private static final Logger log = LogManager.getLogger(IPSConstants.TEST_LOG);

   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   /** true if we already initialized */
   private static boolean ms_inited = false;

   /** the root dir for the tests */
   private static File ms_rootDir;

   /** the connection properties for the single root elem doc */
   private static Properties ms_connProperties;

   /** a document with a single root element and nothing else */
   private static String ms_singleElementFileName;

   /** the expected fields of the single root element doc */
   private static HashMap<String, Boolean> ms_singleElementExpectedFields;

   /** a document with multiple elements */
   private static String ms_multiElementsFileName;

   /** the expected fields of the multi elements doc */
   private static HashMap<String,Boolean> ms_multiElementsExpectedFields;

   /** the expected fields for the CGI variables column request */
   private static HashMap<String,Boolean> ms_cgiVarExpectedFields;


   public PSXmlDatabaseMetaDataTest()
   {
      init();
   }

   private static class DirMap extends PSVirtualApplicationDirectory
   {
      public DirMap(File f)
      {
         super(f.getName(), f, null);
      }

      public boolean hasPermissions(PSUserSession session, int permissions)
      {
         return true;
      }
   }

   /**
    *   Set up the testing directories and files
    */
   @Before
   public void init()
   {
      if (ms_inited)
         return;
      try
      {
         // make sure the class for the JDBC driver gets loaded
         PSXmlDriver d = new PSXmlDriver();

         ms_singleElementFileName = "SingleEl.xml";
         ms_multiElementsFileName = "MultiEl.xml";
         tempFolder.create();
         ms_rootDir = tempFolder.newFolder("Testing");
         ms_rootDir.deleteOnExit();
         assertTrue(ms_rootDir.exists());

         try
         {
            PSFileSystemDriver.addVirtualDirectory(new DirMap(ms_rootDir));
         }
         catch (Exception e)
         {
            throw new RuntimeException(e.toString());
         }

         ms_connProperties = new Properties();
         ms_connProperties.setProperty("catalog", ms_rootDir.getName());

         // create a document with a single element
         File f = new File(ms_rootDir, ms_singleElementFileName);
         f.deleteOnExit();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element docRoot = PSXmlDocumentBuilder.createRoot(doc, "SingleElementDocument");
         try(FileOutputStream out = new FileOutputStream(f)) {
            PSXmlDocumentBuilder.write(doc, out);
         }

         ms_singleElementExpectedFields = new HashMap<>();
         ms_singleElementExpectedFields.put("SingleElementDocument", Boolean.TRUE);

         // create a document with multiple nested elements
         f = new File(ms_rootDir, ms_multiElementsFileName);
         f.deleteOnExit();
         doc = PSXmlDocumentBuilder.createXmlDocument();
         docRoot = PSXmlDocumentBuilder.createRoot(doc, "MultiElementDocument");
         Element el = 
            PSXmlDocumentBuilder.addEmptyElement(doc, docRoot, "Child0");
         for (int i = 0; i < 10; i++)
         {
            Element subEl = null;
            if (shouldHaveData(i, i + 1))
            {
               subEl = PSXmlDocumentBuilder.addElement(doc,   el, "SubChild" + i, "some pcdata");
            }
            else
            {
               subEl = PSXmlDocumentBuilder.addEmptyElement(doc,   el, "SubChild" + i);
            }

            for (int j = 0; j < 10; j++)
            {
               Element subSubEl = null;
               if (shouldHaveData(i, j))
               {
                  subSubEl   = PSXmlDocumentBuilder.addElement
                     (doc,   subEl, "SubSubChild" + i + "_" + j, "some pcdata");
               }
               else
               {
                  subSubEl   = PSXmlDocumentBuilder.addEmptyElement
                     (doc,   subEl, "SubSubChild" + i + "_" + j);
               }
               if (shouldHaveAttribute(i, j))
               {
                  subSubEl.setAttribute("id", "" + (i * 10 + j));
               }
            }
         } 
         try(FileOutputStream out = new FileOutputStream(f)) {
            PSXmlDocumentBuilder.write(doc, out);
         }

         // now create the desired list of fields that should be returned when this
         // document is cataloged, using a similar method to how we created the
         // document
         ms_multiElementsExpectedFields = new HashMap<>();
         for (int i = 0; i < 10; i++)
         {
            if (shouldHaveData(i, i + 1))
            {
               ms_multiElementsExpectedFields.put("MultiElementDocument/Child0/SubChild" + i, Boolean.TRUE);
            }
            for (int j = 0; j < 10; j++)
            {
               // this always is expected because elements at this level are leaves
               ms_multiElementsExpectedFields.put(
                  "MultiElementDocument/Child0/SubChild" + i + "/SubSubChild" + i + "_" + j, Boolean.TRUE);
               
               if (shouldHaveAttribute(i, j))
               {
                  ms_multiElementsExpectedFields.put(
                     "MultiElementDocument/Child0/SubChild" + i + "/SubSubChild" + i + "_" + j + "/@" + "id", Boolean.TRUE);               
               }
            }
         }

         ms_cgiVarExpectedFields = new HashMap<>();
         Class<IPSCgiVariables> serverVariables = IPSCgiVariables.class;
         Field[] serverVariableFields = serverVariables.getFields();

         for (Field serverVariableField : serverVariableFields) {
            ms_cgiVarExpectedFields.put(
                    serverVariableField.get(null).toString(), Boolean.TRUE);
         }

         ms_inited = true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private static boolean shouldHaveData(int i, int j)
   {
      return ((i + j) % 2) == 0;
   }

   private static boolean shouldHaveAttribute(int i, int j)
   {
      return ((i + j) % 6) == 0;
   }

   @Test
   public void testGetColumnsSingleElement() throws Exception
   {
      Connection xmlConn = DriverManager.getConnection("jdbc:psxml",
         ms_connProperties);
      assertTrue(xmlConn instanceof com.percussion.data.jdbc.PSXmlConnection);

      DatabaseMetaData md = xmlConn.getMetaData();
      assertTrue(md instanceof com.percussion.data.jdbc.PSXmlDatabaseMetaData);
      
      ResultSet rs = md.getColumns(
         ms_rootDir.getName(), "%", ms_singleElementFileName, "%");

      testGetColumns(rs, ms_singleElementExpectedFields);

      xmlConn.close();
   }

   @Test
   public void testGetColumnsMultiElements() throws Exception
   {
      Connection xmlConn = DriverManager.getConnection("jdbc:psxml",
         ms_connProperties);
      assertTrue(xmlConn instanceof com.percussion.data.jdbc.PSXmlConnection);
      
      DatabaseMetaData md = xmlConn.getMetaData();
      assertTrue(md instanceof com.percussion.data.jdbc.PSXmlDatabaseMetaData);

      ResultSet rs = md.getColumns(
         ms_rootDir.getName(), "%", ms_multiElementsFileName, "%");

      testGetColumns(rs, ms_multiElementsExpectedFields);

      xmlConn.close();
   }

   @org.junit.Test
   public void testGetColumnsCgiVars() throws Exception
   {
      Connection xmlConn = DriverManager.getConnection("jdbc:psxml",
         ms_connProperties);
      assertTrue(xmlConn instanceof com.percussion.data.jdbc.PSXmlConnection);
      
      DatabaseMetaData md = xmlConn.getMetaData();
      assertTrue(md instanceof com.percussion.data.jdbc.PSXmlDatabaseMetaData);

      ResultSet rs = md.getColumns(
         ms_rootDir.getName(), "%", "PSXCgiVar", "%");

      testGetColumns(rs, ms_cgiVarExpectedFields);

      xmlConn.close();

   }

   @Test
   public void testGetTables() throws Exception
   {
      Connection xmlConn = DriverManager.getConnection("jdbc:psxml",
         ms_connProperties);
      assertTrue(xmlConn instanceof com.percussion.data.jdbc.PSXmlConnection);
      
      DatabaseMetaData md = xmlConn.getMetaData();
      assertTrue(md instanceof com.percussion.data.jdbc.PSXmlDatabaseMetaData);

      ResultSet rs = md.getTables(
         ms_rootDir.getName(), null, "%", null);
      
      while (rs.next())
      {
         log.info(rs.getString(1) + "\t"
            + rs.getString(2) + "\t" + rs.getString(3) + "\t"
            + rs.getString(4) + "\t" + rs.getString(5));
      }

      xmlConn.close();
   }


   private void testGetColumns(ResultSet rs, HashMap<String,Boolean> expectedFields)
      throws SQLException
   {
      HashMap<String,Boolean> ef = new HashMap<>(expectedFields);
      while (rs.next())
      {
         String table_cat = rs.getString(1);
         String table_schem = rs.getString(2);
         String table_name = rs.getString(3);
         String col_name = rs.getString(4);
         log.info(table_cat + "\t" + table_schem + "\t" +
            table_name + "\t" + col_name);
          assertTrue(col_name, ef.containsKey(col_name));
         ef.remove(col_name);
         assertFalse(ef.containsKey(col_name));
      }
      assertTrue(ef.toString(), ef.isEmpty());
   }


}
