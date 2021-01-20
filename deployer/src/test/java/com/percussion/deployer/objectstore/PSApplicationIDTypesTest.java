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


package com.percussion.deployer.objectstore;

import com.percussion.deployer.objectstore.idtypes.PSAppCEItemIdContext;
import com.percussion.deployer.objectstore.idtypes.PSApplicationIdContext;
import com.percussion.server.PSServer;
import com.percussion.util.PSIteratorUtils;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSApplicationIDTypes</code> class.
 */
@Category(IntegrationTest.class)
public class PSApplicationIDTypesTest
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
   public PSApplicationIDTypesTest()
   {
   }

   /**
    * Test constructing this object using parameters
    *
    * @throws Exception If there are any errors.
    */
  @Test
   public void testConstructor() throws Exception
   {
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
      
      PSDeployableElement de2 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "2", "TestElem2", "Test Element2", 
         "myTestElement2", false, false, false);
      
      PSDeployableObject do1 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "1", "TestObj1", "Test Object1", 
         "myTestObject1", true, false, true);
         
      PSDeployableObject do2 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "2", "TestObj2", "Test Object2", 
         "myTestObject2", false, false, true);
         
      File file = new File(PSServer.getRxDir().getAbsolutePath(),
            "rx_resources\\ewebeditpro\\config.xml");
      do1.setDependencies(PSIteratorUtils.emptyIterator());
      PSUserDependency userDep1 = do1.addUserDependency(file);
      
      // these should work fine
      assertTrue(testCtorValid(de1, "res_1", "elem_1"));
      assertTrue(testCtorValid(do1, "res_2", "elem_2"));

      // should be a problem
      assertTrue(!testCtorValid(null, "foo", "type"));
      assertTrue(!testCtorValid(de2, "foo", "type"));
      assertTrue(!testCtorValid(do2, "foo", "type"));
      assertTrue(!testCtorValid(userDep1, "foo", "type"));
      assertTrue(!testCtorValid(de1, null, "type"));
      assertTrue(!testCtorValid(de1, "", "type"));
      assertTrue(!testCtorValid(de1, "foo", null));
      assertTrue(!testCtorValid(de1, "foo", ""));
   }

   /**
    * Tests the equals and copy from methods
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testEquals() throws Exception
   {
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
      
      PSDeployableElement de2 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "2", "TestElem2", "Test Element2", 
         "myTestElement2", true, false, false);
      
      PSDeployableObject do1 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "1", "TestObj1", "Test Object1", 
         "myTestObject1", true, false, true);
      
      PSAppCEItemIdContext ctx0 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_VISIBILITY_RULE);
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSAppCEItemIdContext ctx2_1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION);
      PSAppCEItemIdContext ctx2_2 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
      PSApplicationIDTypes idtype1 = createIDTypes(de1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      PSApplicationIDTypes idtype2 = createIDTypes(de1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      assertEquals(idtype1, idtype2);

      PSApplicationIDTypes idtype1_diffname = createIDTypes(de2,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      assertTrue(!idtype1.equals(idtype1_diffname));

      idtype2 = createIDTypes(do1,
         "r1", "e1", ctx0, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      assertTrue(!idtype1.equals(idtype2));

      PSApplicationIDTypes idtype3 = createIDTypes(de1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      idtype3.removeMapping("r2", "e2", ctx2_1);
      assertTrue(!idtype1.equals(idtype3));

      idtype1.removeMapping("r2", "e2", ctx2_1);
      assertTrue(idtype1.equals(idtype3));
   }

   /**
    * Tests the <code>isComplete(...)</code> and <code>get...List</code> methods
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testCompleteness() throws Exception
   {
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
      
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSAppCEItemIdContext ctx2_1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION);
      PSAppCEItemIdContext ctx2_2 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
      PSApplicationIDTypes idtype1 = createIDTypes(de1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");

      assertTrue(!idtype1.isComplete());
      assertTrue(!idtype1.isComplete("r1"));
      assertTrue(!idtype1.isComplete("r2", "e2"));


      Iterator mappingList = idtype1.getIdTypeMappings("r1", "e1", false);
      PSApplicationIDTypeMapping mapping1 =
         (PSApplicationIDTypeMapping) mappingList.next();
      mapping1.setType("t1");

      assertTrue(idtype1.isComplete("r1"));
      assertTrue(!idtype1.isComplete());
      assertTrue(!idtype1.isComplete("r2", "e2"));

      mappingList = idtype1.getIdTypeMappings("r2", "e2", false);
      mapping1 = (PSApplicationIDTypeMapping) mappingList.next();
      mapping1.setType("t2_1");
      mapping1 = (PSApplicationIDTypeMapping) mappingList.next();
      mapping1.setType("t2_2");

      assertTrue(idtype1.isComplete("r2", "e2"));

   }

   /**
    * Tests the <code>get...List</code> methods
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testGetList() throws Exception
   {
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
      
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSAppCEItemIdContext ctx2_1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION);
      PSAppCEItemIdContext ctx2_2 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
      PSApplicationIDTypes idtype1 = createIDTypes(de1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");

      Iterator mappingList = idtype1.getIdTypeMappings("r1", "e1", false);
      PSApplicationIDTypeMapping mapping1 =
         (PSApplicationIDTypeMapping) mappingList.next();
      mapping1.setType("t1");

      // testing get...list
      Iterator resList = idtype1.getResourceList(true);
      String resourceName = (String) resList.next();
      assertTrue(resourceName.equals("r2"));
      assertTrue(!resList.hasNext());

      Iterator elemList = idtype1.getElementList("r2", true);
      String elemName = (String) elemList.next();
      assertTrue(elemName.equals("e2"));
      assertTrue(!elemList.hasNext());

      //test containment
      assertTrue(idtype1.containsMapping("r2", "e2", ctx2_1));
   }

   /**
    * Tests all Xml functions, and uses equals as well.
    *
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      PSDeployableElement de1 = new PSDeployableElement(
         PSDependency.TYPE_SHARED, "1", "TestElem", "Test Element", 
         "myTestElement", true, false, false);
      
      PSDeployableObject do1 = new PSDeployableObject(
         PSDependency.TYPE_LOCAL, "1", "TestObj1", "Test Object1", 
         "myTestObject1", true, false, true);
      
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSAppCEItemIdContext ctx2_1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION);
      PSAppCEItemIdContext ctx2_2 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
         
      PSApplicationIDTypes src = createIDTypes(de1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);
      PSApplicationIDTypes tgt = new PSApplicationIDTypes(srcEl);
      assertTrue(src.equals(tgt));
      
      src = createIDTypes(do1,
         "r1", "e1", ctx1, "v1", "r2", "e2", ctx2_1, "v2_1", ctx2_2, "v2_2");
      doc = PSXmlDocumentBuilder.createXmlDocument();
      srcEl = src.toXml(doc);
      tgt = new PSApplicationIDTypes(srcEl);
      assertTrue(src.equals(tgt));
      
   }

   /**
    * Constructs a <code>PSApplicationIDTypes</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSApplicationIDTypes} ctor.
    *
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   @Test
   private boolean testCtorValid(PSDependency dep, String res, String elem)
   {
      try
      {
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
         PSApplicationIDTypes idtypes = new PSApplicationIDTypes(dep);
         PSApplicationIDTypeMapping mapping =
            new PSApplicationIDTypeMapping(ctx1, "value_1");
         idtypes.addMapping(res, elem, mapping);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }

   /**
    * This is a helper method to construct a <code>PSApplicationIDTypes</code>
    * object using the supplied params. Expecting valid parameters.
    *
    * @return <code>PSApplicationIDTypes</code> object.
    */
   private PSApplicationIDTypes createIDTypes(PSDependency dep,
      String r1, String e1, PSApplicationIdContext ctx1, String v1,
      String r2, String e2, PSApplicationIdContext ctx2_1, String v2_1, 
      PSApplicationIdContext ctx2_2, String v2_2)
   {
      PSApplicationIDTypes idtype =
         new PSApplicationIDTypes(dep);
      PSApplicationIDTypeMapping mapping1 =
         new PSApplicationIDTypeMapping(ctx1, v1);
      PSApplicationIDTypeMapping mapping2_1 =
         new PSApplicationIDTypeMapping(ctx2_1, v2_1);
      PSApplicationIDTypeMapping mapping2_2 =
         new PSApplicationIDTypeMapping(ctx2_2, v2_2);

      idtype.addMapping(r2, e2, mapping2_1);
      idtype.addMapping(r2, e2, mapping2_2);
      idtype.addMapping(r1, e1, mapping1);

      return idtype;
   }

}
