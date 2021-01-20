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
