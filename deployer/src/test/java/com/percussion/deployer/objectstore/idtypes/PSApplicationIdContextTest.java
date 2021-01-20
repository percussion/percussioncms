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

 
package com.percussion.deployer.objectstore.idtypes;

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.PSXmlField;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit test for the {@link PSApplicationIdContext} class.
 */
public class PSApplicationIdContextTest extends TestCase
{
   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSApplicationIdContextTest(String name)
   {
      super(name);
   }

   /**
    * Test serializing to and from xml, as well as equals method.
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      PSAppCEItemIdContext ceItemctx;
      int[] fieldTypes = 
      {
         PSAppCEItemIdContext.TYPE_APPLY_WHEN,
         PSAppCEItemIdContext.TYPE_CHOICES,
         PSAppCEItemIdContext.TYPE_DATA_LOCATOR,
         PSAppCEItemIdContext.TYPE_DEFAULT_UI,
         PSAppCEItemIdContext.TYPE_DEFAULT_VALUE,
         PSAppCEItemIdContext.TYPE_INPUT_TRANSLATION,
         PSAppCEItemIdContext.TYPE_OUTPUT_TRANSLATION,
         PSAppCEItemIdContext.TYPE_VALIDATION_RULE,
         PSAppCEItemIdContext.TYPE_VISIBILITY_RULE,
         PSAppCEItemIdContext.TYPE_READ_ONLY_RULES,
         PSAppCEItemIdContext.TYPE_CHOICE_FILTER
      };
      for (int i = 0; i < fieldTypes.length; i++) 
      {
         ceItemctx = new PSAppCEItemIdContext(fieldTypes[i]);
         assertTrue(ceItemctx.equals(testXml(ceItemctx)));
      }
      
      PSAppNamedItemIdContext namedItemIdCtx;
      int[] namedTypes = 
      { 
         PSAppNamedItemIdContext.TYPE_APP_FLOW,
         PSAppNamedItemIdContext.TYPE_CE_FIELD,
         PSAppNamedItemIdContext.TYPE_CHILD_ITEM,
         PSAppNamedItemIdContext.TYPE_CONTROL,
         PSAppNamedItemIdContext.TYPE_DISPLAY_MAPPING,
         PSAppNamedItemIdContext.TYPE_FIELD_SET,
         PSAppNamedItemIdContext.TYPE_FUNCTION_CALL,
         PSAppNamedItemIdContext.TYPE_ITEM_FIELD,
         PSAppNamedItemIdContext.TYPE_PARAM,
         PSAppNamedItemIdContext.TYPE_PROCESS_CHECK,
         PSAppNamedItemIdContext.TYPE_PSPROPERTY,
         PSAppNamedItemIdContext.TYPE_RESULT_PAGE,
         PSAppNamedItemIdContext.TYPE_SIMPLE_CHILD_VALUE,
         PSAppNamedItemIdContext.TYPE_STYLESHEET_SET,
         PSAppNamedItemIdContext.TYPE_SYS_DEF_INIT_PARAMS,
         PSAppNamedItemIdContext.TYPE_SYS_DEF_INPUT_DATA_EXITS,
         PSAppNamedItemIdContext.TYPE_SYS_DEF_RESULT_DATA_EXITS
      };
      
      for (int i = 0; i < namedTypes.length; i++) 
      {
         namedItemIdCtx = new PSAppNamedItemIdContext(namedTypes[i], 
            "name" + i);
         assertTrue(namedItemIdCtx.equals(testXml(namedItemIdCtx)));
      }
      
      PSAppIndexedItemIdContext indexedItemIdCtx;
      int[] indexedTypes = 
      { 
         PSAppIndexedItemIdContext.TYPE_CONDITIONAL_EXIT,
         PSAppIndexedItemIdContext.TYPE_CONDITIONAL_REQUEST,
         PSAppIndexedItemIdContext.TYPE_CONDITIONAL_STYLESHEET,
         PSAppIndexedItemIdContext.TYPE_CUSTOM_ACTION_GROUP,
         PSAppIndexedItemIdContext.TYPE_RULE
      };
      for (int i = 0; i < indexedTypes.length; i++) 
      {
         indexedItemIdCtx = new PSAppIndexedItemIdContext(indexedTypes[i], i);
         assertTrue(indexedItemIdCtx.equals(testXml(indexedItemIdCtx)));
      }
      
      
      
      PSAppConditionalIdContext ceCondctx = new PSAppConditionalIdContext(
         new PSConditional(new PSTextLiteral("a"), PSConditional.OPTYPE_EQUALS, 
            new PSHtmlParameter("b")), PSAppConditionalIdContext.TYPE_VALUE);
      assertTrue(ceCondctx.equals(testXml(ceCondctx)));

      PSExtensionParamValue[] params = new PSExtensionParamValue[2];
      params[0] = new PSExtensionParamValue(new PSHtmlParameter("foo"));
      params[1] = new PSExtensionParamValue(new PSTextLiteral("bar"));
      PSExtensionRef ref = new PSExtensionRef("java", "myctx", "myExt");
      PSExtensionCall call = new PSExtensionCall(ref, params);
      PSAppExtensionCallIdContext extCallCtx = 
         new PSAppExtensionCallIdContext(call);
      assertTrue(extCallCtx.equals(testXml(extCallCtx)));
      PSAppExtensionCallIdContext extCallCtx2 = 
         new PSAppExtensionCallIdContext(call, 3);
      assertTrue(extCallCtx2.equals(testXml(extCallCtx2)));
      
      
      PSAppExtensionParamIdContext extParamCtx = 
         new PSAppExtensionParamIdContext(0, params[0]);
      assertTrue(extParamCtx.equals(testXml(extParamCtx)));
      PSAppExtensionParamIdContext extParamCtx2 = 
         new PSAppExtensionParamIdContext(0, params[0]);
      assertEquals(extParamCtx, extParamCtx2);
      extParamCtx2.setParamName("paramName");
      // name should be ignored in equals
      assertEquals(extParamCtx, extParamCtx2);
      assertEquals(extParamCtx2.getParamName(), "paramName");
      assertTrue(extParamCtx2.equals(testXml(extParamCtx2)));
      

      PSUISet uiSet = new PSUISet();
      PSAppUISetIdContext ceUISetCtx = new PSAppUISetIdContext(uiSet);
      assertTrue(ceUISetCtx.equals(testXml(ceUISetCtx)));
      uiSet.setName("test");
      PSAppUISetIdContext ceUISetCtx2 = new PSAppUISetIdContext(uiSet);
      assertTrue(ceUISetCtx2.equals(testXml(ceUISetCtx2)));
      assertTrue(!ceUISetCtx.equals(testXml(ceUISetCtx2)));
      
      
      PSDisplayMapper dispMap = new PSDisplayMapper("test");
      dispMap.setId(2);
      PSAppDisplayMapperIdContext ceDispMapCtx = 
         new PSAppDisplayMapperIdContext(dispMap);
      assertTrue(ceDispMapCtx.equals(testXml(ceDispMapCtx)));

      
      PSEntry entry = new PSEntry("45", new PSDisplayText("label"));
      entry.setSequence(2);
      PSAppEntryIdContext entryCtx = 
         new PSAppEntryIdContext(entry);
      assertTrue(entryCtx.equals(testXml(entryCtx)));
      
      PSCollection urlparams = new PSCollection(PSParam.class);
      PSUrlRequest req = new PSUrlRequest("foo", null, urlparams);
      PSAppUrlRequestIdContext urlCtx = new PSAppUrlRequestIdContext(req);
      assertTrue(urlCtx.equals(testXml(urlCtx)));
      req = new PSUrlRequest(null, "bar", urlparams);
      urlCtx = new PSAppUrlRequestIdContext(req);
      assertTrue(urlCtx.equals(testXml(urlCtx)));
      req = new PSUrlRequest(null, null, urlparams);
      urlCtx = new PSAppUrlRequestIdContext(req);
      assertTrue(urlCtx.equals(testXml(urlCtx)));
      
      PSDataMapping dataMapping = new PSDataMapping(new PSXmlField("testXml"), 
         new PSTextLiteral("testLit"));
      PSAppDataMappingIdContext dataMappingCtx = new PSAppDataMappingIdContext(
         dataMapping, PSAppDataMappingIdContext.TYPE_BACK_END);
      assertTrue(dataMappingCtx.equals(testXml(dataMappingCtx)));
      PSAppDataMappingIdContext dataMappingCtx2 = new PSAppDataMappingIdContext(
         dataMapping, PSAppDataMappingIdContext.TYPE_XML);
      assertTrue(dataMappingCtx2.equals(testXml(dataMappingCtx2)));
      PSAppDataMappingIdContext dataMappingCtx3 = new PSAppDataMappingIdContext(
         dataMapping, PSAppDataMappingIdContext.TYPE_COND);
      assertTrue(dataMappingCtx3.equals(testXml(dataMappingCtx3)));
      
      // set parents
      extParamCtx.setParentCtx(extCallCtx);
      assertTrue(extParamCtx.equals(testXml(extParamCtx)));
      extCallCtx.setParentCtx(ceCondctx);
      assertTrue(extParamCtx.equals(testXml(extParamCtx)));
      
      assertTrue(extParamCtx.getNextRootCtx().equals(ceCondctx));
      assertTrue(extParamCtx.getCurrentRootCtx().equals(ceCondctx));
      assertTrue(extParamCtx.getNextRootCtx().equals(extCallCtx));
      assertTrue(extParamCtx.getCurrentRootCtx().equals(extCallCtx));
      assertTrue(extParamCtx.getNextRootCtx().equals(extParamCtx));
      assertTrue(extParamCtx.getCurrentRootCtx().equals(extParamCtx));
      
      extParamCtx.resetCurrentRootCtx();
      assertTrue(extParamCtx.getNextRootCtx().equals(extParamCtx));
      extParamCtx.resetCurrentRootCtx();
      assertTrue(extParamCtx.getNextRootCtx().equals(extCallCtx));
      extParamCtx.resetCurrentRootCtx();
      assertTrue(extParamCtx.getNextRootCtx().equals(ceCondctx));
      
   }
   
   /**
    * Test serializing to and from xml, as well as equals method.
    * 
    * @param ctx The context to test, assumed not <code>null</code>.
    * 
    * @return The serialized/deserialized context, never <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   private PSApplicationIdContext testXml(PSApplicationIdContext ctx) 
      throws Exception
   {
      System.err.println("testing xml for ctx: " + ctx.getDisplayText());
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = ctx.toXml(doc);

      return PSApplicationIDContextFactory.fromXml(el);
   }
   
   
   
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSApplicationIdContextTest("testXml"));
      return suite;
   }
   
}
