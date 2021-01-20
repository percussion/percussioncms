/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.cas;

import com.percussion.data.PSConversionException;
import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;


import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

/**
 * @author DougRand
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
@Category(UnitTest.class)
public class PSCreatePortalPropertyListTest
{
   /**
    * Test parameters for the test
    */
   Object[] params =
      {
         "METADATA",
         "searchKeywords",
         "string",
         "",
         "foo, bar",
         "excludeFromSearch",
         "string",
         "",
         "no",
         "categories",
         "string",
         ",",
         "white,yellow,red",
         "regions",
         "integer",
         "",
         "1, 2",
         "locale",
         "string",
         "",
         "en-us",
         "creationDate",
         "date",
         "yyyyMMdd",
         "20030612",
         "emptyValue",
         "date",
         "",
         "",
         "number",
         "integer",
         "",
         "123" };

   String resultDoc =
      "<METADATA>"
         + "<Property name=\"searchKeywords\" pattern=\"\" type=\"string\">foo, bar</Property>"
         + "<Property name=\"excludeFromSearch\" pattern=\"\" type=\"string\">no</Property>"
         + "<Property name=\"categories\" pattern=\"\" type=\"string\">white</Property>"
         + "<Property name=\"categories\" pattern=\"\" type=\"string\">yellow</Property>"
         + "<Property name=\"categories\" pattern=\"\" type=\"string\">red</Property>"
         + "<Property name=\"regions\" pattern=\"\" type=\"integer\">1</Property>"
         + "<Property name=\"regions\" pattern=\"\" type=\"integer\">2</Property>"
         + "<Property name=\"locale\" pattern=\"\" type=\"string\">en-us</Property>"
         + "<Property name=\"creationDate\" pattern=\"yyyyMMdd\" type=\"date\">20030612</Property>"
         + "<Property name=\"number\" pattern=\"\" type=\"integer\">123</Property>"
         + "</METADATA>";
         
   public PSCreatePortalPropertyListTest()
   {
   }

   @Test
   public void testList() throws PSConversionException {
      PSCreatePortalPropertyList test = new PSCreatePortalPropertyList();

         Document doc = (Document) test.processUdf(params, null);
         System.out.println(PSXmlDocumentBuilder.toString(doc));

   }
}
