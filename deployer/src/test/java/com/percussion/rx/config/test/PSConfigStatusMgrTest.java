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
package com.percussion.rx.config.test;

import com.percussion.rx.config.IPSConfigStatusMgr;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
import com.percussion.rx.config.impl.PSConfigService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.util.Date;
import java.util.List;

@Category(IntegrationTest.class)
public class PSConfigStatusMgrTest extends ServletTestCase
{
   public void testConfigStatus() throws PSNotFoundException {
      PSConfigService cfgSrvc = (PSConfigService) PSConfigServiceLocator
            .getConfigService();
      IPSConfigStatusMgr mgr = cfgSrvc.getConfigStatusManager();
      //Delete any test entries if exists.
      mgr.deleteConfigStatus("Test%");
      //Create a Test Config entry
      PSConfigStatus cs = mgr.createConfigStatus("Test1");
      cs.setDateApplied(getNewDate());
      cs.setStatus(ConfigStatus.FAILURE);
      cs.setLocalConfig("Test Config");
      cs.setDefaultConfig("Test Default Config");
      mgr.saveConfigStatus(cs);
      //Test the entries
      List<PSConfigStatus> csList = mgr.findConfigStatus("Test1");
      assertEquals(csList.size(), 1);
      //Save the same entry again and make sure there is only one entry 
      mgr.saveConfigStatus(cs);
      csList = mgr.findConfigStatus("Test1");
      assertEquals(csList.size(), 1);
      //Create another entry with the same name.
      cs = mgr.createConfigStatus("Test1");      
      // add a second, as both the first and second one are getting saved
      // with the same date applied time.
      cs.setDateApplied(getNewDate());
      cs.setStatus(ConfigStatus.FAILURE);
      cs.setLocalConfig("Latest Test Config");
      cs.setDefaultConfig("Latest Test Default Config");
      cs.setConfigDef("Latest Test Config Def");
      mgr.saveConfigStatus(cs);
      //Test and make sure there are two entries.
      csList = mgr.findConfigStatus("Test1");
      assertEquals(csList.size(), 2);
      //Create a new entry with name Test2
      cs = mgr.createConfigStatus("Test2");
      cs.setDateApplied(getNewDate());
      cs.setStatus(ConfigStatus.FAILURE);
      cs.setLocalConfig("Test Config1");
      cs.setDefaultConfig("Test Default Config1");
      mgr.saveConfigStatus(cs);

      //Find the config status with name Test1 this should return two entries
      csList = mgr.findConfigStatus("Test1");
      assertEquals(csList.size(), 2);
      //Find the latest config status with name Test1, this should return 
      //only one entry.
      csList = mgr.findLatestConfigStatus("Test1");
      assertEquals(csList.size(), 1);
      assertEquals(csList.get(0).getLocalConfig(), "Latest Test Config");
      assertEquals(csList.get(0).getDefaultConfig(), "Latest Test Default Config");
      assertEquals(csList.get(0).getConfigDef(), "Latest Test Config Def");

      //Find the latest config status with name Test%, this should return two 
      //entries one for Test1 and second one for Test2
      csList = mgr.findLatestConfigStatus("Test%");
      assertEquals(csList.size(), 2);
      assertEquals(csList.get(0).getConfigName(), "Test1");
      assertEquals(csList.get(1).getConfigName(), "Test2");

      PSConfigStatus cs2 = mgr.findLastSuccessfulConfigStatus("Test2");
      //As there is no successful entry for this package make sure cs2 is null
      assertNull(cs2);

      //Creaete a new entry with Test2 as name and set the status to success
      cs = mgr.createConfigStatus("Test2");
      cs.setDateApplied(getNewDate());
      cs.setStatus(ConfigStatus.SUCCESS);
      cs.setLocalConfig("Test Config2");
      cs.setDefaultConfig("Test Default Config2");
      cs.setConfigDef("Test Config Def2");
      mgr.saveConfigStatus(cs);
      
      //Find the last successful status and make sure this time it is not null.
      cs2 = mgr.findLastSuccessfulConfigStatus("Test2");
      assertNotNull(cs2);
      //Make sure it is successful entry
      assertTrue(cs2.getStatus().equals(ConfigStatus.SUCCESS));
      //Create another entry with failure
      cs = mgr.createConfigStatus("Test2");
      cs.setDateApplied(getNewDate());
      cs.setStatus(ConfigStatus.FAILURE);
      cs.setLocalConfig("Test Failed Local Config2");
      cs.setDefaultConfig("Test Failed Default Config2");
      cs.setConfigDef("Test Failed Config Def2");
      mgr.saveConfigStatus(cs);
      
      //Make sure the latest entry is a failure one.
      csList = mgr.findLatestConfigStatus("Test2");
      assertTrue(csList.get(0).getStatus().equals(ConfigStatus.FAILURE));
      assertEquals(csList.get(0).getLocalConfig(), "Test Failed Local Config2");

      // Even though the last entry is failure, we should get the last
      // successful entry
      cs2 = mgr.findLastSuccessfulConfigStatus("Test2");
      assertTrue(cs2.getStatus().equals(ConfigStatus.SUCCESS));
      assertEquals(cs2.getLocalConfig(), "Test Config2");

      //Creaete a new entry with Test2 as name and set the status to success again
      cs = mgr.createConfigStatus("Test2");
      cs.setDateApplied(getNewDate());
      cs.setStatus(ConfigStatus.SUCCESS);
      cs.setLocalConfig("Test Config XXX");
      cs.setDefaultConfig("Test Default Config1");
      mgr.saveConfigStatus(cs);
      
      cs2 = mgr.findLastSuccessfulConfigStatus("Test2");
      //Make sure this time we got the latest successful one.
      assertTrue(cs2.getLocalConfig().equals("Test Config XXX"));

      // Delete a config status entry by id
      mgr.deleteConfigStatus(csList.get(0).getStatusId());

      // Delete all test data
      mgr.deleteConfigStatus("Test%");
      csList = mgr.findConfigStatus("Test1");
      assertEquals(csList.size(), 0);

   }
   private Date getNewDate()
   {
      counter += 100;
      return new Date(System.currentTimeMillis() + counter);
   }
   private int counter = 0;
}
