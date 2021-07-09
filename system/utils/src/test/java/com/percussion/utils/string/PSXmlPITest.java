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
package com.percussion.utils.string;

import com.percussion.utils.string.PSXmlPIUtils.Action;
import com.percussion.utils.testing.UnitTest;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class PSXmlPITest
{
   static String ms_test1 = "<doc><el><%active%><?php phpcode?></el><%active2%></doc>";

   static String ms_encode = "<doc><el><!-- @psx_activetag_0 --><!-- @psx_activetag_1 --></el><!-- @psx_activetag_2 --></doc>";

   static String ms_result1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         + "<doc>\n"
         + "   <el><?psx-activetag <%active%>?><?php phpcode?></el><?psx-activetag <%active2%>?>\n"
         + "</doc>";

   @Test
   public void testEncode() throws Exception
   {
      PSStopwatch sw = new PSStopwatch();

      sw.start();
      PSPair<Map<Integer, PSPair<Action, String>>, String> result = PSXmlPIUtils
            .encodeTags(ms_test1);
      sw.stop();
      System.out.println("Encode took " + sw);
      assertEquals(ms_encode, result.getSecond());
      assertEquals(3, result.getFirst().size());
      assertEquals("<%active%>", result.getFirst().get(0).getSecond());
      assertEquals("<?php phpcode?>", result.getFirst().get(1).getSecond());
      assertEquals("<%active2%>", result.getFirst().get(2).getSecond());
      assertEquals(Action.QUOTE, result.getFirst().get(0).getFirst());
      assertEquals(Action.PHP, result.getFirst().get(1).getFirst());
      assertEquals(Action.QUOTE, result.getFirst().get(2).getFirst());
      

      // Dom handling
      sw.start();
      InputStream stream = new ByteArrayInputStream(result.getSecond()
            .getBytes("UTF8"));
      Document doc = PSXmlDocumentBuilder.createXmlDocument(stream, false);
      PSXmlPIUtils.substitutePIs(doc, result.getFirst());
      String str = PSXmlDocumentBuilder.toString(doc);
      sw.stop();
      System.out.println("DOM took " + sw);
      assertEquals(ms_result1, str);
   }

   @Test
   public void testEncode2() throws Exception
   {
      testEncode(); // Just run again for timings
   }   

   static String ms_result2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         + "<doc>\n" + "   <el><%active%><?php phpcode?></el><%active2%>\n" + "</doc>";

   @Test
   public void testStrip() throws Exception
   {
      PSStopwatch sw = new PSStopwatch();

      sw.start();
      String out = PSXmlPIUtils.removePI(ms_result1);
      sw.stop();
      System.out.println("Remove took " + sw);
      assertEquals(ms_result2, out);
   }

}
