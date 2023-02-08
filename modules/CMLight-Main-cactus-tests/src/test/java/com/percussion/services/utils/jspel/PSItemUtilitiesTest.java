/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services.utils.jspel;

import com.percussion.security.PSThreadRequestUtils;

import java.util.Collection;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.collections.MultiMap;
import org.junit.experimental.categories.Category;

/**
 * Test item utilities methods. 
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSItemUtilitiesTest extends ServletTestCase
{
   @SuppressWarnings("unchecked")
   public void testSiteInfo()
   {
      MultiMap info = PSItemUtilities.getItemSiteInfo(376);
      assertEquals(1, info.size());
      
      Collection folders = info.values();
      assertEquals(1, folders.size());
      
      info = PSItemUtilities.getItemSiteInfo(455);
      assertEquals(2, info.size());
      
      folders = (Collection) info.get(info.keySet().iterator().next());
      assertEquals(1, folders.size());
   }
   
   public void testFolderPathInfo()
   {
      PSThreadRequestUtils.initServerThreadRequest();
      Integer fid = PSItemUtilities.getFolderIdFromPath(
            "//Sites/EnterpriseInvestments");
      assertNotNull(fid);
      
      fid = PSItemUtilities.getFolderIdFromPath("//X/Y/Z/W");
      assertNull(fid);
   }
   
   public void testSiteIdInfo()
   {
      Long sid = PSItemUtilities.getSiteIdFromName("Enterprise_Investments");
      assertNotNull(sid);
      assertEquals(301, sid.intValue());
      
      sid = PSItemUtilities.getSiteIdFromName("Unknown Site Name");
      assertNull(sid);
   }
   
   public void testTitleInfo()
   {
      String title = PSItemUtilities.getTitle(325);
      assertEquals("Tax", title);
      
      title = PSItemUtilities.getTitle(1000);
      assertNull(title);
   }
}
