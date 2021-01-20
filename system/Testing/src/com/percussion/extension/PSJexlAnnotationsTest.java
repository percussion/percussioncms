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
package com.percussion.extension;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

public class PSJexlAnnotationsTest extends TestCase
{
   public PSJexlAnnotationsTest(String name) {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSJexlAnnotationsTest.class);
   }

   @IPSJexlMethod (
         description = "A test method", 
         params = {
            @IPSJexlParam(name = "first", type = "int", description = "first parameter"),
            @IPSJexlParam(name = "second", description = "second parameter")
            }
         )
   public void tryit(int param1, int param2) 
   {
      
   }
         
   public void test1() throws Exception
   {
      Class noargs[] = new Class[] { int.class, int.class } ;
      Method m = this.getClass().getMethod("tryit", noargs);
      Annotation a[] = m.getAnnotations();
      assertTrue(a.length == 1);
      IPSJexlMethod method = (IPSJexlMethod) a[0];
      System.out.println(method.description());
      IPSJexlParam parameters[] = method.params();
      for(IPSJexlParam p : parameters)
      {
         System.out.println("Param " + p);
      }
   }
}
