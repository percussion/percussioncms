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
package com.percussion.server.config;

import com.percussion.server.PSRequest;
import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSConfigHelperTestCase;
import com.percussion.testing.PSRequestHandlerTestSuite;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link PSRelationshipConfigurationCache} class.
 */
@Category(IntegrationTest.class)
public class PSConfigManagerTest extends PSConfigHelperTestCase 
   implements IPSServerBasedJunitTest

{
   // see base class
   public PSConfigManagerTest(String name)
   {
      super(name);
   }

   /* (non-Javadoc)
    * @see com.percussion.testing.IPSServerBasedJunitTest#oneTimeSetUp(com.percussion.server.PSRequest)
    */
   public void oneTimeSetUp(PSRequest req) {
      // TODO Auto-generated method stub
      
   }

   /**
    * The loadable handler will call this method once before any test method.
    *
    * @param req The request that was passed to the loadable handler.
    *            Never <code>null</code>;
    */
   @Override
   public void oneTimeSetUp(Object req) {

   }

   /* (non-Javadoc)
    * @see com.percussion.testing.IPSServerBasedJunitTest#oneTimeTearDown()
    */
   public void oneTimeTearDown() {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Test all public interfaces including the constructor.
    * @throws Exception if any errors occur.
    */  
   public void testConstruction() throws Exception
   {
      PSConfigManager manager = null;
      try
      {
         manager = PSConfigManager.getInstance();
         manager.reloadConfigs();
      }
      catch (Exception e)
      {
         assertTrue("construction failed", false);
      }
   }
}
