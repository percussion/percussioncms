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
package com.percussion.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *   Unit tests for the PSMapClassToObject class
 */
public class PSMapClassToObjectTest extends TestCase
{
   public PSMapClassToObjectTest(String name)
   {
      super(name);
   }

   /**
    *   Verify that an empty map always returns null
    */
   public void testEmptyMap()
   {
      assertTrue(null == m_emptyMap.getMapping(m_parentClass));
      assertTrue(null == m_emptyMap.getMapping(m_sonClass));
      assertTrue(null == m_emptyMap.getMapping(m_daughterClass));
      assertTrue(null == m_emptyMap.getMapping(m_sonsonClass));
      assertTrue(null == m_emptyMap.getMapping(m_daughterdaughterClass));
   }

   /**
    *   Make the mappings more and more specific and test at each stage to
    *   make sure that the most specific mappings apply.
    */
   public void testObjectMapping()
   {
      Object o = new Object();
      String firstString = 
         "There is a boundary to men's passions when they act from feelings; but none when they are under the influence of imagination.";
      String secondString =
         "The only thing necessary for the triumph of evil is for good men to do nothing.";
      String thirdString =
         "Whenever a separation is made between liberty and justice, neither, in my opinion, is safe.";
      String fourthString =
         "Example is the school of mankind, and they will learn at no other.";
      String fifthString =
         "The first and simplest emotion which we discover in the human mind, is curiosity.";
      String sixthString =
         "What ever disunites man from God, also disunites man from man.";

      PSMapClassToObject map = new PSMapClassToObject();
      
      map.addReplaceMapping(o.getClass(), firstString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(firstString, map.getMapping(m_parentClass));
      assertEquals(firstString, map.getMapping(m_sonClass));
      assertEquals(firstString, map.getMapping(m_daughterClass));
      assertEquals(firstString, map.getMapping(m_sonsonClass));
      assertEquals(firstString, map.getMapping(m_daughterdaughterClass));

      map.addReplaceMapping(m_parentClass, secondString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(secondString, map.getMapping(m_parentClass));
      assertEquals(secondString, map.getMapping(m_sonClass));
      assertEquals(secondString, map.getMapping(m_daughterClass));
      assertEquals(secondString, map.getMapping(m_sonsonClass));
      assertEquals(secondString, map.getMapping(m_daughterdaughterClass));

      map.addReplaceMapping(m_parentClass, secondString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(secondString, map.getMapping(m_parentClass));
      assertEquals(secondString, map.getMapping(m_sonClass));
      assertEquals(secondString, map.getMapping(m_daughterClass));
      assertEquals(secondString, map.getMapping(m_sonsonClass));
      assertEquals(secondString, map.getMapping(m_daughterdaughterClass));

      map.addReplaceMapping(m_sonClass, thirdString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(secondString, map.getMapping(m_parentClass));
      assertEquals(thirdString, map.getMapping(m_sonClass));
      assertEquals(secondString, map.getMapping(m_daughterClass));
      assertEquals(thirdString, map.getMapping(m_sonsonClass));
      assertEquals(secondString, map.getMapping(m_daughterdaughterClass));

      map.addReplaceMapping(m_daughterClass, fourthString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(secondString, map.getMapping(m_parentClass));
      assertEquals(thirdString, map.getMapping(m_sonClass));
      assertEquals(fourthString, map.getMapping(m_daughterClass));
      assertEquals(thirdString, map.getMapping(m_sonsonClass));
      assertEquals(fourthString, map.getMapping(m_daughterdaughterClass));

      map.addReplaceMapping(m_sonsonClass, fifthString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(secondString, map.getMapping(m_parentClass));
      assertEquals(thirdString, map.getMapping(m_sonClass));
      assertEquals(fourthString, map.getMapping(m_daughterClass));
      assertEquals(fifthString, map.getMapping(m_sonsonClass));
      assertEquals(fourthString, map.getMapping(m_daughterdaughterClass));

      map.addReplaceMapping(m_daughterdaughterClass, sixthString);
      assertEquals(firstString, map.getMapping(o.getClass()));
      assertEquals(secondString, map.getMapping(m_parentClass));
      assertEquals(thirdString, map.getMapping(m_sonClass));
      assertEquals(fourthString, map.getMapping(m_daughterClass));
      assertEquals(fifthString, map.getMapping(m_sonsonClass));
      assertEquals(sixthString, map.getMapping(m_daughterdaughterClass));
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSMapClassToObjectTest("testEmptyMap"));
      suite.addTest(new PSMapClassToObjectTest("testObjectMapping"));
      return suite;
   }

   // set up the data for the test
   public void setUp()
   {
      m_emptyMap = new PSMapClassToObject();
   }

   class Parent {}
   class Son extends Parent {}
   class Daughter extends Parent {}
   class SonSon extends Son {}
   class DaughterDaughter extends Daughter {}

   private PSMapClassToObject m_emptyMap;
   private Parent m_parent = new Parent();
   private Son m_son = new Son();
   private Daughter m_daughter = new Daughter();
   private SonSon m_sonson = new SonSon();
   private DaughterDaughter m_daughterdaughter = new DaughterDaughter();

   private Class m_parentClass = m_parent.getClass();
   private Class m_sonClass = m_son.getClass();
   private Class m_daughterClass = m_daughter.getClass();
   private Class m_sonsonClass = m_sonson.getClass();
   private Class m_daughterdaughterClass = m_daughterdaughter.getClass();
}
