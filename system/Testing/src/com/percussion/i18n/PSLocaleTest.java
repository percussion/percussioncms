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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
