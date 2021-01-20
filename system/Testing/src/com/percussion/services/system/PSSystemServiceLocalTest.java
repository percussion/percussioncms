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
package com.percussion.services.system;

import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.system.data.PSUIComponent;
import com.percussion.services.system.data.PSUIComponentProperty;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.UnitTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for system service calls that don't require a running server.
 */
@Category(IntegrationTest.class)
public class PSSystemServiceLocalTest
{
   /**
    * Test loading content status history. Assumes database is available and
    * has content status entries for a fixed content id (currently 471).
    */
   @Test
   public void testLoadContentStatusHistory()
   {
      int contentId = 471;
      IPSGuid id = new PSLegacyGuid(contentId, 1);
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      List<PSContentStatusHistory> histList = svc.findContentStatusHistory(id);
      assertFalse(histList.isEmpty());
   }
   
   /**
    * Test the read only ui component objects
    */
   @Test
   public void testLoadUIComponents()
   {
      IPSSystemService svc = PSSystemServiceLocator.getSystemService();
      PSUIComponent c = svc.findComponentByName("cmp_banner");
      assertNotNull(c);
      assertNotNull(c.getProperties());
      assertTrue(c.getProperties().size() > 0);
      
      boolean found = false;
      for(PSUIComponentProperty prop : c.getProperties())
      {
         if (prop.getName().equals("wfrole")) 
         {
            found = true;
            break;
         }
      }
      
      assertTrue(found);
   }
}

