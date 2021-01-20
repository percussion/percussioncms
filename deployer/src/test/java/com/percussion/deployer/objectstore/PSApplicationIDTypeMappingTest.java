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
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test class for the <code>PSApplicationIDTypeMapping</code> class.
 */
public class PSApplicationIDTypeMappingTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSApplicationIDTypeMappingTest(String name)
   {
      super(name);
   }

   /**
    * Test constructing this object using parameters
    *
    * @throws Exception If there are any errors.
    */
   public void testConstructor() throws Exception
   {
      
      // these should work fine
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSAppCEItemIdContext ctx2 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
      assertTrue(testCtorValid(ctx1, "value_1", "type_1"));
      assertTrue(testCtorValid(ctx2, "value_2", "type_2"));

      // should be a problem
      assertTrue(!testCtorValid(null, "foo", "type"));
      assertTrue(!testCtorValid(ctx1, null, "type"));
      assertTrue(!testCtorValid(ctx1, "", "type"));
      assertTrue(!testCtorValid(ctx1, "foo", null));
      assertTrue(!testCtorValid(ctx1, "foo", ""));
   }

   /**
    * Tests the equals and copy from methods
    *
    * @throws Exception if there are any errors.
    */
   public void testEquals() throws Exception
   {
      
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSAppCEItemIdContext ctx2 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION);
      PSApplicationIDTypeMapping mapping1 =
         new PSApplicationIDTypeMapping(ctx1, "value_1");
      PSApplicationIDTypeMapping mapping2 =
         new PSApplicationIDTypeMapping(ctx1, "value_1");
      assertEquals(mapping1, mapping2);

      assertNull(mapping1.getParentId());
      assertNull(mapping2.getParentId());
      assertNull(mapping1.getParentType());
      assertNull(mapping2.getParentType());
      mapping2.setParent("35", "WorkflowDef");
      assertTrue(!mapping1.equals(mapping2));
      
      mapping1.setParent("35", "WorkflowDef");
      assertEquals(mapping1, mapping2);
      assertEquals(mapping1.getParentId(), mapping2.getParentId());
      assertEquals(mapping1.getParentType(), mapping2.getParentType());
      mapping1.setParent(null, null);
      assertTrue(!mapping1.equals(mapping2));
      mapping2.setParent(null, null);
      assertEquals(mapping1, mapping2);
      
      mapping2 = new PSApplicationIDTypeMapping(ctx2, "value_2");
      assertTrue(!mapping1.equals(mapping2));

      
      // check defined/undefined type
      assertTrue(!mapping1.hasDefinedType());

      mapping1.setType("type_2");
      assertTrue(mapping1.hasDefinedType());
   }

   /**
    * Tests defined and undefined (or mapped/unmapped)
    *
    * @throws Exception if there are any errors.
    */
   public void testTypeMapping() throws Exception
   {
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSApplicationIDTypeMapping mapping1 =
         new PSApplicationIDTypeMapping(ctx1, "value_1");

      assertTrue(!mapping1.hasDefinedType());

      mapping1.setType("type_1");
      assertTrue(mapping1.hasDefinedType());
   }
   /**
    * Tests all Xml functions, and uses equals as well.
    *
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSAppCEItemIdContext ctx1 = new PSAppCEItemIdContext(
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE);
      PSApplicationIDTypeMapping src =
         new PSApplicationIDTypeMapping(ctx1, "value_1");
      src.setType("type_1");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element srcEl = src.toXml(doc);
      PSApplicationIDTypeMapping tgt = new PSApplicationIDTypeMapping(srcEl);
      assertTrue(src.equals(tgt));
      
      src.setParent("301", "WorkflowDef");
      srcEl = src.toXml(doc);
      tgt = new PSApplicationIDTypeMapping(srcEl);
      assertTrue(src.equals(tgt));
      
   }

   /**
    * Constructs a <code>PSApplicationIDTypeMapping</code> object using the
    * supplied params and catches any exception.  For params,
    * see {@link PSApplicationIDTypeMapping} ctor.
    *
    * @return <code>true</code> if no exceptions were caught, <code>false</code>
    * otherwise.
    */
   private boolean testCtorValid(PSApplicationIdContext ctx, String value, 
      String type)
   {
      try
      {
         PSApplicationIDTypeMapping mapping =
            new PSApplicationIDTypeMapping(ctx, value);
         mapping.setType(type);
      }
      catch (Exception ex)
      {
         return false;
      }

      return true;
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSApplicationIDTypeMappingTest("testConstructor"));
      suite.addTest(new PSApplicationIDTypeMappingTest("testEquals"));
      suite.addTest(new PSApplicationIDTypeMappingTest("testTypeMapping"));
      suite.addTest(new PSApplicationIDTypeMappingTest("testXml"));
      return suite;
   }

}
