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
package test.percussion.pso.imageedit.data;

import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.imageedit.data.MasterImageMetaData;

public class MasterImageMetaDataTest
{
   private static final Logger log = LogManager.getLogger(MasterImageMetaDataTest.class);
   
   MasterImageMetaData cut; 
   
   @Before
   public void setUp() throws Exception
   {
      cut = new MasterImageMetaData(); 
   }
   
   @Test 
   public void testConstructor()
   {
      assertNotNull(cut.getSizedImages());
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public void testDescribe() 
   {
      try
      {
         cut.setAlt("alt string");
         cut.setSysTitle("sys title");
         cut.setDisplayTitle("display title"); 
         cut.setDescription("This is the description"); 
         Map  description = BeanUtils.describe(cut);
         assertTrue(description.size() > 0); 
         log.info("Master Image Metadata : " + description); 
         
         PropertyDescriptor[] p = PropertyUtils.getPropertyDescriptors(cut);
         assertNotNull(p); 
         for(PropertyDescriptor pd : p)
         {
            log.info("Property Descriptor " + pd.getName() + " type " 
                  + pd.getPropertyType()); 
         }
         
         BeanInfo beanInfo = Introspector.getBeanInfo(cut.getClass());
         assertNotNull(beanInfo);
         log.info("Bean Info " + beanInfo.getBeanDescriptor().getName());
         for(PropertyDescriptor pd2 : beanInfo.getPropertyDescriptors())
         {
            log.info("Property Descriptor " + pd2.getName() + " type " 
                  + pd2.getPropertyType().getName());
         }
         assertTrue("description complete", true); 
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      }
      
   }
}
