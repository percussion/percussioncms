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

/**
 * Unit tests for the <code>PSDisplayTextLiteralTest</code> class.
 */
public class PSDisplayTextLiteralTest extends TestCase
{
   public PSDisplayTextLiteralTest(String name)
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
      PSDisplayTextLiteral foo = new PSDisplayTextLiteral( "foo", "FOOFOO" );
      foo.setId( 99 );
      PSDisplayTextLiteral bar = (PSDisplayTextLiteral) foo.clone();

      assertEquals( foo, bar );
      assertTrue( "id copied", bar.getId() == 99 );
      assertTrue( "m_value copied", bar.getValueText().equals( "FOOFOO" ) );
      bar.setValueText( "bar" );
      assertTrue( "bar changed", bar.getValueText().equals( "bar" ) );
      assertTrue( "foo unchanged", foo.getValueText().equals( "FOOFOO" ) );
      assertTrue( !foo.equals( bar ) );
   }


   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest( new PSDisplayTextLiteralTest( "testClone" ) );
      return suite;
   }
}
