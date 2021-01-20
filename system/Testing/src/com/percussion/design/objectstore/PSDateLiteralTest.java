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

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the <code>PSDateLiteralTest</code> class.
 */
public class PSDateLiteralTest extends TestCase
{
   public PSDateLiteralTest(String name)
   {
      super( name );
   }


   /**
    * Tests that the <code>clone()</code> method creates a separate-but-equal
    * instance, including fields defined in the superclass, and that the copy
    * was deep.
    * 
    * @throws Exception if the test fails.
    */ 
   public void testClone() throws Exception
   {
      Date now = new Date();
      SimpleDateFormat format = new SimpleDateFormat();
      PSDateLiteral foo = new PSDateLiteral( now, format );
      foo.setId( 99 );
      PSDateLiteral bar = (PSDateLiteral) foo.clone();

      assertEquals( foo, bar );
      assertTrue( "id copied", bar.getId() == 99 );
      assertTrue( "m_date copied", bar.getDate().equals( now ) );

      now.setTime( 1 ); // mutate
      assertTrue( "foo changed", foo.getDate().getTime() == 1 );
      assertTrue( "bar unchanged", bar.getDate().getTime() != 1 );
      assertTrue( !foo.equals( bar ) );
   }


   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSDateLiteralTest( "testClone" ) );
      return suite;
   }
}
