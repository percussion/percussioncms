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
