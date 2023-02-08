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
/*
 * test.percussion.pso.utils SimplifyParameterMapTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.percussion.pso.utils.SimplifyParameters;
import junit.framework.TestCase;

public class SimplifyParameterMapTest extends TestCase
{
   public static void main(String[] args)
   {
      junit.textui.TestRunner.run(SimplifyParameterMapTest.class);
   }
  
   public void testSimplifyValue()
   {
     assertEquals("xyzzy",SimplifyParameters.simplifyValue("xyzzy")); 
     assertEquals("foo", SimplifyParameters.simplifyValue(new String[]{"foo", "bar"}));
     assertEquals("", SimplifyParameters.simplifyValue(new String[0])); 
     ArrayList<String> l = new ArrayList<String>();
     //empty list test
     assertEquals("",SimplifyParameters.simplifyValue(l)); 
     l.add("a");
     l.add("b");
     l.add("c"); 
     assertEquals("a",SimplifyParameters.simplifyValue(l)); 
     assertNull(SimplifyParameters.simplifyValue(null)); 
   }
   
   /*
    * Test method for 'com.percussion.pso.utils.SimplifyParameters.simplify(Map<String, Object>)'
    */
   public void testSimplifyMap()
   {
      Map<String,Object> inmap = new HashMap<String, Object>(); 
      List<String> inlist = new ArrayList<String>();
      inlist.add("X");
      inlist.add("Y");
      inlist.add("Z"); 
      inmap.put("list", inlist); 
      inmap.put("string", "xyzzy"); 
      inmap.put("array", new String[]{"fee","fie","fo","fum"});
      
      Map<String,String> outmap = SimplifyParameters.simplifyMap(inmap); 
      assertEquals("xyzzy",outmap.get("string")); 
      assertEquals("X", outmap.get("list")); 
      assertEquals("fee",outmap.get("array")); 
      
   }
   
   @SuppressWarnings("unchecked")
   public void testGetValueAsList()
   {
      List<String> result; 
      result = SimplifyParameters.getValueAsList("foo");
      assertEquals(1,result.size());
      assertEquals("foo",result.get(0));
      
      result = SimplifyParameters.getValueAsList("fee,fie,fo,fum"); 
      assertEquals(4,result.size()); 
      assertEquals("fee", result.get(0)); 
      assertEquals("fum", result.get(3));
      
      result = SimplifyParameters.getValueAsList("fee;fie:fo:fum"); 
      assertEquals(4,result.size()); 
      assertEquals("fee", result.get(0)); 
      assertEquals("fum", result.get(3));
      
      String[] arr = {"fee", "fie", "fum"}; 
      result = SimplifyParameters.getValueAsList(arr);
      assertEquals(3,result.size()); 
      assertEquals("fee", result.get(0)); 
      assertEquals("fum", result.get(2));
      
      List in = Arrays.asList(arr);
      result = SimplifyParameters.getValueAsList(in);
      assertEquals(3,result.size()); 
      assertEquals("fee", result.get(0)); 
      assertEquals("fum", result.get(2));
      
      
   }
}
