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
package com.percussion.design.objectstore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

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
      FastDateFormat format = FastDateFormat.getInstance();
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
