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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for <code>PSContentEditorMapper</code>.
 */
public class PSContentEditorMapperTest extends TestCase
{
   /**
    * Case tests object->xml->object serialization.The mapper is built as a Java
    * object. Converted to XML document using toXml() method. Then the object is
    * restored from this xml document and compared with original object using
    * equals() method.
    * 
    * @throws PSUnknownNodeTypeException if construction of the object from XML
    *            document fails
    */
   public void testXml() throws PSUnknownNodeTypeException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "Test");

      PSFieldSet fs = createFieldSet();
      PSUIDefinition uiDef = createUIDefinition();

      ArrayList<String> systemExcludes = new ArrayList<String>();
      systemExcludes.add("exclude1");
      systemExcludes.add("exclude2");

      ArrayList<String> sharedIncludes = new ArrayList<String>();
      sharedIncludes.add("include1");
      sharedIncludes.add("include2");

      PSContentEditorMapper testTo = new PSContentEditorMapper(null, null, fs,
            uiDef);
      testTo.setSystemFieldExcludes(systemExcludes);
      // testTo.setSharedFieldIncludes(sharedIncludes);
      Element elem = testTo.toXml(doc);
      root.appendChild(elem);

      // create a new object and populate it from our testTo element
      PSContentEditorMapper testFrom = new PSContentEditorMapper(elem, null,
            null);
      Document doc2 = PSXmlDocumentBuilder.createXmlDocument();
      Element root2 = PSXmlDocumentBuilder.createRoot(doc2, "Test");
      Element elem2 = testFrom.toXml(doc2);
      root2.appendChild(elem2);
      assertTrue(testTo.equals(testFrom));
   }

   /**
    * Tests that excluding a specific shared field results in that field being
    * excluded from the merged field set and UI defintion.
    */
   public void testMergeForInclusionOfSharedGroupWithExcludedField()
         throws Exception
   {
      // now include a shared field group
      ArrayList<String> sharedGroupIncludes = new ArrayList<String>();
      sharedGroupIncludes.add("shared");
      m_testMapper.setSharedFieldIncludes(sharedGroupIncludes);

      ArrayList<String> sharedFieldExcludes = new ArrayList<String>();
      sharedFieldExcludes.add("body");
      m_testMapper.setSharedFieldExcludes(sharedFieldExcludes);

      PSContentEditorMapper mergedCem = m_testMapper.getMergedMapper(ms_sysDef,
            ms_sharedDef, true);
      assertNotNull(mergedCem);

      // merged fields should have local, all system, shared (except excluded)
      PSFieldSet mergedFs = mergedCem.getFieldSet();
      assertTrue(mergedFs.getAllFields().length > 1);
      assertTrue(mergedFs.contains("field_1"));
      assertTrue(mergedFs.contains("sys_communityid"));
      assertTrue(mergedFs.contains("shared"));
      PSFieldSet shared = (PSFieldSet) mergedFs.get("shared");
      assertNotNull(shared);
      assertTrue(shared.contains("displaytitle"));
      assertFalse(shared.contains("body")); // excluded field

      // merged display should have local, all system, shared (except excluded)
      PSUIDefinition mergedUi = mergedCem.getUIDefinition();
      PSDisplayMapper display = mergedUi.getDisplayMapper();
      assertNotNull(display);
      assertNotNull(display.getMapping("field_1"));
      assertNotNull(display.getMapping("sys_communityid"));
      assertNotNull(display.getMapping("sys_workflowid"));
      assertNotNull(display.getMapping("sys_pubdate"));
      assertNotNull(display.getMapping("displaytitle"));
      assertNull(display.getMapping("body")); // excluded field
   }

   /**
    * Tests that system fields are included in the merged field set and UI
    * definition.
    */
   public void testMergeForInclusionOfSystemFields() throws Exception
   {
      PSContentEditorMapper mergedCem = m_testMapper.getMergedMapper(ms_sysDef,
            ms_sharedDef, true);
      assertNotNull(mergedCem);
      // original mapper should not change
      assertEquals(1, m_testMapper.getFieldSet().getAllFields().length);

      // merged fields should have local, all system, no shared
      PSFieldSet mergedFs = mergedCem.getFieldSet();
      assertTrue(mergedFs.getAllFields().length > 1);
      assertTrue(mergedFs.contains("field_1"));
      assertTrue(mergedFs.contains("sys_communityid"));
      assertTrue(mergedFs.contains("sys_workflowid"));
      assertTrue(mergedFs.contains("sys_pubdate"));
      assertFalse(mergedFs.contains("shared"));

      // merged display should have local, all system, no shared
      PSUIDefinition mergedUi = mergedCem.getUIDefinition();
      PSDisplayMapper display = mergedUi.getDisplayMapper();
      assertNotNull(display);
      assertNotNull(display.getMapping("field_1"));
      assertNotNull(display.getMapping("sys_communityid"));
      assertNotNull(display.getMapping("sys_workflowid"));
      assertNotNull(display.getMapping("sys_pubdate"));
      assertNull(display.getMapping("body"));
   }

   /**
    * Tests that excluding a specific system field results in that field being
    * excluded from the merged field set and UI defintion.
    */
   public void testMergeForInclusionOfSystemFieldsWithExcludedField()
         throws Exception
   {
      // add an excluded system field to mapper
      ArrayList<String> systemFieldExcludes = new ArrayList<String>();
      systemFieldExcludes.add("sys_pubdate");
      m_testMapper.setSystemFieldExcludes(systemFieldExcludes);

      PSContentEditorMapper mergedCem = m_testMapper.getMergedMapper(ms_sysDef,
            ms_sharedDef, true);
      assertNotNull(mergedCem);
      // original mapper should not change
      assertEquals(1, m_testMapper.getFieldSet().getAllFields().length);

      // merged fields should have local, system (except excluded), no shared
      PSFieldSet mergedFs = mergedCem.getFieldSet();
      assertTrue(mergedFs.getAllFields().length > 1);
      assertTrue(mergedFs.contains("field_1"));
      assertTrue(mergedFs.contains("sys_communityid"));
      assertTrue(mergedFs.contains("sys_workflowid"));
      assertFalse(mergedFs.contains("sys_pubdate")); // excluded field
      assertFalse(mergedFs.contains("shared"));

      // merged display should have local, system (except excluded), no shared
      PSUIDefinition mergedUi = mergedCem.getUIDefinition();
      PSDisplayMapper display = mergedUi.getDisplayMapper();
      assertNotNull(display);
      assertNotNull(display.getMapping("field_1"));
      assertNotNull(display.getMapping("sys_communityid"));
      assertNotNull(display.getMapping("sys_workflowid"));
      assertNull(display.getMapping("sys_pubdate")); // excluded field
      assertNull(display.getMapping("body"));
   }

   /**
    * Tests that including a shared group adds its fields to merged field set 
    * and UI definition.
    */
   public void testMergeForInclusionOfEntireSharedGroup() throws Exception
   {
      // include a shared group
      ArrayList<String> sharedGroupIncludes = new ArrayList<String>(1);
      sharedGroupIncludes.add("shared");
      m_testMapper.setSharedFieldIncludes(sharedGroupIncludes);

      PSContentEditorMapper mergedCem = m_testMapper.getMergedMapper(ms_sysDef,
            ms_sharedDef, true);
      assertNotNull(mergedCem);

      // merged fields should have local, system, and shared
      PSFieldSet mergedFs = mergedCem.getFieldSet();
      assertTrue(mergedFs.getAllFields().length > 1);
      assertTrue(mergedFs.contains("field_1"));
      assertTrue(mergedFs.contains("sys_communityid"));
      assertTrue(mergedFs.contains("shared"));
      PSFieldSet shared = (PSFieldSet) mergedFs.get("shared");
      assertNotNull(shared);
      assertTrue(shared.contains("body"));

      // merged UI should have original, system, and shared
      PSUIDefinition mergedUi = mergedCem.getUIDefinition();
      PSDisplayMapper display = mergedUi.getDisplayMapper();
      assertNotNull(display);
      assertNotNull(display.getMapping("field_1"));
      assertNotNull(display.getMapping("sys_communityid"));
      assertNotNull(display.getMapping("body"));
      
      // merged display order within a group should match definition
      boolean foundFilename = false;
      boolean foundBody = false;
      for (Iterator it = display.iterator(); it.hasNext();)
      {
         PSDisplayMapping mapping = (PSDisplayMapping) it.next();
         if (mapping.getFieldRef().equals("filename"))
         {
            foundFilename = true;
            assertFalse(foundBody); // should not have found yet
         }
         if (mapping.getFieldRef().equals("body"))
         {
            foundBody = true;      
            assertTrue(foundFilename); // should have already found
         }
      }
      assertTrue(foundBody);
      assertTrue(foundFilename);
   }

   /**
    * Tests that including two shared groups adds fields from both groups to
    * merged field set and UI definition.
    */
   public void testMergeForInclusionOfTwoSharedGroups()
         throws Exception
   {
      // include two shared groups
      ArrayList<String> sharedGroupIncludes = new ArrayList<String>(2);
      sharedGroupIncludes.add("shared");
      sharedGroupIncludes.add("sharedbinary");
      m_testMapper.setSharedFieldIncludes(sharedGroupIncludes);

      PSContentEditorMapper mergedCem = m_testMapper.getMergedMapper(ms_sysDef,
            ms_sharedDef, true);
      assertNotNull(mergedCem);

      // merged fields should have local, system, and both shared
      PSFieldSet mergedFs = mergedCem.getFieldSet();
      assertTrue(mergedFs.getAllFields().length > 1);
      assertTrue(mergedFs.contains("field_1"));
      assertTrue(mergedFs.contains("sys_communityid"));
      assertTrue(mergedFs.contains("shared"));
      PSFieldSet shared = (PSFieldSet) mergedFs.get("shared");
      assertNotNull(shared);
      assertTrue(shared.contains("body"));
      assertTrue(mergedFs.contains("sharedbinary"));
      PSFieldSet sharedbinary = (PSFieldSet) mergedFs.get("sharedbinary");
      assertNotNull(sharedbinary);
      assertTrue(sharedbinary.contains("item_filename"));

      // merged display should have local, system, and both shared
      PSUIDefinition mergedUi = mergedCem.getUIDefinition();
      PSDisplayMapper display = mergedUi.getDisplayMapper();
      assertNotNull(display);
      assertNotNull(display.getMapping("field_1"));
      assertNotNull(display.getMapping("sys_communityid"));
      assertNotNull(display.getMapping("body"));
      assertNotNull(display.getMapping("item_filename"));

      // merged display order within a group should match definition
      boolean foundDisplayTitle = false;
      boolean foundCallout = false;
      for (Iterator it = display.iterator(); it.hasNext();)
      {
         PSDisplayMapping mapping = (PSDisplayMapping) it.next();
         if (mapping.getFieldRef().equals("displaytitle"))
         {
            foundDisplayTitle = true;          
            assertFalse(foundCallout); // should not have found yet
         }
         if (mapping.getFieldRef().equals("callout"))
         {
            foundCallout = true;         
            assertTrue(foundDisplayTitle); // should have already found
         }
      }
      assertTrue(foundCallout);
      assertTrue(foundDisplayTitle);
   }

   private static PSContentEditorSharedDef createSharedDef() throws Exception
   {
      FileInputStream in = null;
      try
      {
         File file = new File(RESOURCE_BASE, "rxs_ct_shared.xml");
         in = new FileInputStream(file);
         Document xml = PSXmlDocumentBuilder.createXmlDocument(in, false);
         return new PSContentEditorSharedDef(xml);
      }
      finally
      {
         IOUtils.closeQuietly(in);
      }
   }

   private static PSContentEditorSystemDef createSystemDef() throws Exception
   {
      FileInputStream in = null;
      try
      {
         File file = new File(RESOURCE_BASE, "ContentEditorSystemDef.xml");
         in = new FileInputStream(file);
         Document xml = PSXmlDocumentBuilder.createXmlDocument(in, false);
         return new PSContentEditorSystemDef(xml);
      }
      finally
      {
         IOUtils.closeQuietly(in);
      }
   }

   private PSUIDefinition createUIDefinition()
   {
      PSDisplayMapper dispMapper = new PSDisplayMapper("fs_1");
      PSUISet uiSet = new PSUISet();
      PSParam param = new PSParam("param_1", new PSTextLiteral("value_1"));
      PSCollection parameters = new PSCollection(param.getClass());
      parameters.add(param);
      uiSet.setControl(new PSControlRef("sys_EditBox", parameters));
      PSDisplayMapping mapping = new PSDisplayMapping("field_1", uiSet);
      PSUIDefinition uiDef = new PSUIDefinition(dispMapper);
      uiDef.appendMapping(dispMapper, mapping);
      return uiDef;
   }

   private PSFieldSet createFieldSet()
   {
      PSBackEndCredential cred = new PSBackEndCredential("credential_1");
      cred.setDataSource("rxdefault");
      PSTableLocator tl = new PSTableLocator(cred);
      PSTableRef tr = new PSTableRef("tableName_1", "tableAlias_1");
      PSTableSet ts = new PSTableSet(tl, tr);
      PSCollection tsCol = new PSCollection(ts.getClass());
      tsCol.add(ts);
      // PSContainerLocator loc = new PSContainerLocator(tsCol);
      PSBackEndTable table = new PSBackEndTable("RXARTICLE");
      PSField f = new PSField("field_1", new PSBackEndColumn(table,
            "DISPLAYTITLE"));
      PSFieldSet fs = new PSFieldSet("fieldSet_1", f);
      return fs;
   }

   /**
    * The directory containing test resource files.
    */
   private static final String RESOURCE_BASE = "UnitTestResources/com/percussion/design/objectstore";

   private PSContentEditorMapper m_testMapper;

   private static PSContentEditorSystemDef ms_sysDef;

   private static PSContentEditorSharedDef ms_sharedDef;

   /**
    * Assigns test objects to system def, shared def, and CE mapper fields
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSFieldSet fieldSet = createFieldSet();
      PSUIDefinition uiDefinition = createUIDefinition();
      m_testMapper = new PSContentEditorMapper(null, null, fieldSet,
            uiDefinition);

      assertNotNull(m_testMapper);
      assertEquals(1, m_testMapper.getFieldSet().getAllFields().length);
      assertTrue(m_testMapper.getFieldSet().contains("field_1"));

      if (ms_sysDef == null)
         ms_sysDef = createSystemDef();
      assertNotNull(ms_sysDef);
      if (ms_sharedDef == null)
         ms_sharedDef = createSharedDef();
      assertNotNull(ms_sharedDef);

   }

}
