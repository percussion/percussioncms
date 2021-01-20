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
package com.percussion.design.objectstore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link PSRelationshipSet} class.
 */
public class PSRelationshipSetTest extends TestCase
{
   // see base class
   public PSRelationshipSetTest(String name)
   {
      super(name);
   }
   
   /**
    * The all public constructor contracts.
    * 
    * @throws Exception for any error.
    */
   public void testConstructors() throws Exception
   {
      Exception exception = null;
      PSRelationshipSet rset = null;
      try
      {
         rset = new PSRelationshipSet(null, null, null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof PSUnknownNodeTypeException);
   }
   
   /**
    * Test public API contracts.
    * 
    * @throws Exception for all errors.
    */
   public void testPublicAPI() throws Exception
   {
      Exception exception = null;
      PSRelationshipSet rset = new PSRelationshipSet();
      try
      {
         rset.toXml(null);
      }
      catch (Exception e)
      {
         exception = e;
      }
      assertTrue(exception instanceof NullPointerException);
   }
   
   // collect all tests into a TestSuite and return it - see base class
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      
      suite.addTest(new PSRelationshipSetTest("testConstructors"));
      suite.addTest(new PSRelationshipSetTest("testPublicAPI"));
      
      return suite;
   }
}