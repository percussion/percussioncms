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

package com.percussion.server.actions;

import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.design.objectstore.server.PSXmlObjectStoreHandler;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionManager;
import com.percussion.server.PSRequestTest;

import java.util.Properties;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Tests the functionality of <code>PSActionSetRequestHandler</code>.
 */
@Category(IntegrationTest.class)
public class PSActionSetRequestHandlerTest
{
   /**
    * Constructs an instance of this class to run the test implemented by the
    * named method.
    * 
    * @param methodName name of the method that implements a test
    */
   public PSActionSetRequestHandlerTest()
   {

   }

   /**
    * Tests that <code>PSActionSetRequestHandler</code> can be constructed
    * and that it will not process requests until inited.
    * <p>
    * Unfortunately, init'ing the handler and processing requests require
    * a running server, and are therefore tested as an AutoTest instead of a
    * UnitTest.
    */
   @Test
   public void testCtor() throws Exception
   {
      Properties osProps = new Properties();
      osProps.setProperty( "driverType", "XML" );
      osProps.setProperty( "objectDirectory", RESOURCE_PATH );
      IPSObjectStoreHandler osh = new PSXmlObjectStoreHandler( osProps );
      IPSExtensionManager extMgr = new PSExtensionManager();
      
      PSActionSetRequestHandler rh = new PSActionSetRequestHandler(
         osh, extMgr );

      // makes sure the requests cannot be proceed when handler not init
      boolean didThrow = false;
      try
      {
         rh.processRequest( 
            PSRequestTest.makeRequest(null,null,null,null,null,null,null) );
      } catch (IllegalStateException e)
      {
         didThrow = true;
      }
      assertTrue( didThrow );
   }

   /**
    * Defines the path to the files used by this unit test, relative from the
    * E2 root.
    */
   private static final String RESOURCE_PATH =
      "/com/percussion/server/actions/";
   
}
