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
