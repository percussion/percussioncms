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
package com.percussion.log;

import com.percussion.testing.IPSServerBasedJunitTest;
import com.percussion.testing.PSConfigHelperTestCase;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the PSLogManager class. These are private and
 * are not to be shipped with the product.
 */
@Category(IntegrationTest.class)
public class PSLogManagerTest extends PSConfigHelperTestCase
   implements IPSServerBasedJunitTest
{
   /*
    * This is the necessary request handler to run tests using
    * this class
      <RequestHandlerDef handlerName="JUnitTestHandler"
        className="com.percussion.testing.PSJunitRequestHandler" >
        <RequestRoots>
           <RequestRoot baseName="sys_junitTestHandler">
              <RequestType>POST</RequestType>
              <RequestType>GET</RequestType>
           </RequestRoot>
        </RequestRoots>
      </RequestHandlerDef>
   */

   public PSLogManagerTest(String name)
   {
      super(name);
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
    * Init should throw if you pass a null properties
    */
   public void testInitWithOneNullArg() throws Exception
   {
      boolean didThrow = false;
      try {
         PSLogManager.init(null);
      }
      catch (IllegalArgumentException e) {
         didThrow = true;
      }
      finally
      {
         PSLogManager.close();
      }
      assertTrue(didThrow);
   }

   /**
    * Init should throw if you pass a null properties and a null
    * location
    */
   @Test
   public void testInitWithTwoNullArgs() throws Exception
   {
      boolean didThrow = false;
      try {
         PSLogManager.init(null, null);
      }
      catch (IllegalArgumentException e) {
         didThrow = true;
      }
      finally
      {
         PSLogManager.close();
      }
      assertTrue(didThrow);
   }

   /**
    * Init should throw if you pass an empty properties
    */
   public void testInitWithEmptyProps() throws Exception
   {
      boolean didThrow = false;
      try {
         PSLogManager.init(m_emptyProps);
      }
      catch (IllegalArgumentException e) {
         didThrow = true;
      }
      finally
      {
         PSLogManager.close();
      }
      assertTrue(didThrow);
   }

   /**
    * Test init with valid DBMS properties
    */
   public void testInitWithDBMSProps() throws Exception
   {
      try
      {
         PSLogManager.init(m_validDBMSProps);
         assertTrue(PSLogManager.isOpen());
      }
      finally
      {
         PSLogManager.close();
      }
   }

   /**
    * Test init with valid local file properties
    */
   public void testInitWithFileProps() throws Exception
   {
      try
      {
         PSLogManager.init(m_validFileProps);
         assertTrue(PSLogManager.isOpen());
      }
      finally
      {
         PSLogManager.close();
      }
   }

   /**
    * For the first version of the server, we only support file logging
    * with a file:/// type (local file) URL. Make sure that init throws
    * if the URL is of any other type.
    */
   public void testInitWithFilePropsRemoteURL() throws Exception
   {
      boolean didThrow = false;
      try {
         java.util.Properties props =
            (java.util.Properties )m_validFileProps.clone();
         props.put("logUrl", "http://www.freewill.org/index.html");
         PSLogManager.init(props);
      }
      catch (IllegalArgumentException e) {
         didThrow = true;
      }
      finally
      {
         PSLogManager.close();
      }
      assertTrue(didThrow);
   }



   /**
    * Set up the test case variables
    * @throws Exception
    */
   @BeforeClass
   public void setUp() throws Exception
   {
      m_validDBMSProps  = new Properties(getConnectionProps(CONN_TYPE_SQL));
      m_validDBMSProps.setProperty("logTo", "DBMS");

      m_validFileProps  = new Properties(getConnectionProps(CONN_TYPE_RXSERVER));
      m_validFileProps.setProperty("logTo", "FILE");

      File f =  File.createTempFile("test", ".log");
      m_validFileProps.setProperty("logUrl", "file:///" + f.getAbsolutePath());

      m_emptyProps = new Properties();
   }

   private Properties m_validDBMSProps = null;
   private Properties m_validFileProps = null;
   private Properties m_emptyProps = null;

}
