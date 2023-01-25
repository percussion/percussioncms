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
package test.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.collections.ListUtils;

import com.percussion.pso.jexl.PSOListTools;

import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class PSOListToolsTest extends TestCase
{

   private Collection emptyList;
   private Collection nullCollection = null;
   private Collection <String> stringVectorSingle;
   private Collection <String> stringVectorThree;
   private String[] stringArrayThree;
   private Vector <String> stringVectorTen;
   private Set <Integer> integerSetFour;
   private Object[] nullArray;
   //private Object[] emptyArray;
   private PSOListTools listTools;
   

   public PSOListToolsTest(String arg0)
   {
      super(arg0);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      listTools = new PSOListTools();
      emptyList = new ArrayList();
      nullArray = null;
      //emptyArray = new Object[] {};
      stringVectorSingle = new Vector<String> ();
      stringVectorSingle.add("one");
      stringVectorThree = new Vector<String> ();
      stringVectorThree.add("a");
      stringVectorThree.add("b");
      stringVectorThree.add("c");
      
      stringArrayThree = new String[] {"a","b","c"};
      
      stringVectorTen = new Vector<String> ();
      String[] tenArray = new String[] {
            "one", "two","three","four","five","six","seven","eight","nine","ten"
      };
      Collection<String>tenList = Arrays.asList(tenArray);
      stringVectorTen.addAll(tenList);
      integerSetFour = new HashSet<Integer>();
      integerSetFour.add(0);
      integerSetFour.add(1);
      integerSetFour.add(2);
      integerSetFour.add(3);
      
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      emptyList = null;
      listTools = null;
      emptyList = null;
      nullArray = null;
      //emptyArray = null;
      stringVectorSingle = null;
      stringVectorThree = null;
      stringVectorTen = null;
      integerSetFour = null;
   }

   /*
    * Test method for 'com.percussion.pso.jexl.PSOListTools.subListUnSafe(Collection, int, int)'
    */
   public void testSubListUnSafe()
   {

   }

   /*
    * Test method for 'com.percussion.pso.jexl.PSOListTools.sublist(Collection, int, int)'
    */
   public void testSublistCollectionIntInt()
   {
      /* 
       * sublist(null, *, *)    = []
      *  sublist([], * ,  *)    = [];
      *  sublist(["a","b","c"], 0, 2)   = ["a","b"]
      *  sublist(["a","b","c"], 2, 0)   = []
      *  sublist(["a","b","c"], 2, 4)   = ["c"]
      *  sublist(["a","b","c"], 4, 6)   = []
      *  sublist(["a","b","c"], 2, 2)   = []
      *  sublist(["a","b","c"], -2, -1) = ["b"]
      *  sublist(["a","b","c"], -4, 2)  = ["a","b"]
      */
      List ab = Arrays.asList("a","b");
      List c = Arrays.asList("c");
      List b = Arrays.asList("b");
      assertNotNull("Empty collection should not be null", listTools.sublist(emptyList, 5, 6));
      assertNotNull("Null value for collection should return an empty list",
            listTools.sublist(nullCollection, 3, 4));
      assertTrue("List should be empty", listTools.sublist(emptyList,5,6).size() == 0);
      
      ////sublist(["a","b","c"], 0, 3)   = ["a","b","c"]
      List abcTest = listTools.sublist(stringVectorThree, 0, 3);
      assertTrue("List should be equal", ListUtils.isEqualList(abcTest, stringVectorThree));
      
      // sublist(["a","b","c"], 2, 4)   = ["c"]
      List cTest = listTools.sublist(stringVectorThree,2,4);
      assertTrue("List should be equal to [\"c\"] but is " + cTest, ListUtils.isEqualList(cTest, c));
      
      //sublist(["a","b","c"], 0, 2)   = ["a","b"]
      List abTest = listTools.sublist(stringVectorThree, 0, 2);
      assertTrue("List should be equal to ['a','b'] ", ListUtils.isEqualList(abTest, ab));
      
      //sublist(["a","b","c"], -2, -1) = ["b"]
      List bTest = listTools.sublist(stringVectorThree, -2, -1);
      assertTrue("List should be equal to ['b'] ", ListUtils.isEqualList(bTest, b));
      
      //sublist(["a","b","c"], -4, 2)  = ["a","b"]
      abTest = listTools.sublist(stringVectorThree,-4,2);
      assertTrue("List should be equal to ['a','b']", ListUtils.isEqualList(abTest,ab));
      
      
   }

   /*
    * Test method for 'com.percussion.pso.jexl.PSOListTools.sublist(Collection, String, String)'
    */
   public void testSublistCollectionStringString()
   {
      List ab = Arrays.asList("a","b");
      List b = Arrays.asList("b");
      //sublist(["a","b","c"], -2, -1) = ["b"]
      List bTest = listTools.sublist(stringVectorThree, "-2", "-1");
      assertTrue("List should be equal to ['b'] ", ListUtils.isEqualList(bTest, b));
      
      //sublist(["a","b","c"], -4, 2)  = ["a","b"]
      List abTest = listTools.sublist(stringVectorThree,"-4","2");
      assertTrue("List should be equal to ['a','b']", ListUtils.isEqualList(abTest,ab));
      try {
         listTools.sublist(stringVectorThree,"wef","");
         fail("IllegalArgumentException should have been thrown");
      } catch (IllegalArgumentException success) {
      }
   }

   /*
    * Test method for 'com.percussion.pso.jexl.PSOListTools.sublist(Object[], int, int)'
    */
   public void testSublistObjectArrayIntInt()
   {
      List abc = Arrays.asList("a","b","c");
      List ab = Arrays.asList("a","b");
      List c = Arrays.asList("c");
      List b = Arrays.asList("b");
      
      assertNotNull("Empty collection should not be null", listTools.sublist(emptyList, 5, 6));
      assertNotNull("Null value for collection should return an empty list",
            listTools.sublist(nullArray, 3, 4));
      assertTrue("List should be empty", listTools.sublist(nullArray,5,6).size() == 0);
      
      ////sublist(["a","b","c"], 0, 3)   = ["a","b","c"]
      List abcTest = listTools.sublist(stringArrayThree, 0, 3);
      assertTrue("List should be equal", ListUtils.isEqualList(abcTest, abc));
      
      // sublist(["a","b","c"], 2, 4)   = ["c"]
      List cTest = listTools.sublist(stringArrayThree,2,4);
      assertTrue("List should be equal to [\"c\"] but is " + cTest, ListUtils.isEqualList(cTest, c));
      
      //sublist(["a","b","c"], 0, 2)   = ["a","b"]
      List abTest = listTools.sublist(stringArrayThree, 0, 2);
      assertTrue("List should be equal to ['a','b'] ", ListUtils.isEqualList(abTest, ab));
      
      //sublist(["a","b","c"], -2, -1) = ["b"]
      List bTest = listTools.sublist(stringArrayThree, -2, -1);
      assertTrue("List should be equal to ['b'] ", ListUtils.isEqualList(bTest, b));
      
      //sublist(["a","b","c"], -4, 2)  = ["a","b"]
      abTest = listTools.sublist(stringArrayThree,-4,2);
      assertTrue("List should be equal to ['a','b']", ListUtils.isEqualList(abTest,ab));
      
   }

   /*
    * Test method for 'com.percussion.pso.jexl.PSOListTools.sublist(Object[], String, String)'
    */
   public void testSublistObjectArrayStringString()
   {
      List ab = Arrays.asList("a","b");
      List b = Arrays.asList("b");
      //sublist(["a","b","c"], -2, -1) = ["b"]
      List bTest = listTools.sublist(stringArrayThree, "-2", "-1");
      assertTrue("List should be equal to ['b'] ", ListUtils.isEqualList(bTest, b));
      
      //sublist(["a","b","c"], -4, 2)  = ["a","b"]
      List abTest = listTools.sublist(stringArrayThree,"-4","2");
      assertTrue("List should be equal to ['a','b']", ListUtils.isEqualList(abTest,ab));
      try {
         listTools.sublist(stringArrayThree,"wef","");
         fail("IllegalArgumentException should have been thrown");
      } catch (IllegalArgumentException success) {
      }

   }

}
