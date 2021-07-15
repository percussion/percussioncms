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
