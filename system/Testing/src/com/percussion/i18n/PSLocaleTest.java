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

package com.percussion.i18n;

import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Unit test for the <code>PSLocale</code> class
 */
@Category(UnitTest.class)
public class PSLocaleTest
{
   /**
    * Tests the constructors (XML ctor is tested by {@link #testXml()}).
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testCtors() throws Exception
   {
      assertTrue(checkMemberCtor("en-us", "english", null, 
         PSLocale.STATUS_ACTIVE));
      assertTrue(checkMemberCtor("en-us", "english", "", 
         PSLocale.STATUS_ACTIVE));
      assertTrue(checkMemberCtor("en-us", "english", "foo", 
         PSLocale.STATUS_ACTIVE));
      assertTrue(!checkMemberCtor(null, "english", null, 
         PSLocale.STATUS_ACTIVE));
      assertTrue(!checkMemberCtor("", "english", null, 
         PSLocale.STATUS_ACTIVE));
      assertTrue(!checkMemberCtor("en-us", null, null, 
         PSLocale.STATUS_ACTIVE));
      assertTrue(!checkMemberCtor("en-us", "", null, 
         PSLocale.STATUS_ACTIVE));
      assertTrue(!checkMemberCtor("en-us", "english", null, 
         -1));
      
      String[] colnames = 
      {
         "LANGUAGESTRING",
         "DISPLAYNAME",
         "DESCRIPTION",
         "STATUS"
      };
      
      Map<String[], Boolean> valMap = new HashMap<String[], Boolean>();
      String[] validRowVals = new String[] {"en-us", "english", null, "1"};
      valMap.put(validRowVals, true);
      valMap.put(new String[] {"en-us", "english", "", "0"}, true);
      valMap.put(new String[] {null, "english", null, "1"}, false);
      valMap.put(new String[] {"", "english", null, "1"}, false);
      valMap.put(new String[] {"en-us", null, null, "1"}, false);
      valMap.put(new String[] {"en-us", "", null, "1"},  false);
      valMap.put(new String[] {"en-us", "", null, "foo"}, false);
      valMap.put(new String[] {"en-us", "", null, "2"}, false);

      Iterator entries = valMap.entrySet().iterator();
      while (entries.hasNext())
      {
         List cols = new ArrayList();
         Map.Entry entry = (Map.Entry)entries.next();
         String[] colvals = (String[])entry.getKey();
         boolean result = ((Boolean)entry.getValue()).booleanValue();
         for (int i = 0; i < colvals.length; i++) 
         {
            cols.add(new PSJdbcColumnData(colnames[i], colvals[i]));
         }
         PSJdbcRowData rowData = new PSJdbcRowData(cols.iterator(), 
            PSJdbcRowData.ACTION_INSERT);
         assertEquals(checkRowDataCtor(rowData), result);
      }
      
      // test missing col
      List cols = new ArrayList();
      for (int i = 0; i < colnames.length - 1; i++) 
      {
         cols.add(new PSJdbcColumnData(colnames[i], validRowVals[i]));
      }
      PSJdbcRowData badRow = new PSJdbcRowData(cols.iterator(), 
         PSJdbcRowData.ACTION_INSERT);
      assertTrue(!checkRowDataCtor(badRow));
   }
   
   /**
    * Tests the equals and hashcode methods.
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testEquals() throws Exception
   {
      PSLocale locale1 = new PSLocale("en-us", "english", null, 
         PSLocale.STATUS_ACTIVE);
      PSLocale locale2 = new PSLocale("en-us", "english", "desc", 
         PSLocale.STATUS_ACTIVE);
      PSLocale locale3 = new PSLocale("en-uk", "english", null, 
         PSLocale.STATUS_ACTIVE);
      PSLocale locale4 = new PSLocale("en-us", "dude", null, 
         PSLocale.STATUS_ACTIVE);
      PSLocale locale5 = new PSLocale("en-us", "english", null, 
         PSLocale.STATUS_INACTIVE);
      PSLocale locale6 = new PSLocale("en-us", "english", "", 
         PSLocale.STATUS_ACTIVE);
      PSLocale localeSame = new PSLocale("en-us", "english", null, 
         PSLocale.STATUS_ACTIVE);
      
      assertEquals(locale1, localeSame);
      assertTrue(!locale1.equals(locale2));
      assertTrue(!locale1.equals(locale3));
      assertTrue(!locale1.equals(locale4));
      assertTrue(!locale1.equals(locale5));
      assertTrue(!locale1.equals(locale6));
   }
   
   /**
    * Tests the serialization to and from XML.
    * 
    * @throws Exception if there are any errors.
    */
   @Test
   public void testXml() throws Exception
   {
      Document doc;
      PSLocale locale1 = new PSLocale("en-us", "english", null, 
         PSLocale.STATUS_ACTIVE);
      PSLocale locale2 = new PSLocale("en-us", "english", "desc", 
         PSLocale.STATUS_INACTIVE);
      
      
      PSLocale localeXml;
      doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el;
      el = locale1.toXml(doc);
      System.out.println(PSXmlDocumentBuilder.toString(el));
      localeXml = new PSLocale(el);
      assertEquals(locale1, localeXml);
      
      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = locale2.toXml(doc);
      localeXml = new PSLocale(el);
      assertEquals(locale2, localeXml);
      
   }
   
   /**
    * Test the construction of a <code>PSLocale</code> from its member data.
    * See the constructor of this object for parameter information.
    * 
    * @return <code>true</code> if the construction using the supplied params
    * does not throw any exceptions, <code>false</code> otherwise.
    */   
   private boolean checkMemberCtor(String langString, String dispName, 
      String desc, int status)
   {
      boolean success = true;
      try
      {
         new PSLocale(langString, dispName, desc, status);
      }
      catch(Exception e)
      {
         System.err.println(e.toString());
         success = false;
      }
      
      return success;
   }
   
   /**
    * Test the construction of a <code>PSLocale</code> from a 
    * <code>PSJdbcRowData</code>.
    * See the constructor of this object for parameter information.
    * 
    * @return <code>true</code> if the construction using the supplied params
    * does not throw any exceptions, <code>false</code> otherwise.
    */   
   private boolean checkRowDataCtor(PSJdbcRowData rowData)
   {
      boolean success = true;
      try
      {
         new PSLocale(rowData);
      }
      catch(Exception e)
      {
         System.err.println(e.toString());
         success = false;
      }
      
      return success;
   }
}
