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
package com.percussion.services.schedule.impl;

import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.services.utils.general.PSServiceConfigurationBean;

import java.io.IOException;
import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Andriy Palamarchuk
 */
@Category(IntegrationTest.class)
public class PSSchedulerBeanTest
{
   @Test
   public void testIsSingleton()
   {
      assertTrue(create().isSingleton());
   }

   @Test
   public void testGetObjectType()
   {
      assertEquals(Scheduler.class, create().getObjectType());
   }

   @Test
   public void testGetObject() throws SchedulerException, IOException
   {
      final PSSchedulerBean bean = new PSSchedulerBean()
      {
         @Override
         void setConnectionProviderDatasourceManager()
         {
            // disable this method, because datasource provider is global,
            // and it fails on an attempt to set datasource manager second time 
         }
      };
      assertNull(bean.getObject());
      
      bean.setQuartzProperties(getSampleQuartzProperties());
      bean.setDatasourceManager(PSDatasourceMgrLocator.getDatasourceMgr());
      bean.setConfigurationBean(new PSServiceConfigurationBean());
      bean.afterPropertiesSet();
      assertTrue(bean.getObject() instanceof Scheduler);
   }

   /**
    * Generates Quartz properties from the default Quartz properties distributed
    * with the library.
    * @return the properties object. Never null.
    * @throws IOException on properties loading failure. 
    */
   private Properties getSampleQuartzProperties() throws IOException
   {
      final Properties properties = new Properties();
      properties.load(getClass().getClassLoader().getResourceAsStream(
            "org/quartz/quartz.properties"));
      properties.put("org.quartz.jobStore.tablePrefix", "PSX_Q");
      return properties;
   }

   /**
    * Convenience method to create a scheduler factory.
    * @return new scheduler factory, never null.
    */
   private PSSchedulerBean create()
   {
      return new PSSchedulerBean();
   }

}
