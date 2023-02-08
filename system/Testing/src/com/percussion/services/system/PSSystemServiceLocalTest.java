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

