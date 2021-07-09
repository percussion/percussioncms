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
package com.percussion.services.assembly.data;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IPSReflectionFilter;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.PSReflectionHelper;
import junit.framework.Assert;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.IOUtils;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Test assembly work items
 */
@SuppressWarnings("unused")
@Category(IntegrationTest.class)
public class PSAssemblyWorkItemTest extends ServletTestCase
{
   /**
    * 
    */
   private static final byte[] REFARRAY =
   {1, 2, 3, 4, 5, 6, 7};
   
   /**
    * 
    */
   private static final byte[] BIGRESULT = new byte[100000];
   
   /**
    * Initialize big result
    */
   static {
      Random rand = new Random();
      rand.nextBytes(BIGRESULT);
   }

   private static final IPSGuidManager ms_guidMgr = PSGuidManagerLocator.getGuidMgr();

   /**
    * @param item
    * @param bigresult TODO
    * @param usestream TODO
    * @throws IOException 
    */
   void setupWorkItemData(PSAssemblyWorkItem item, boolean bigresult,
         boolean usestream) throws IOException
   {
      item.setBindings(new HashMap<String, Object>());
      item.setId(new PSGuid(PSTypeEnum.ITEM, 1));
      item.setJobId(-1);
      item.setMimeType("text/html");
      item.setParameters(new HashMap<String, String[]>());
      item.setPath("/foo/bar");
      item.setReferenceId(-1);
      if (usestream)
      {
         if (bigresult)
            item.setResultStream(new ByteArrayInputStream(BIGRESULT));
         else
            item.setResultStream(new ByteArrayInputStream(REFARRAY));
      }
      else
      {
         if (bigresult)
            item.setResultData(BIGRESULT);
         else
            item.setResultData(REFARRAY);
      }
      item.setFilter(new IPSItemFilter()
      {
         public List<IPSFilterItem> filter(List<IPSFilterItem> ids,
               Map<String, String> params) throws PSFilterException
         {
            return null;
         }

         public String getName()
         {
            return "test";
         }

         public void setName(String name) throws PSFilterException
         {
         }

         public String getDescription()
         {
            return "dummy";
         }

         public void setDescription(String description)
         {
         }

         public Integer getLegacyAuthtypeId()
         {
            return null;
         }

         public void setLegacyAuthtypeId(Integer authTypeId)
         {
         }

         public Set<IPSItemFilterRuleDef> getRuleDefs()
         {
            return null;
         }

         public void setRuleDefs(Set<IPSItemFilterRuleDef> rules)
         {
         }

         public void addRuleDef(IPSItemFilterRuleDef def)
         {
         }

         public void removeRuleDef(IPSItemFilterRuleDef def)
         {
         }

         public String toXML() throws IOException, SAXException
         {
            return null;
         }

         public void fromXML(String xmlsource) throws IOException, SAXException
         {
         }

         public IPSGuid getGUID()
         {
            return null;
         }

         public void setGUID(IPSGuid newguid) throws IllegalStateException
         {
         }

         public IPSItemFilter getParentFilter()
         {
            return null;
         }

         public void setParentFilter(IPSItemFilter parentFilter)
         {
         }
      });
   }

   /**
    * Test result data for correct behavior
    * 
    * @throws Exception
    */
   public void testResultData() throws Exception
   {
      PSAssemblyWorkItem item = new PSAssemblyWorkItem();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      setupWorkItemData(item, false, false);
      byte data[] = item.getResultData();
      assertEqualArrays(REFARRAY, data);
      setupWorkItemData(item, false, false);
      InputStream is = item.getResultStream();
      IOUtils.copy(is, bos);
      assertEqualArrays(REFARRAY, bos.toByteArray());
      item.clearResults();
      
      bos = new ByteArrayOutputStream();
      setupWorkItemData(item, true, false);
      data = item.getResultData();
      assertEqualArrays(BIGRESULT, data);
      setupWorkItemData(item, true, false);
      is = item.getResultStream();
      IOUtils.copy(is, bos);
      assertEqualArrays(BIGRESULT, bos.toByteArray());
      item.clearResults();
      
      bos = new ByteArrayOutputStream();
      setupWorkItemData(item, false, true);
      data = item.getResultData();
      assertEqualArrays(REFARRAY, data);
      setupWorkItemData(item, false, true);
      is = item.getResultStream();
      IOUtils.copy(is, bos);
      assertEqualArrays(REFARRAY, bos.toByteArray());
      item.clearResults();
      
      bos = new ByteArrayOutputStream();
      setupWorkItemData(item, true, true);
      data = item.getResultData();
      assertEqualArrays(BIGRESULT, data);
      setupWorkItemData(item, true, true);
      is = item.getResultStream();
      IOUtils.copy(is, bos);
      assertEqualArrays(BIGRESULT, bos.toByteArray());
      item.clearResults();
   }
   
   /**
    * @throws Exception
    */
   public void testObjectMethods() throws Exception
   {
      IPSReflectionFilter filter = new IPSReflectionFilter()
      {
         public boolean acceptMethod(String methodname)
         {
            return !methodname.contains("Node")
                  && !methodname.contains("ResultData")
                  && !methodname.contains("NavHelper")
                  && !methodname.contains("SiteId")
                  && !methodname.contains("Status")
                  && !methodname.contains("Stream");
         }
      };

      PSAssemblyWorkItem first = new PSAssemblyWorkItem();
      PSAssemblyWorkItem second = new PSAssemblyWorkItem();
      setupWorkItemData(first, false, false);
      setupWorkItemData(second, false, false);
      first.getBindings().put("a", "b");
      second.getBindings().put("a", "c");
      first.getParameters().put("test1", new String[]
      {"testval"});
      PSReflectionHelper.testClone(first, second, filter);

      second = (PSAssemblyWorkItem) first.clone();

      PSReflectionHelper.testEquals(first, second, filter);
   }

   private static final String TEST_ITEM_PATH = "//Sites/EnterpriseInvestments/ProductsAndServices"
         + "/Products and Services HomePage Image (News and Glasses).jpg";

   private static final int TEST_ITEM_CONTENTID = 477;

   private static final int TEST_ITEM_REVISION = 1;

   private static final int TEST_ITEM_FOLDERID = 309;

   private static final String TEST_ITEM_PSEUDO_PATH = "/"
         + TEST_ITEM_CONTENTID + "#" + TEST_ITEM_REVISION;

   public void testNormalizeWithNothing() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      try
      {
         item.normalize();
         fail("normalize() without any identities should throw exception");
      }
      catch (PSAssemblyException e)
      {
         assertEquals(IPSAssemblyErrors.PARAMS_ITEM_SPEC, e.getErrorCode());
         assertNotNull(e.getMessage());
      }
   }

   public void testNormalizeWithIdAndFolder() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setId(ms_guidMgr.makeGuid(new PSLocator(TEST_ITEM_CONTENTID, TEST_ITEM_REVISION)));
      item.setFolderId(TEST_ITEM_FOLDERID);
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      assertParameterEquals(TEST_ITEM_FOLDERID, item,
            IPSHtmlParameters.SYS_FOLDERID);

      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PATH, item.getPath());
   }

   public void testNormalizeWithIdOnly() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setId(ms_guidMgr.makeGuid(new PSLocator(TEST_ITEM_CONTENTID, TEST_ITEM_REVISION)));
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      // the sys_folderid property should not be assigned when no folder is set
      assertNull(item.getParameterValue(IPSHtmlParameters.SYS_FOLDERID, null));
      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PSEUDO_PATH, item.getPath());
   }

   public void testNormalizeWithMismatchId() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, String[]> params = new HashMap<String, String[]>();

      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      params.put("sys_contentid", new String[]
      {"999"});
      params.put("sys_revision", new String[]
      {Integer.toString((TEST_ITEM_REVISION))});
      item.setParameters(params);
      item.setId(ms_guidMgr.makeGuid(new PSLocator(TEST_ITEM_CONTENTID, TEST_ITEM_REVISION)));
      try
      {
         item.normalize();
         fail("normalize() with guid/contentid mismatch should throw exception");
      }
      catch (PSAssemblyException e)
      {
         assertEquals(IPSAssemblyErrors.PARAMS_ITEM_ID_MISMATCH, e
               .getErrorCode());
         assertNotNull(e.getMessage());
      }
   }

   public void testNormalizeWithMismatchFolder() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, String[]> params = new HashMap<String, String[]>();

      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      params.put("sys_contentid", new String[]
      {Integer.toString((TEST_ITEM_CONTENTID))});
      params.put("sys_revision", new String[]
      {Integer.toString((TEST_ITEM_REVISION))});
      params.put("sys_folderid", new String[]
      {Integer.toString((TEST_ITEM_FOLDERID))});
      item.setParameters(params);
      item.setId(ms_guidMgr.makeGuid(new PSLocator(TEST_ITEM_CONTENTID, TEST_ITEM_REVISION)));
      item.setFolderId(999);
      try
      {
         item.normalize();
         fail("normalize() with folder/sys_folderid mismatch should throw exception");
      }
      catch (PSAssemblyException e)
      {
         assertEquals(IPSAssemblyErrors.PARAMS_ITEM_FOLDER_MISMATCH, e
               .getErrorCode());
         assertNotNull(e.getMessage());
      }
   }

   public void testNormalizeWithParams() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, String[]> params = new HashMap<String, String[]>();

      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      params.put("sys_contentid", new String[]
      {Integer.toString((TEST_ITEM_CONTENTID))});
      params.put("sys_revision", new String[]
      {Integer.toString((TEST_ITEM_REVISION))});
      params.put("sys_folderid", new String[]
      {Integer.toString((TEST_ITEM_FOLDERID))});
      item.setParameters(params);
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      assertParameterEquals(TEST_ITEM_FOLDERID, item,
            IPSHtmlParameters.SYS_FOLDERID);

      assertNotNull(item.getId());
      PSLocator loc = ms_guidMgr.makeLocator(item.getId());
      assertEquals(TEST_ITEM_CONTENTID, loc.getId());
      assertEquals(TEST_ITEM_REVISION, loc.getRevision());

      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PATH, item.getPath());
   }

   public void testNormalizeWithParamsNoFolder() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, String[]> params = new HashMap<String, String[]>();

      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      params.put("sys_contentid", new String[]
      {Integer.toString((TEST_ITEM_CONTENTID))});
      params.put("sys_revision", new String[]
      {Integer.toString((TEST_ITEM_REVISION))});
      item.setParameters(params);
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      assertNull(item.getParameterValue(IPSHtmlParameters.SYS_FOLDERID, null));

      assertNotNull(item.getId());
      PSLocator loc = ms_guidMgr.makeLocator(item.getId());
      assertEquals(TEST_ITEM_CONTENTID, loc.getId());
      assertEquals(TEST_ITEM_REVISION, loc.getRevision());
      assertEquals(0, item.getFolderId());

      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PSEUDO_PATH, item.getPath());
   }

   private void assertParameterEquals(int expectedValue,
         PSAssemblyWorkItem item, String parameterName)
   {
      assertEquals(Integer.toString((expectedValue)), item.getParameterValue(
            parameterName, null));
   }

   public void testNormalizeWithMissingPath() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setPath("//Sites/EnterpriseInvestments/Prod/item");
      try
      {
         item.normalize();
         fail("normalize() with missing path should throw exception");
      }
      catch (PSAssemblyException e)
      {
         assertEquals(IPSAssemblyErrors.MISSING_PATH, e.getErrorCode());
      }
   }

   public void testNormalizeWithPath() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setPath(TEST_ITEM_PATH);
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      assertParameterEquals(TEST_ITEM_FOLDERID, item,
            IPSHtmlParameters.SYS_FOLDERID);

      assertNotNull(item.getId());
      PSLocator loc = ms_guidMgr.makeLocator(item.getId());
      assertEquals(TEST_ITEM_CONTENTID, loc.getId());
      assertEquals(TEST_ITEM_REVISION, loc.getRevision());
      assertEquals(TEST_ITEM_FOLDERID, item.getFolderId());

      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PATH, item.getPath());
   }

   public void testNormalizeWithPseudoPathAndFolder() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, String[]> params = new HashMap<String, String[]>();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      params.put("sys_folderid", new String[]
      {Integer.toString((TEST_ITEM_FOLDERID))});
      item.setParameters(params);
      item.setPath(TEST_ITEM_PSEUDO_PATH);
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      assertParameterEquals(TEST_ITEM_FOLDERID, item,
            IPSHtmlParameters.SYS_FOLDERID);

      assertNotNull(item.getId());
      PSLocator loc = ms_guidMgr.makeLocator(item.getId());
      assertEquals(TEST_ITEM_CONTENTID, loc.getId());
      assertEquals(TEST_ITEM_REVISION, loc.getRevision());
      assertEquals(TEST_ITEM_FOLDERID, item.getFolderId());

      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PATH, item.getPath());
   }

   public void testNormalizeWithPseudoPathNoFolder() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setPath(TEST_ITEM_PSEUDO_PATH);
      item.normalize();

      assertParameterEquals(TEST_ITEM_CONTENTID, item,
            IPSHtmlParameters.SYS_CONTENTID);
      assertParameterEquals(TEST_ITEM_REVISION, item,
            IPSHtmlParameters.SYS_REVISION);
      assertNull(item.getParameterValue(IPSHtmlParameters.SYS_FOLDERID, null));

      assertNotNull(item.getId());
      PSLocator loc = ms_guidMgr.makeLocator(item.getId());
      assertEquals(TEST_ITEM_CONTENTID, loc.getId());
      assertEquals(TEST_ITEM_REVISION, loc.getRevision());
      assertEquals(0, item.getFolderId());

      assertNotNull(item.getPath());
      assertEquals(TEST_ITEM_PSEUDO_PATH, item.getPath());
   }

   /**
    * @throws Exception
    */
   public void testNormalization1() throws Exception
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      Map<String, String[]> params = new HashMap<String, String[]>();

      params.put("sys_contentid", new String[]
      {"477"});
      params.put("sys_revision", new String[]
      {"1"});
      params.put("sys_folderid", new String[]
      {"309"});

      PSAssemblyWorkItem item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setParameters(params);
      item.setReferenceId(1);
      item.setJobId(10);
      item.normalize();
      assertEquals("477", item.getParameterValue("sys_contentid", null));
      assertEquals("1", item.getParameterValue("sys_revision", null));
      assertEquals("309", item.getParameterValue("sys_folderid", null));
      assertNotNull(item.getId());

      item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setParameters(params);
      item.setReferenceId(2);
      item.setJobId(11);
      item.normalize();

      params = new HashMap<String, String[]>();
      params.put("sys_contentid", new String[]
      {"477"});
      params.put("sys_revision", new String[]
      {"1"});

      item = (PSAssemblyWorkItem) asm.createAssemblyItem();
      item.setParameters(params);
      item.setReferenceId(1);
      item.setJobId(10);
      item.normalize();
      assertNotNull(item.getPath());

      // Test special node loading code for legacy
      assertNotNull(item.getNode());

   }
   
   /**
    * Compare the elements of two arrays for equality
    * @param a the first array
    * @param b the second array
    */
   public static void assertEqualArrays(byte a[], byte b[])
   {
      if (!Arrays.equals(a, b))
      {
         Assert.fail();
      }
   }
}
